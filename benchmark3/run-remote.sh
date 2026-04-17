#!/usr/bin/env bash
# Run benchmarks on remote server(s). Builds locally, ships jar, runs remotely.
#
# Usage:
#   ./run-remote.sh root@8.136.39.6                          # standard, all suites
#   ./run-remote.sh root@8.136.39.6 eishay quick             # quick eishay only
#   ./run-remote.sh root@8.136.39.6 all full                 # full, all suites
#   ./run-remote.sh root@host1 root@host2 -- all quick       # parallel on 2 hosts
#
# What happens:
#   1. Build benchmark3.jar locally (with all dependencies shaded in)
#   2. Ship jar + run-benchmark.sh + data to remote via tar+ssh
#   3. Run JMH on remote
#   4. Pull results back

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"
LOCAL_RESULTS="$SCRIPT_DIR/results"

# ---- Parse arguments ----
HOSTS=()
BENCH_ARGS=()
PAST_SEPARATOR=false

for arg in "$@"; do
    if [[ "$arg" == "--" ]]; then
        PAST_SEPARATOR=true
        continue
    fi
    if $PAST_SEPARATOR; then
        BENCH_ARGS+=("$arg")
    elif [[ "$arg" == *@* ]] || ssh -G "$arg" 2>/dev/null | grep -q "^hostname"; then
        HOSTS+=("$arg")
    else
        BENCH_ARGS+=("$arg")
    fi
done

if [[ ${#HOSTS[@]} -eq 0 ]]; then
    echo "Usage: $0 user@host [user@host2 ...] [--] [suite] [profile] [threads]"
    echo ""
    echo "Examples:"
    echo "  $0 root@8.136.39.6                                  # standard, default JDK"
    echo "  $0 root@8.136.39.6 eishay quick                     # quick eishay"
    echo "  $0 root@8.136.39.6 eishay quick 16 jdk21,jdk25      # two JDK versions"
    echo "  $0 root@8.136.39.6 all standard 16 all              # all JDKs (>=21)"
    echo "  $0 root@server1 root@server2 -- all full 16 all     # parallel, all JDKs"
    exit 1
fi

SUITE="${BENCH_ARGS[0]:-all}"
PROFILE="${BENCH_ARGS[1]:-standard}"
THREADS="${BENCH_ARGS[2]:-16}"
JDK_ARG="${BENCH_ARGS[3]:-default}"

# ---- 1. Local build ----
echo "═══════════════════════════════════════"
echo " Building locally..."
echo "═══════════════════════════════════════"
mvn package -q -DskipTests 2>&1 | grep -v WARNING || true

if [[ ! -f target/benchmark3.jar ]]; then
    echo "ERROR: target/benchmark3.jar not found"
    exit 1
fi

JAR_SIZE=$(du -h target/benchmark3.jar | cut -f1)
echo "  benchmark3.jar: $JAR_SIZE"

# ---- Run on one host ----
run_on_host() {
    local host="$1"

    echo ""
    echo "═══════════════════════════════════════"
    echo " $host"
    echo "═══════════════════════════════════════"

    local remote_dir="/tmp/benchmark3"

    # 2. Ship jar + script + data
    echo "[1/3] Shipping to remote ($JAR_SIZE)..."
    tar -czf - \
        -C "$SCRIPT_DIR" \
        target/benchmark3.jar \
        run-benchmark.sh \
        src/main/resources/data/ \
        | ssh "$host" "rm -rf $remote_dir && mkdir -p $remote_dir && cd $remote_dir && tar -xzf -"

    # 3. Run on remote (nohup to survive SSH disconnect)
    echo "[2/3] Running ($SUITE $PROFILE ${THREADS}T $JDK_ARG)..."
    ssh "$host" "cd $remote_dir && chmod +x run-benchmark.sh && \
        nohup bash run-benchmark.sh $SUITE $PROFILE $THREADS $JDK_ARG > /tmp/benchmark-run.log 2>&1 & \
        echo \$! > /tmp/benchmark-run.pid && echo PID=\$(cat /tmp/benchmark-run.pid)"

    # Poll until done
    echo "  Waiting for completion (polling every 30s)..."
    while true; do
        ALIVE=$(ssh "$host" 'kill -0 $(cat /tmp/benchmark-run.pid 2>/dev/null) 2>/dev/null && echo "running" || echo "done"')
        if [[ "$ALIVE" == "done" ]]; then
            break
        fi
        LINES=$(ssh "$host" 'wc -l < /tmp/benchmark-run.log 2>/dev/null || echo 0')
        LAST=$(ssh "$host" 'tail -1 /tmp/benchmark-run.log 2>/dev/null')
        echo "  ... $LINES lines | $LAST"
        sleep 30
    done
    echo "  Benchmark finished."
    ssh "$host" 'cat /tmp/benchmark-run.log' 2>/dev/null | tail -30

    # 4. Pull all result directories
    echo "[3/3] Pulling results..."
    ssh "$host" "cd $remote_dir/results 2>/dev/null && tar -czf - ." \
        | (mkdir -p "$LOCAL_RESULTS" && cd "$LOCAL_RESULTS" && tar -xzf -) 2>/dev/null || \
        echo "Warning: could not pull results"

    echo ""
    echo "Results pulled to: $LOCAL_RESULTS/"
    ls -d "$LOCAL_RESULTS"/*/ 2>/dev/null | while read -r d; do
        if [[ -f "$d/env.json" ]]; then
            local jdk
            jdk=$(grep '"version"' "$d/env.json" | head -1 | sed 's/.*: *"\(.*\)".*/\1/')
            echo "  $(basename "$d"): JDK $jdk"
        fi
    done
}

# ---- Run ----
if [[ ${#HOSTS[@]} -eq 1 ]]; then
    run_on_host "${HOSTS[0]}"
else
    echo "Running on ${#HOSTS[@]} hosts in parallel..."
    PIDS=()
    for host in "${HOSTS[@]}"; do
        run_on_host "$host" &
        PIDS+=($!)
    done
    for pid in "${PIDS[@]}"; do
        wait "$pid" || true
    done
fi

# Compare if we have multiple results
RESULT_COUNT=$(find "$LOCAL_RESULTS" -maxdepth 2 -name "env.json" 2>/dev/null | wc -l)
if [[ $RESULT_COUNT -gt 1 ]] && [[ -f "$SCRIPT_DIR/compare-results.sh" ]]; then
    echo ""
    "$SCRIPT_DIR/compare-results.sh"
fi
