#!/usr/bin/env bash
# Benchmark runner with automatic environment capture and result archiving.
#
# Usage:
#   ./run-benchmark.sh eishay                         # standard, default JDK
#   ./run-benchmark.sh eishay quick                   # quick profile
#   ./run-benchmark.sh all full 16                    # full, 16 threads
#   ./run-benchmark.sh eishay quick 16 jdk21          # specify JDK
#   ./run-benchmark.sh eishay quick 16 jdk21,jdk25    # multiple JDKs
#   ./run-benchmark.sh eishay quick 16 all            # all installed JDKs (21+)
#
# Results are saved to: results/<date>-<hostname>-<jdk>/
#
# JDK lookup order:
#   /root/Install/jdkNN  →  /usr/lib/jvm/*-NN-*  →  java on PATH

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Parse arguments ----
SUITE="${1:-all}"
PROFILE="${2:-standard}"
THREADS="${3:-16}"
JDK_ARG="${4:-default}"

# ---- Translate profile to JMH args ----
case "$PROFILE" in
    quick)
        JMH_WARMUP=1; JMH_MEASURE=2; JMH_FORKS=1
        JMH_EXCLUDE="-e .*_reflect.* -e .*_asm.* -e .*\.gson.* -e .*\.jackson.*"
        ;;
    full)
        JMH_WARMUP=3; JMH_MEASURE=5; JMH_FORKS=3
        JMH_EXCLUDE=""
        ;;
    *)  # standard
        JMH_WARMUP=2; JMH_MEASURE=3; JMH_FORKS=2
        JMH_EXCLUDE=""
        ;;
esac

# ---- JDK resolution ----
find_java() {
    local jdk_name="$1"  # e.g. "jdk21" or "jdk25"
    local version="${jdk_name#jdk}"  # e.g. "21" or "25"

    # Try /root/Install/jdkNN
    if [[ -x "/root/Install/${jdk_name}/bin/java" ]]; then
        echo "/root/Install/${jdk_name}/bin/java"
        return
    fi

    # Try /usr/lib/jvm/*-NN-*
    for d in /usr/lib/jvm/*-"${version}"-* /usr/lib/jvm/*"${version}"*; do
        if [[ -x "$d/bin/java" ]]; then
            echo "$d/bin/java"
            return
        fi
    done

    echo ""
}

resolve_jdks() {
    local arg="$1"
    local jdks=()

    if [[ "$arg" == "default" ]]; then
        jdks=("default")
    elif [[ "$arg" == "all" ]]; then
        # Find all installed JDKs >= 21
        for d in /root/Install/jdk*; do
            local ver="${d##*/root/Install/jdk}"
            if [[ "$ver" =~ ^[0-9]+$ ]] && [[ $ver -ge 21 ]]; then
                jdks+=("jdk${ver}")
            fi
        done
        for d in /usr/lib/jvm/*; do
            if [[ -x "$d/bin/java" ]]; then
                local ver
                ver=$("$d/bin/java" -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/')
                if [[ "$ver" =~ ^[0-9]+$ ]] && [[ $ver -ge 21 ]]; then
                    local name="jdk${ver}"
                    # Avoid duplicates
                    local found=false
                    for j in "${jdks[@]}"; do [[ "$j" == "$name" ]] && found=true; done
                    $found || jdks+=("$name")
                fi
            fi
        done
        # Sort
        IFS=$'\n' jdks=($(sort -V <<<"${jdks[*]}")); unset IFS
    else
        # Comma-separated: jdk21,jdk25
        IFS=',' read -ra jdks <<< "$arg"
    fi

    echo "${jdks[@]}"
}

# ---- Build if needed ----
if [[ ! -f target/benchmark3.jar ]]; then
    echo "Building benchmark3..."
    mvn package -q -DskipTests 2>&1 | grep -v WARNING || true
fi

# ---- Capture environment ----
capture_env() {
    local env_file="$1"
    local java_cmd="$2"

    # CPU info
    local cpu_model
    if [[ -f /proc/cpuinfo ]]; then
        cpu_model=$(grep 'model name' /proc/cpuinfo 2>/dev/null | head -1 | cut -d: -f2 | xargs || true)
        # ARM may not have 'model name'
        [[ -z "$cpu_model" ]] && cpu_model=$(uname -m)
    elif command -v sysctl &>/dev/null; then
        cpu_model=$(sysctl -n machdep.cpu.brand_string 2>/dev/null || echo "$(uname -m)")
    else
        cpu_model="$(uname -m)"
    fi

    local cpu_cores
    cpu_cores=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 0)

    # Memory
    local mem_total
    if [[ -f /proc/meminfo ]]; then
        mem_total=$(awk '/MemTotal/ {printf "%.0f GB", $2/1024/1024}' /proc/meminfo)
    elif command -v sysctl &>/dev/null; then
        mem_total=$(sysctl -n hw.memsize 2>/dev/null | awk '{printf "%.0f GB", $1/1024/1024/1024}')
    else
        mem_total="unknown"
    fi

    # JDK
    local jdk_version
    jdk_version=$("$java_cmd" -version 2>&1 | head -1 | sed 's/.*"\(.*\)".*/\1/')

    local jdk_vm
    jdk_vm=$("$java_cmd" -version 2>&1 | tail -1)

    # OS
    local os_name
    os_name=$(uname -srm)

    local hostname
    hostname=$(hostname -s 2>/dev/null || echo "unknown")

    cat > "$env_file" <<EOF
{
  "timestamp": "$(date -Iseconds)",
  "hostname": "$hostname",
  "cpu": {
    "model": "$cpu_model",
    "cores": $cpu_cores
  },
  "memory": "$mem_total",
  "jdk": {
    "version": "$jdk_version",
    "vm": "$jdk_vm"
  },
  "os": "$os_name",
  "profile": "$PROFILE",
  "threads": $THREADS,
  "suite": "$SUITE"
}
EOF
    echo "  CPU: $cpu_model ($cpu_cores cores)"
    echo "  MEM: $mem_total"
    echo "  JDK: $jdk_version"
    echo "  OS:  $os_name"
}

