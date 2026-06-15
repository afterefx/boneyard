#!/usr/bin/env bash
# run_functional_tests.sh — Run functional theme tests inside the iOS Simulator

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$SCRIPT_DIR/Boneyard.xcodeproj"
SCHEME="Boneyard"
BUNDLE_ID="app.boneyard"
BUILD_DIR="$SCRIPT_DIR/.build/simulator"

# Colors for output
green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$*"; }
bold()  { printf '\033[1m%s\033[0m\n' "$*"; }
dim()   { printf '\033[2m%s\033[0m\n' "$*"; }
step()  { printf '\n\033[1;36m▶ %s\033[0m\n' "$*"; }

# 1. Resolve Booted Simulator
step "Resolving booted simulator..."
SIM_UDID=$(xcrun simctl list devices available | grep Booted | awk -F '[()]' '{print $2}' | head -1)

if [[ -z "$SIM_UDID" ]]; then
    red "❌ No booted simulator found. Please boot a simulator first (e.g. running deploy.sh or starting Simulator.app)."
    exit 1
fi

SIM_NAME=$(xcrun simctl list devices available | grep "$SIM_UDID" | sed 's/ (.*//' | xargs)
bold "🎯 Target: $SIM_NAME ($SIM_UDID)"

# 2. Build application
step "Building PipTally app..."
xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration Debug \
    -destination "platform=iOS Simulator,id=$SIM_UDID" \
    -derivedDataPath "$BUILD_DIR" \
    build \
    | xcpretty 2>/dev/null || \
xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration Debug \
    -destination "platform=iOS Simulator,id=$SIM_UDID" \
    -derivedDataPath "$BUILD_DIR" \
    build \
    2>&1 | grep -E "(error:|warning:|BUILD SUCCEEDED|BUILD FAILED)" | grep -v warning

APP_PATH="$(find "$BUILD_DIR" -name "${SCHEME}.app" -path "*/Debug-iphonesimulator/*" | head -1)"
if [[ -z "$APP_PATH" ]]; then
    red "❌ Could not locate the built .app bundle."
    exit 1
fi
dim "  App bundle: $APP_PATH"

# 3. Terminate running instance & Clean up old results
step "Preparing simulator state..."
xcrun simctl terminate "$SIM_UDID" "$BUNDLE_ID" 2>/dev/null || true

# Get data container path to clear old results
DATA_DIR=""
if DATA_DIR=$(xcrun simctl get_app_container "$SIM_UDID" "$BUNDLE_ID" data 2>/dev/null); then
    RESULTS_FILE="$DATA_DIR/Documents/theme_test_results.json"
    if [[ -f "$RESULTS_FILE" ]]; then
        dim "  Clearing old test results at $RESULTS_FILE"
        rm -f "$RESULTS_FILE"
    fi
fi

# 4. Install App
step "Installing app on simulator..."
xcrun simctl install "$SIM_UDID" "$APP_PATH"

# Get data container path (it will definitely exist now after install)
DATA_DIR=$(xcrun simctl get_app_container "$SIM_UDID" "$BUNDLE_ID" data)
RESULTS_FILE="$DATA_DIR/Documents/theme_test_results.json"

# 5. Launch App with Test Argument
step "Launching app with functional test mode..."
xcrun simctl launch "$SIM_UDID" "$BUNDLE_ID" -runThemeTests

# 6. Poll for results file
step "Waiting for functional tests to run on simulator..."
POLL_INTERVAL=1
TIMEOUT=30
ELAPSED=0

while [[ ! -f "$RESULTS_FILE" ]]; do
    if [[ $ELAPSED -ge $TIMEOUT ]]; then
        red "❌ Timeout waiting for functional test results ($TIMEOUT seconds)."
        exit 1
    fi
    sleep $POLL_INTERVAL
    ELAPSED=$((ELAPSED + POLL_INTERVAL))
    printf "."
done
printf "\n"

# 7. Parse and report results
step "Reading functional test results..."
cat "$RESULTS_FILE"
printf "\n\n"

STATUS=$(python3 -c "import json; print(json.load(open('$RESULTS_FILE'))['status'])")
MESSAGE=$(python3 -c "import json; print(json.load(open('$RESULTS_FILE'))['message'])")

# Cleanup results file
rm -f "$RESULTS_FILE"

if [[ "$STATUS" == "SUCCESS" ]]; then
    green "✅ FUNCTIONAL TESTS PASSED: $MESSAGE"
    exit 0
else
    red "❌ FUNCTIONAL TESTS FAILED: $MESSAGE"
    exit 1
fi
