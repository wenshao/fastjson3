#!/usr/bin/env bash
#
# Cross-platform EishayParseString benchmark runner.
#
# Builds benchmark3.jar locally, SCPs it to a list of remote hosts, runs
# JMH in parallel on all of them, collects stdout + the JMH JSON result
# file, and prints a side-by-side table comparing fj2 / fj3 REFLECT /
# fj3_asm / wast throughput + the fj3_asm/fj2 ratio.
#
# Usage:
#   scripts/bench-eishay-cross-platform.sh [-f FORKS] [-i ITERS] [-p PATTERN] \
#       user@host1 [user@host2 ...]
#
# Defaults: -f 5 -i 5 -p 'EishayParseString\.(fastjson2|fastjson3|fastjson3_asm|wast|image_fj3_asm|media_fj3_asm)'
#
# Examples:
#   scripts/bench-eishay-cross-platform.sh root@172.16.1.231 root@172.16.172.143
#   scripts/bench-eishay-cross-platform.sh -f 3 -i 3 root@arm-v2 root@x64-bench
#
# Requirements:
#   - Local: mvn, scp
#   - Remote: java 21+ with jdk.incubator.vector, passwordless SSH
#
# The script does NOT require git / mvn on the remote — it ships a
# pre-built jar. Each remote just needs a JVM.
#

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FORKS=5
ITERATIONS=5
WARMUP_ITERATIONS=3
WARMUP_TIME=2
MEAS_TIME=2
PATTERN='EishayParseString\.(fastjson2|fastjson3|fastjson3_asm|wast|image_fj3_asm|media_fj3_asm)'

usage() {
    sed -n '2,30p' "$0" >&2
    exit 1
}

while getopts "f:i:p:h" opt; do
    case $opt in
        f) FORKS=$OPTARG ;;
        i) ITERATIONS=$OPTARG ;;
        p) PATTERN=$OPTARG ;;
        h|*) usage ;;
    esac
done
shift $((OPTIND - 1))

