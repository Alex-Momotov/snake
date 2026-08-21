#!/bin/bash
# Build, then exercise the game headlessly. Run this after every change.
set -e
cd "$(dirname "$0")/.."
mvn -q -o package -DskipTests

echo "=== 1. autoplay soak: snake should grow, score and survive ==="
python3 tools/play.py --mode auto --rows=16 --cols=22 --run-ms 15000 --step-ms 70 \
    -- --food_interval_ms=900 --seed=2

echo
echo "=== 2. wall collision: driving into the wall should end the game ==="
python3 tools/play.py --mode script --rows=12 --cols=16 --run-ms 4000 \
    --script "3000:snap" -- --food_interval_ms=100000 --seed=7

echo
echo "=== 3. later levels still lay out and spawn correctly ==="
python3 tools/play.py --mode script --rows=16 --cols=22 --run-ms 900 --script "700:snap" \
    -- --seed=4 --start_level=6 --step_ms=200 --food_interval_ms=100000
