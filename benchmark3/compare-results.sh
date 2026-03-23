#!/usr/bin/env bash
# Compare benchmark results across different machines/dates.
#
# Usage:
#   ./compare-results.sh                                    # compare all in results/
#   ./compare-results.sh results/2026-03-20-server1 results/2026-03-21-laptop
#
# Requires: jq

set -euo pipefail

if ! command -v jq &>/dev/null; then
    echo "Error: jq is required. Install with: apt install jq / brew install jq"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Find result directories
if [[ $# -gt 0 ]]; then
    DIRS=("$@")
else
    DIRS=(results/*/)
fi

if [[ ${#DIRS[@]} -eq 0 ]]; then
    echo "No result directories found."
    exit 0
fi

echo "========================================="
echo " Cross-Machine Benchmark Comparison"
echo "========================================="
echo ""

# Print machine info header
printf "%-20s" "Machine"
for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    label=$(basename "$dir")
    printf " %20s" "$label"
done
echo ""

printf "%-20s" "CPU"
for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    if [[ -f "$dir/env.json" ]]; then
        cpu=$(jq -r '.cpu.model' "$dir/env.json" 2>/dev/null | cut -c1-20)
        printf " %20s" "$cpu"
    else
        printf " %20s" "?"
    fi
done
echo ""

printf "%-20s" "Cores"
for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    if [[ -f "$dir/env.json" ]]; then
        cores=$(jq -r '.cpu.cores' "$dir/env.json" 2>/dev/null)
        printf " %20s" "$cores"
    else
        printf " %20s" "?"
    fi
done
echo ""

printf "%-20s" "JDK"
for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    if [[ -f "$dir/env.json" ]]; then
        jdk=$(jq -r '.jdk.version' "$dir/env.json" 2>/dev/null | cut -c1-20)
        printf " %20s" "$jdk"
    else
        printf " %20s" "?"
    fi
done
echo ""
echo ""

# Collect all benchmark names across all result dirs
declare -A BENCHMARKS

for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    for json_file in "$dir"/*.json; do
        [[ "$(basename "$json_file")" == "env.json" ]] && continue
        [[ ! -f "$json_file" ]] && continue

        while IFS= read -r name; do
            BENCHMARKS["$name"]=1
        done < <(jq -r '.[].benchmark' "$json_file" 2>/dev/null)
    done
done

# Sort benchmark names
IFS=$'\n' SORTED_NAMES=($(sort <<<"${!BENCHMARKS[*]}")); unset IFS

# Print comparison table
printf "\n%-50s" "Benchmark (ops/ms)"
for dir in "${DIRS[@]}"; do
    dir="${dir%/}"
    label=$(basename "$dir")
    printf " %20s" "$label"
done
echo ""
printf "%-50s" "$(printf '%0.s─' {1..50})"
for dir in "${DIRS[@]}"; do
    printf " %20s" "────────────────────"
done
echo ""

for name in "${SORTED_NAMES[@]}"; do
    short_name=$(echo "$name" | sed 's/com\.alibaba\.fastjson3\.benchmark\.//')
    printf "%-50s" "$short_name"

    for dir in "${DIRS[@]}"; do
        dir="${dir%/}"
        score=""
        for json_file in "$dir"/*.json; do
            [[ "$(basename "$json_file")" == "env.json" ]] && continue
            [[ ! -f "$json_file" ]] && continue

            found=$(jq -r --arg name "$name" '.[] | select(.benchmark == $name) | .primaryMetric.score' "$json_file" 2>/dev/null)
            if [[ -n "$found" && "$found" != "null" ]]; then
                score="$found"
                break
            fi
        done

        if [[ -n "$score" ]]; then
            printf " %20.1f" "$score"
        else
            printf " %20s" "-"
        fi
    done
    echo ""
done

echo ""
echo "Higher is better (ops/ms = operations per millisecond)"
