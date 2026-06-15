#!/usr/bin/env bash
# deploy.sh — Build and deploy PipTally to the iOS Simulator
# Usage:
#   ./deploy.sh                   # Auto-picks the booted simulator (preserves app data)
#   ./deploy.sh "iPhone 16"       # Target a specific simulator by name
#   ./deploy.sh --clean           # Wipe app data before installing (fresh start)
#   ./deploy.sh --list            # List available simulators

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$SCRIPT_DIR/Boneyard.xcodeproj"
SCHEME="Boneyard"
BUNDLE_ID="app.boneyard"
BUILD_DIR="$SCRIPT_DIR/.build/simulator"

# ── Helpers ──────────────────────────────────────────────────────────────────

bold()  { printf '\033[1m%s\033[0m\n' "$*"; }
green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$*"; }
dim()   { printf '\033[2m%s\033[0m\n' "$*"; }
step()  { printf '\n\033[1;36m▶ %s\033[0m\n' "$*"; }

# ── Simulator resolution ──────────────────────────────────────────────────────

list_simulators() {
    bold "Available iOS Simulators:"
    xcrun simctl list devices available | grep -E "iPhone|iPad" | sed 's/^/  /'
}

get_booted_udid() {
    xcrun simctl list devices available | grep Booted | awk -F '[()]' '{print $2}' | head -1
}

get_udid_by_name() {
    xcrun simctl list devices available | grep "$1" | awk -F '[()]' '{print $2}' | head -1
}

# ── Argument handling ─────────────────────────────────────────────────────────

CLEAN_INSTALL=0

for arg in "$@"; do
    case "$arg" in
        --clean) CLEAN_INSTALL=1; shift ;;
        --list)  list_simulators; exit 0 ;;
    esac
done
export CLEAN_INSTALL

if [[ -n "${1:-}" ]]; then
    SIM_NAME="$1"
    SIM_UDID="$(get_udid_by_name "$SIM_NAME")"
    if [[ -z "$SIM_UDID" ]]; then
        red "❌ No simulator found matching: $SIM_NAME"
        list_simulators
        exit 1
    fi
else
    SIM_UDID="$(get_booted_udid)"
    if [[ -z "$SIM_UDID" ]]; then
        red "❌ No simulator is currently booted."
        dim "Boot one from Xcode or run: xcrun simctl boot \"iPhone 17 Pro\""
        list_simulators
        exit 1
    fi
    SIM_NAME="$(xcrun simctl list devices available | grep "$SIM_UDID" | sed 's/ (.*//' | xargs)"
fi

bold "🎯 Target: $SIM_NAME ($SIM_UDID)"

# ── Boot simulator if needed ──────────────────────────────────────────────────

SIM_STATE="$(xcrun simctl list devices | grep "$SIM_UDID" | grep -o 'Booted\|Shutdown' || echo 'Unknown')"
if [[ "$SIM_STATE" != "Booted" ]]; then
    step "Booting simulator..."
    xcrun simctl boot "$SIM_UDID"
    sleep 2
fi

# Open Simulator.app so the device is visible
open -a Simulator --args -CurrentDeviceUDID "$SIM_UDID" 2>/dev/null || true

# ── Build ─────────────────────────────────────────────────────────────────────

step "Building $SCHEME..."
dim "  Project : $PROJECT"
dim "  Output  : $BUILD_DIR"

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

# ── Locate .app bundle ────────────────────────────────────────────────────────

APP_PATH="$(find "$BUILD_DIR" -name "${SCHEME}.app" -path "*/Debug-iphonesimulator/*" | head -1)"
if [[ -z "$APP_PATH" ]]; then
    red "❌ Could not locate the built .app bundle."
    exit 1
fi
dim "  App bundle: $APP_PATH"

# ── Terminate existing instance ───────────────────────────────────────────────

step "Terminating any running instance..."
xcrun simctl terminate "$SIM_UDID" "$BUNDLE_ID" 2>/dev/null || true

# ── Install (preserving user data) ────────────────────────────────────────────

step "Installing app..."
if [[ "${CLEAN_INSTALL:-}" == "1" ]]; then
    dim "  --clean flag set: removing existing app data"
    xcrun simctl uninstall "$SIM_UDID" "$BUNDLE_ID" 2>/dev/null || true
    xcrun simctl install "$SIM_UDID" "$APP_PATH"
elif ! xcrun simctl install "$SIM_UDID" "$APP_PATH" 2>/dev/null; then
    dim "  In-place install failed, falling back to clean install..."
    xcrun simctl uninstall "$SIM_UDID" "$BUNDLE_ID" 2>/dev/null || true
    xcrun simctl install "$SIM_UDID" "$APP_PATH"
fi

# ── Launch ────────────────────────────────────────────────────────────────────

step "Launching $SCHEME..."
xcrun simctl launch "$SIM_UDID" "$BUNDLE_ID"

green "\n✅ Deployed successfully to $SIM_NAME"