if [ $# -lt 1 ]; then
    echo "Error: at least one host required" >&2
    usage
fi

HOSTS=("$@")
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="/tmp/fj3-bench-$TIMESTAMP"
mkdir -p "$OUT_DIR"

echo "=== Build ==="
cd "$REPO_ROOT"
mvn -q install -pl core3 -am -DskipTests
# benchmark3 is not in the root reactor — build it from its own directory.
(cd "$REPO_ROOT/benchmark3" && mvn -q package -DskipTests)
JAR="$REPO_ROOT/benchmark3/target/benchmark3.jar"
if [ ! -f "$JAR" ]; then
    echo "Error: $JAR not found after build" >&2
    exit 2
fi
JAR_MD5="$(md5sum "$JAR" | awk '{print $1}')"
echo "jar md5: $JAR_MD5"
echo

echo "=== Distribute jar to ${#HOSTS[@]} host(s) ==="
for HOST in "${HOSTS[@]}"; do
    echo "→ $HOST"
    scp -q -o StrictHostKeyChecking=accept-new "$JAR" "$HOST:/tmp/benchmark3-xplat.jar"
done
echo

echo "=== Launch bench runs in parallel ==="
PIDS=()
for HOST in "${HOSTS[@]}"; do
    HOST_SAFE="${HOST//[^a-zA-Z0-9]/_}"
    LOG="$OUT_DIR/$HOST_SAFE.log"
    JSON_REMOTE="/tmp/bench-xplat-$HOST_SAFE.json"

    # Capture arch + JDK into the log header, then run JMH.
    (
        ssh -o StrictHostKeyChecking=accept-new "$HOST" "
            echo '### Host: $HOST'
            echo '### arch: '\$(uname -m)
            echo '### kernel: '\$(uname -r)
            java -version 2>&1 | head -3 | sed 's/^/### /'
            echo '### jar md5: '\$(md5sum /tmp/benchmark3-xplat.jar | awk '{print \$1}')
            echo '### cmd: java -XX:-UseCompressedOops --add-modules jdk.incubator.vector -jar benchmark3.jar $PATTERN -f $FORKS -wi $WARMUP_ITERATIONS -i $ITERATIONS -w $WARMUP_TIME -r $MEAS_TIME'
            echo
            java -XX:-UseCompressedOops --add-modules jdk.incubator.vector \
                -jar /tmp/benchmark3-xplat.jar '$PATTERN' \
                -f $FORKS -wi $WARMUP_ITERATIONS -i $ITERATIONS \
                -w $WARMUP_TIME -r $MEAS_TIME \
                -rf json -rff $JSON_REMOTE
        " > "$LOG" 2>&1 &
        wait $!
        # Pull the JSON back for later diffs
        scp -q "$HOST:$JSON_REMOTE" "$OUT_DIR/$HOST_SAFE.json" 2>/dev/null || true
    ) &
    PIDS+=($!)
    echo "→ $HOST [pid $!] log=$LOG"
done

echo
echo "=== Waiting for ${#PIDS[@]} host(s) to finish ==="
FAILED=0
for i in "${!PIDS[@]}"; do
    PID=${PIDS[$i]}
    HOST=${HOSTS[$i]}
    if wait $PID; then
        echo "✓ $HOST"
    else
        echo "✗ $HOST (exit $?)"
        FAILED=$((FAILED + 1))
    fi
done
echo

if [ $FAILED -gt 0 ]; then
    echo "WARNING: $FAILED host(s) failed. Inspect logs under $OUT_DIR/" >&2
fi

echo "=== Summary ==="
printf '%-30s %-10s %15s %15s %15s %15s %10s %10s\n' \
    "host" "arch" "fj2" "fj3 REFLECT" "fj3_asm" "wast" "asm/fj2" "asm/wast"
printf '%-30s %-10s %15s %15s %15s %15s %10s %10s\n' \
    "------------------------------" "----------" \
    "---------------" "---------------" "---------------" "---------------" \
    "----------" "----------"

for HOST in "${HOSTS[@]}"; do
    HOST_SAFE="${HOST//[^a-zA-Z0-9]/_}"
    LOG="$OUT_DIR/$HOST_SAFE.log"

    if [ ! -f "$LOG" ]; then
        printf '%-30s %-10s %15s\n' "$HOST" "-" "(no log)"
        continue
    fi

    ARCH=$(grep -E '^### arch:' "$LOG" | awk '{print $3}' || echo '?')

    # Parse the JMH table at the end of the log.
    # JMH row format: <benchmark> <mode> <cnt> <score> ± <error> <units>
    # Columns separated by runs of whitespace → $4 is the Score.
    FJ2=$(grep 'EishayParseString.fastjson2 ' "$LOG" | awk '{print $4}' | tail -1)
    FJ3=$(grep 'EishayParseString.fastjson3 ' "$LOG" | awk '{print $4}' | tail -1)
    FJ3_ASM=$(grep 'EishayParseString.fastjson3_asm ' "$LOG" | awk '{print $4}' | tail -1)
    WAST=$(grep 'EishayParseString.wast ' "$LOG" | awk '{print $4}' | tail -1)

    if [ -z "$FJ2" ] || [ -z "$FJ3_ASM" ]; then
        printf '%-30s %-10s %15s\n' "$HOST" "$ARCH" "(parse error)"
        continue
    fi

    # JMH emits numbers like "3536187.476" — strip the commas if any, keep
    # 0 decimals. Compute ratios via awk for portable floating point.
    RATIO_ASM_FJ2=$(awk -v a="$FJ3_ASM" -v b="$FJ2" 'BEGIN { if (b+0 == 0) print "-"; else printf "%.2f%%", 100.0 * a / b }')
    RATIO_ASM_WAST=$(awk -v a="$FJ3_ASM" -v b="$WAST" 'BEGIN { if (b+0 == 0) print "-"; else printf "%.2f%%", 100.0 * a / b }')

    # Round scores for display (one decimal place as ops/s, no commas).
    FJ2_F=$(awk -v v="$FJ2" 'BEGIN { printf "%.0f", v }')
    FJ3_F=$(awk -v v="${FJ3:-0}" 'BEGIN { printf "%.0f", v }')
    ASM_F=$(awk -v v="$FJ3_ASM" 'BEGIN { printf "%.0f", v }')
    WAST_F=$(awk -v v="${WAST:-0}" 'BEGIN { printf "%.0f", v }')

    printf '%-30s %-10s %15s %15s %15s %15s %10s %10s\n' \
        "$HOST" "$ARCH" "$FJ2_F" "$FJ3_F" "$ASM_F" "$WAST_F" \
        "$RATIO_ASM_FJ2" "$RATIO_ASM_WAST"
done

echo
echo "Full logs + JMH JSON results: $OUT_DIR/"
echo "To archive: tar -C /tmp -czf fj3-bench-$TIMESTAMP.tar.gz fj3-bench-$TIMESTAMP"