# ---- Run benchmark ----
run_suite() {
    local suite_name="$1"
    local include_pattern="$2"
    local java_cmd="$3"
    local result_file="$RESULT_DIR/${suite_name}.json"

    echo ""
    echo "========================================="
    echo " Running $suite_name ($PROFILE, ${THREADS}T)"
    echo "========================================="

    # shellcheck disable=SC2086
    "$java_cmd" -jar target/benchmark3.jar \
        "$include_pattern" \
        -wi $JMH_WARMUP -i $JMH_MEASURE -f $JMH_FORKS -t $THREADS \
        -rf json -rff "$result_file" \
        -jvmArgs "-server" \
        $JMH_EXCLUDE \
        2>&1 | tee "$RESULT_DIR/${suite_name}.log"

    echo ""
    echo "Results saved -> $result_file"
}

# ---- Generate summary ----
generate_summary() {
    local summary_file="$RESULT_DIR/summary.txt"

    {
        echo "========================================="
        echo " Benchmark Summary"
        echo "========================================="
        echo ""
        cat "$RESULT_DIR/env.json"
        echo ""

        if command -v jq &>/dev/null; then
            for json_file in "$RESULT_DIR"/*.json; do
                [[ "$(basename "$json_file")" == "env.json" ]] && continue
                [[ ! -f "$json_file" ]] && continue

                local suite_name
                suite_name=$(basename "$json_file" .json)
                echo ""
                echo "--- $suite_name ---"
                echo ""
                printf "%-50s %12s %8s\n" "Benchmark" "ops/ms" "±error"
                printf "%-50s %12s %8s\n" "──────────────────────────────────────────────────" "────────────" "────────"
                jq -r '.[] | "\(.benchmark | split(".") | .[-2:] | join(".")) \(.primaryMetric.score) \(.primaryMetric.scoreError)"' "$json_file" 2>/dev/null \
                    | while read -r name score error; do
                        printf "%-50s %12.1f %8.1f\n" "$name" "$score" "$error"
                    done
            done
        else
            echo "(install jq for formatted summary)"
        fi
    } > "$summary_file"

    echo ""
    cat "$summary_file"
    echo ""
    echo "Summary saved -> $summary_file"
}

# ---- Run for each JDK ----
run_with_jdk() {
    local jdk_name="$1"
    local java_cmd

    if [[ "$jdk_name" == "default" ]]; then
        java_cmd="java"
        jdk_name="jdk$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/')"
    else
        java_cmd=$(find_java "$jdk_name")
        if [[ -z "$java_cmd" ]]; then
            echo "ERROR: $jdk_name not found"
            return 1
        fi
    fi

    local hostname
    hostname=$(hostname -s 2>/dev/null || echo "unknown")
    DATE=$(date +%Y-%m-%d)
    RESULT_DIR="results/${DATE}-${hostname}-${jdk_name}"
    mkdir -p "$RESULT_DIR"

    echo ""
    echo "╔═══════════════════════════════════════╗"
    echo "  $jdk_name on $hostname"
    echo "╚═══════════════════════════════════════╝"

    capture_env "$RESULT_DIR/env.json" "$java_cmd"

    case "$SUITE" in
        eishay)
            run_suite "eishay" ".*eishay\.Eishay.*" "$java_cmd"
            ;;
        jjb)
            run_suite "jjb" ".*jjb\.(Users|Clients).*" "$java_cmd"
            ;;
        all)
            run_suite "eishay" ".*eishay\.Eishay.*" "$java_cmd"
            run_suite "jjb" ".*jjb\.(Users|Clients).*" "$java_cmd"
            ;;
        *)
            echo "Unknown suite: $SUITE"
            echo "Usage: $0 {eishay|jjb|all} [quick|standard|full] [threads] [jdk21|jdk25|jdk21,jdk25|all|default]"
            exit 1
            ;;
    esac

    generate_summary
}

# ---- Main ----
JDKS=($(resolve_jdks "$JDK_ARG"))

echo "Suites: $SUITE | Profile: $PROFILE | Threads: $THREADS | JDKs: ${JDKS[*]}"

for jdk in "${JDKS[@]}"; do
    run_with_jdk "$jdk"
done

# Show cross-JDK comparison if multiple JDKs
if [[ ${#JDKS[@]} -gt 1 ]] && [[ -f "$SCRIPT_DIR/compare-results.sh" ]]; then
    echo ""
    echo "╔═══════════════════════════════════════╗"
    echo "  Cross-JDK Comparison"
    echo "╚═══════════════════════════════════════╝"
    "$SCRIPT_DIR/compare-results.sh" results/*-"$(hostname -s)"-*
fi
