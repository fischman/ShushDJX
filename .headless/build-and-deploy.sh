#!/usr/bin/env bash
#
# Build APK and optionally deploy to a connected device.
#
# Flags:
#   --build-only - Don't deploy.
#   --release - Build release instead of debug APK.

set -euo pipefail

cd "$(dirname "$0")/.."

PACKAGE="org.fischman.shushdjx"

BUILD_ONLY=false
RELEASE=false
for arg in "$@"; do
  case "$arg" in
      --build-only) BUILD_ONLY=true ;;
      --release) RELEASE=true ;;
      *) echo "Unknown flag $arg" >&2 ; exit 1 ;;
  esac
done

TASK="assembleDebug"
APK="app/build/outputs/apk/debug/app-debug.apk"
if $RELEASE; then
    TASK="assembleRelease"
    APK="$(echo "$APK" | sed -e 's/debug/release/g')"
fi

echo "==> Building $TASK..."
./gradlew --warning-mode all "$TASK"

echo "==> APK: $APK ($(du -h "$APK" | cut -f1))"

if $BUILD_ONLY; then
  exit 0
fi

adb devices | grep -q 'device$' || { echo "ERROR: No ADB device connected." >&2; exit 1; }

echo -n "==> Installing... "
if adb install "$APK"; then
    echo
else
    echo "failed; uninstalling first..."
    adb uninstall "$PACKAGE" 2>/dev/null || true
    adb install "$APK"
fi

echo "==> Launching..."
adb shell am start -n "${PACKAGE}/${PACKAGE}.MainActivity"

echo "==> Done."
