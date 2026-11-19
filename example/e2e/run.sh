#!/usr/bin/env bash
# Runs the Maestro end-to-end flows against the example app.
#
# Usage: example/e2e/run.sh android|ios [extra maestro test options]
#
# Needs: the example app (debug build) installed on one booted emulator or simulator,
# Metro running (`npm start` in example/), the Maestro CLI, and python3 (serves the
# "remote" test image locally, so the tests don't depend on the internet).
set -euo pipefail

PLATFORM="${1:?usage: run.sh android|ios [maestro options]}"
shift
DIR="$(cd "$(dirname "$0")" && pwd)"
FIXTURE="$DIR/fixtures/local-test.jpg"
IMAGE_SERVER_PORT=8090
# Maestro's iOS driver can take a few minutes to start on a cold simulator (always the case in CI).
export MAESTRO_DRIVER_STARTUP_TIMEOUT="${MAESTRO_DRIVER_STARTUP_TIMEOUT:-300000}"

case "$PLATFORM" in
  android)
    APP_ID=com.shareimageexample
    # Put the fixture in the app's own files folder (run-as works because it's a debug build).
    adb push "$FIXTURE" /data/local/tmp/local-test.jpg >/dev/null
    adb shell "run-as $APP_ID sh -c 'mkdir -p files && cat /data/local/tmp/local-test.jpg > files/local-test.jpg'"
    adb shell rm /data/local/tmp/local-test.jpg
    # Debug builds load JavaScript from Metro on the host.
    adb reverse tcp:8081 tcp:8081 >/dev/null
    # Lets the emulator reach the local image server as localhost.
    adb reverse tcp:$IMAGE_SERVER_PORT tcp:$IMAGE_SERVER_PORT >/dev/null
    LOCAL_IMAGE_URI="file:///data/user/0/$APP_ID/files/local-test.jpg"
    ;;
  ios)
    APP_ID=org.reactjs.native.example.ShareImageExample
    DATA_DIR="$(xcrun simctl get_app_container booted "$APP_ID" data)"
    mkdir -p "$DATA_DIR/Documents"
    cp "$FIXTURE" "$DATA_DIR/Documents/local-test.jpg"
    LOCAL_IMAGE_URI="file://$DATA_DIR/Documents/local-test.jpg"
    ;;
  *)
    echo "Unknown platform: $PLATFORM (expected android or ios)" >&2
    exit 1
    ;;
esac

# Build the JavaScript bundle once before any flow runs. On a fresh machine (always in CI)
# Metro's first build takes minutes, which would otherwise count against the first flow.
METRO_URL="http://localhost:8081"
for _ in $(seq 1 120); do
  curl -fs "$METRO_URL/status" | grep -q "packager-status:running" && break
  sleep 2
done
echo "Building the JavaScript bundle for $PLATFORM..."
curl -fs --max-time 900 -o /dev/null \
  "$METRO_URL/index.bundle?platform=$PLATFORM&dev=true&minify=false" \
  || { echo "Metro isn't running on $METRO_URL or failed to build the bundle" >&2; exit 1; }

# Serve the fixtures over HTTP for the remote-image flow; stopped when the script exits.
python3 -m http.server "$IMAGE_SERVER_PORT" --bind 127.0.0.1 --directory "$DIR/fixtures" >/dev/null 2>&1 &
IMAGE_SERVER_PID=$!
trap 'kill $IMAGE_SERVER_PID 2>/dev/null || true' EXIT

share_link() {
  printf 'shareimageexample://share-image?uri=%s' \
    "$(node -e 'process.stdout.write(encodeURIComponent(process.argv[1]))' "$1")"
}

maestro --platform "$PLATFORM" test \
  --env APP_ID="$APP_ID" \
  --env LOCAL_IMAGE_LINK="$(share_link "$LOCAL_IMAGE_URI")" \
  --env REMOTE_IMAGE_LINK="$(share_link "http://localhost:$IMAGE_SERVER_PORT/remote-test.png")" \
  "$@" \
  "$DIR/flows"
