#!/bin/bash
cd "$(dirname "$0")"
echo "Compiling PhotoBrowser.java..."
javac PhotoBrowser.java
if [ $? -eq 0 ]; then
    echo "Build successful. Run with: java PhotoBrowser"
else
    echo "Build failed."
    exit 1
fi
