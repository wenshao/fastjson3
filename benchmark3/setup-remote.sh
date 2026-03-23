#!/usr/bin/env bash
# Initialize a fresh server for running fastjson3 benchmarks.
#
# Usage:
#   ./setup-remote.sh root@172.16.172.143
#
# What it does:
#   1. Copies JDK 21 + JDK 25 from local machine (fast, no public download)
#   2. Installs jq on remote (for result formatting)
#   3. Sets default java symlink
#
# Prerequisites:
#   - Local machine has /root/Install/jdk21 and /root/Install/jdk25
#   - Same CPU architecture (x86_64 or aarch64) on both machines

set -euo pipefail

if [[ $# -eq 0 ]]; then
    echo "Usage: $0 user@host"
    exit 1
fi

HOST="$1"

echo "═══════════════════════════════════════"
echo " Setting up $HOST"
echo "═══════════════════════════════════════"

# Check architectures match
LOCAL_ARCH=$(uname -m)
REMOTE_ARCH=$(ssh "$HOST" 'uname -m')
echo "Local:  $LOCAL_ARCH"
echo "Remote: $REMOTE_ARCH"

if [[ "$LOCAL_ARCH" != "$REMOTE_ARCH" ]]; then
    echo "ERROR: Architecture mismatch ($LOCAL_ARCH vs $REMOTE_ARCH)"
    echo "Cannot copy local JDK to remote. Install JDK manually on remote."
    exit 1
fi

# Install jq
echo ""
echo "[1/4] Installing jq..."
ssh "$HOST" 'command -v jq &>/dev/null && echo "jq already installed" || (apt-get update -qq 2>/dev/null && apt-get install -y -qq jq 2>/dev/null || yum install -y -q jq 2>/dev/null || echo "WARNING: could not install jq")'

# Copy JDKs
for JDK in jdk21 jdk25; do
    LOCAL_DIR="/root/Install/$JDK"
    STEP=$([[ "$JDK" == "jdk21" ]] && echo "2" || echo "3")

    if [[ ! -x "$LOCAL_DIR/bin/java" ]]; then
        echo "[$STEP/4] SKIP: $LOCAL_DIR not found locally"
        continue
    fi

    # Check if already installed on remote
    REMOTE_CHECK=$(ssh "$HOST" "[[ -x /root/Install/$JDK/bin/java ]] && /root/Install/$JDK/bin/java -version 2>&1 | head -1 || echo 'NOT_INSTALLED'")
    if [[ "$REMOTE_CHECK" != "NOT_INSTALLED" ]]; then
        echo "[$STEP/4] $JDK already installed: $REMOTE_CHECK"
        continue
    fi

    LOCAL_SIZE=$(du -sh "$LOCAL_DIR" | cut -f1)
    echo "[$STEP/4] Copying $JDK ($LOCAL_SIZE)..."
    tar -C /root/Install -cf - "$JDK" | ssh "$HOST" 'mkdir -p /root/Install && tar -C /root/Install -xf -'
    REMOTE_VER=$(ssh "$HOST" "/root/Install/$JDK/bin/java -version 2>&1 | head -1")
    echo "  Installed: $REMOTE_VER"
done

# Set default java
echo "[4/4] Setting default java..."
ssh "$HOST" '
if [[ ! -x /usr/local/bin/java ]]; then
    for jdk in jdk25 jdk21; do
        if [[ -x "/root/Install/$jdk/bin/java" ]]; then
            ln -sf "/root/Install/$jdk/bin/java" /usr/local/bin/java
            echo "  Default: $jdk"
            break
        fi
    done
fi
echo ""
echo "Available JDKs:"
for d in /root/Install/jdk*; do
    [[ -x "$d/bin/java" ]] && echo "  $(basename $d): $($d/bin/java -version 2>&1 | head -1)"
done
'

echo ""
echo "═══════════════════════════════════════"
echo " Setup complete! Now run:"
echo ""
echo "  ./run-remote.sh $HOST eishay quick 16 jdk21,jdk25"
echo "═══════════════════════════════════════"
