#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/com/jeweled/*.java
exec java -cp out com.jeweled.Game
