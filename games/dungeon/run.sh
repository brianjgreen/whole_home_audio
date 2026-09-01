#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
"$SCRIPT_DIR/build.sh" && java -cp "$SCRIPT_DIR/out" dungeon.Main
