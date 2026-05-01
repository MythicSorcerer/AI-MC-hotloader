#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

LOG_MARKER="[00:00:00] [Server thread/INFO]: Summoned new Pig"
STUB_LOG="$TMP_DIR/log.txt"
export STUB_LOG
printf "%s\n%s\n%s\n" "[00:00:00] [Server thread/INFO]: Starting minecraft server" "$LOG_MARKER" "[00:00:01] [Server thread/INFO]: Done (1.234s)! For help, type \"help\"" > "$STUB_LOG"

STUB_CURL="$TMP_DIR/curl"
cat > "$STUB_CURL" <<'EOF'
#!/usr/bin/env bash
if [[ "$*" == *"/api/client/servers/"*"/resources" ]]; then
  printf "200"
  exit 0
fi
if [[ "$*" == *"/files/upload"* ]]; then
  echo '{"attributes":{"url":"http://upload.local"}}'
  exit 0
fi
if [[ "$*" == *"/command"* ]]; then
  printf "\n204"
  exit 0
fi
if [[ "$*" == *"/files/download"* ]]; then
  printf '{"attributes":{"url":"file://%s"}}' "$STUB_LOG"
  exit 0
fi
echo "OK"
EOF
chmod +x "$STUB_CURL"

# Ensure child processes (like pteroctl) see the stub.
export PATH="$TMP_DIR:$PATH"
export PTERO_API_KEY="test"
export PTERO_PANEL_URL="http://example.invalid"
export PTERO_DEFAULT_SERVER="serverid"

DUMMY_FILE="$TMP_DIR/test.jar"
printf 'dummy' > "$DUMMY_FILE"

OUTPUT=""
STATUS=0
failures=0

run_cmd() {
  local status=0
  local output
  output="$("$@" 2>&1)" || status=$?
  OUTPUT="$output"
  STATUS=$status
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

run_cmd "$REPO_ROOT/pteroctl" cmd say test
if [[ "$STATUS" -ne 0 ]]; then
  echo "Expected cmd to succeed"
  failures=$((failures+1))
fi

run_cmd "$REPO_ROOT/pteroctl" cmd "summon pig"
assert_contains "$LOG_MARKER" "$OUTPUT"

if [[ "$failures" -gt 0 ]]; then
  echo "Tests failed: $failures"
  exit 1
fi

echo "All tests passed"
