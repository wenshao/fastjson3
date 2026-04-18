#!/bin/bash
# Build and run the fastjson3 native-image smoke test.
#
# Verifies:
#   - reachability-metadata.json schema is accepted by current GraalVM
#   - NATIVE_IMAGE flag is detected at runtime
#   - Parse + write + list parsing work end-to-end in a native binary
#
# Requires `native-image` on PATH (ships with GraalVM distributions).
# Example: `export PATH=/path/to/graalvm/bin:$PATH`.
#
# Usage:
#   ./scripts/test-native-image.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$REPO_ROOT/core3/target/native-image-test"

if ! command -v native-image >/dev/null 2>&1; then
    echo "error: native-image not found on PATH." >&2
    echo "  install GraalVM and add its bin/ to PATH, then re-run." >&2
    exit 1
fi

native-image --version | head -1

echo "==> Building core3 jar"
(cd "$REPO_ROOT" && mvn -pl core3 -am install -DskipTests -q)

echo "==> Assembling test classpath in $BUILD_DIR"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/run/com/alibaba/fastjson3"
mkdir -p "$BUILD_DIR/user-meta/META-INF/native-image/fastjson3-ni-test"
# Find the main jar (exclude android / sources / javadoc variants)
FJ3_JAR=""
for candidate in "$REPO_ROOT"/core3/target/fastjson3-*.jar; do
    case "$candidate" in
        *-android.jar|*-sources.jar|*-javadoc.jar) continue ;;
    esac
    FJ3_JAR="$candidate"
    break
done
if [ -z "$FJ3_JAR" ]; then
    echo "error: cannot locate fastjson3 main jar under $REPO_ROOT/core3/target/" >&2
    exit 1
fi
cp "$FJ3_JAR" "$BUILD_DIR/fj3.jar"
# Make sure test classes are up to date
(cd "$REPO_ROOT" && mvn -pl core3 test-compile -q)
cp "$REPO_ROOT"/core3/target/test-classes/com/alibaba/fastjson3/NativeImageTest*.class \
   "$BUILD_DIR/run/com/alibaba/fastjson3/"

# User-supplied reachability metadata for the POJO the test exercises.
# This lives OUTSIDE the library's own META-INF/native-image so it doesn't
# leak test types into the published jar — the library only registers its
# own reflection hooks (annotations, String/Unsafe fast paths). Real users
# bundle their own reachability-metadata.json alongside their code, OR use
# the `native-image-agent` to record usage at run time.
cat > "$BUILD_DIR/user-meta/META-INF/native-image/fastjson3-ni-test/reachability-metadata.json" <<'JSON'
{
  "reflection": [
    {
      "type": "com.alibaba.fastjson3.NativeImageTest$User",
      "unsafeAllocated": true,
      "allDeclaredFields": true,
      "methods": [
        { "name": "<init>", "parameterTypes": [] }
      ]
    }
  ]
}
JSON

echo "==> Compiling native image"
(cd "$BUILD_DIR" && native-image \
    --no-fallback \
    -cp "fj3.jar:run:user-meta" \
    -o native-test \
    com.alibaba.fastjson3.NativeImageTest)

echo "==> Running native binary"
(cd "$BUILD_DIR" && ./native-test)

echo "==> Startup comparison (3 runs each)"
time_cmd() {
    # Time a command silently using the shell's built-in $SECONDS / EPOCHREALTIME.
    local label="$1"; shift
    local t0 t1
    t0=$EPOCHREALTIME
    "$@" >/dev/null 2>&1
    t1=$EPOCHREALTIME
    # EPOCHREALTIME is seconds with microsecond fraction.
    awk -v t0="$t0" -v t1="$t1" -v label="$label" \
        'BEGIN { printf "  %-6s %.3fs\n", label, t1-t0 }'
}
echo "--- JVM cold start"
for _ in 1 2 3; do
    time_cmd "jvm" java -cp "$BUILD_DIR/fj3.jar:$BUILD_DIR/run" com.alibaba.fastjson3.NativeImageTest
done
echo "--- Native binary"
for _ in 1 2 3; do
    time_cmd "native" "$BUILD_DIR/native-test"
done

echo
echo "native-image smoke test passed: $BUILD_DIR/native-test"
