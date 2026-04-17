#!/usr/bin/env bash
# Run this on the REMOTE machine to establish a reverse SSH tunnel.
# Usage: ./setup-tunnel.sh <port> <this-machine-ip>
#
# Examples:
#   ./setup-tunnel.sh 2231 10.0.0.100    # ARM machine #3
#   ./setup-tunnel.sh 2232 10.0.0.100    # ARM machine #4
#   ./setup-tunnel.sh 2233 10.0.0.100    # RISC-V machine
#
# This creates a persistent tunnel: the build machine can reach this
# machine via  ssh -p <port> root@localhost

set -euo pipefail

PORT="${1:?Usage: $0 <port> <build-machine-ip>}"
BUILD_HOST="${2:?Usage: $0 <port> <build-machine-ip>}"

echo "Establishing reverse tunnel: build-machine:${PORT} -> this-machine:22"
echo "Press Ctrl-C to stop."

# -N = no remote command, -R = reverse tunnel, -o = keepalive
exec ssh -N \
    -R ${PORT}:localhost:22 \
    -o ServerAliveInterval=30 \
    -o ServerAliveCountMax=3 \
    -o ExitOnForwardFailure=yes \
    root@${BUILD_HOST}
