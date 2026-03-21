#!/bin/bash
# Quick benchmark script for git bisect
# Returns 0 for good (performance OK), 1 for bad (performance regression)
# Usage: ./run_jjb_benchmark.sh <baseline_user_score> <baseline_client_score>

set -e

# Build core3 and benchmark3
mvn -q clean package -DskipTests -pl core3,benchmark3 -am -q

# Get classpath
CP="core3/target/classes:benchmark3/target/classes:benchmark3/target/test-classes"
CP="$CP:$(mvn -q dependency:build-classpath -pl core3 -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout 2>/dev/null)"
CP="$CP:$(mvn -q dependency:build-classpath -f benchmark3/pom.xml -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout 2>/dev/null)"

# Run JMH with quick config and extract scores
OUTPUT=$(java -cp "$CP" -Xms2g -Xmx2g org.openjdk.jmh.Main \
    -wi 1 -i 1 -f 1 \
    com.alibaba.fastjson3.benchmark.jjb.JJBQuickBenchmark 2>&1)

USER_SCORE=$(echo "$OUTPUT" | grep "JJBQuickBenchmark.parseUser_fastjson3" | grep "thrpt" | awk '{print $3}')
CLIENT_SCORE=$(echo "$OUTPUT" | grep "JJBQuickBenchmark.parseClient_fastjson3" | grep "thrpt" | awk '{print $3}')

echo "Current: parseUser=$USER_SCORE, parseClient=$CLIENT_SCORE"

# If baseline provided, check for regression (more than 2% slowdown)
if [ -n "$1" ] && [ -n "$2" ]; then
    BASE_USER=$1
    BASE_CLIENT=$2

    # Check if current is more than 2% slower than baseline using Python
    CHANGE=$(python3 -c "
user_curr = $USER_SCORE
user_base = $BASE_USER
client_curr = $CLIENT_SCORE
client_base = $BASE_CLIENT
user_pct = (user_curr - user_base) * 100 / user_base
client_pct = (client_curr - client_base) * 100 / client_base
print(f'{user_pct:.1f},{client_pct:.1f}')
")

    USER_PCT=$(echo "$CHANGE" | cut -d',' -f1)
    CLIENT_PCT=$(echo "$CHANGE" | cut -d',' -f2)

    echo "User change: $USER_PCT%, Client change: $CLIENT_PCT%"

    # Consider bad if either is more than 2% slower
    if python3 -c "exit(0 if float('$USER_PCT') >= -2.0 and float('$CLIENT_PCT') >= -2.0 else 1)"; then
        echo "=> GOOD (performance OK)"
        exit 0
    else
        echo "=> BAD (performance regression)"
        exit 1
    fi
fi

# No baseline, just output scores
exit 0
