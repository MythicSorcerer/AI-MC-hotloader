#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

STUB_CURL="$TMP_DIR/curl"
cat > "$STUB_CURL" <<'EOF'
#!/usr/bin/env bash
if [[ "$*" == *"/files/upload"* ]]; then
  echo '{"attributes":{"url":"http://upload.local"}}'
  exit 0
fi
echo "OK"
EOF
chmod +x "$STUB_CURL"

PATH="$TMP_DIR:$PATH"
export PTERO_API_KEY="test"
export PTERO_PANEL_URL="http://example.invalid"
export PTERO_DEFAULT_SERVER="serverid"

DUMMY_FILE="$TMP_DIR/test.jar"
printf 'dummy' > "$DUMMY_FILE"

failures=0

run_cmd() {
  STATUS=0
  OUTPUT="$($@ 2>&1)" || STATUS=$?
}

assert_contains() {
  local needle="$1"
  local haystack="$2"
  if [[ "$haystack" != *"$needle"* ]]; then
    echo "Expected to find: $needle"
    echo "Output was: $haystack"
    failures=$((failures+1))
  fi
}

run_cmd "$REPO_ROOT/pteroctl" upload "$DUMMY_FILE"
assert_contains "Directory not given; defaulting to /plugins" "$OUTPUT"
assert_contains "/plugins" "$OUTPUT"

run_cmd "$REPO_ROOT/pteroctl" upload "$DUMMY_FILE" /otherdir
assert_contains "/otherdir" "$OUTPUT"

run_cmd "$REPO_ROOT/pteroctl" upload "$DUMMY_FILE" -d /mods
assert_contains "/mods" "$OUTPUT"

run_cmd "$REPO_ROOT/pteroctl" upload "$DUMMY_FILE" /positional -d /flags
assert_contains "/flags" "$OUTPUT"

run_cmd "$REPO_ROOT/pteroctl" upload "$DUMMY_FILE" -d
if [[ "$STATUS" -eq 0 ]]; then
  echo "Expected failure for missing -d arg"
  failures=$((failures+1))
fi

if [[ "$failures" -gt 0 ]]; then
  echo "Tests failed: $failures"
  exit 1
fi

echo "All tests passed"
