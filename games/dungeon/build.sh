#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$SCRIPT_DIR/out"
javac -d "$SCRIPT_DIR/out" "$SCRIPT_DIR/src/dungeon/"*.java
echo "Build successful."
