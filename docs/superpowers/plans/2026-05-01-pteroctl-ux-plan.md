# Pteroctl UX Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `pteroctl upload` default to `/plugins` with clear messaging and add `--dir/-d` while consolidating to a single CLI.

**Architecture:** Keep `pteroctl` as a bash script with small, localized parsing changes inside the `upload` case. Add a lightweight bash test harness that stubs `curl` so upload behavior can be tested without network calls. Remove `pteroctl_fixed` and update docs to reference the single CLI and new defaults.

**Tech Stack:** Bash, curl (stubbed in tests), python3, basic shell scripts.

---

### Task 1: Add upload parsing tests (bash harness)

**Files:**
- Create: `tests/pteroctl_upload_tests.sh`

- [ ] **Step 1: Write the failing test**

```bash
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
  OUTPUT="$($@ 2>&1)" || STATUS=$?
  STATUS=${STATUS:-0}
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `bash tests/pteroctl_upload_tests.sh`
Expected: FAIL because current `pteroctl` defaults to `/` and lacks `-d/--dir` handling.

### Task 2: Implement upload defaults and flags

**Files:**
- Modify: `pteroctl`

- [ ] **Step 1: Write minimal implementation**

Update the `upload)` case to parse `-d/--dir`, default to `/plugins`, and print the default message. Replace the existing `upload)` block with this version:

```bash
    upload)
        shift
        FILE="$1"
        shift || true

        DIR_FLAG=""
        POSITIONAL_DIR=""

        while [ "$#" -gt 0 ]; do
            case "$1" in
                -d|--dir)
                    shift
                    if [ -z "${1:-}" ]; then
                        echo "Error: --dir requires a value"
                        exit 1
                    fi
                    DIR_FLAG="$1"
                    ;;
                *)
                    if [ -z "$POSITIONAL_DIR" ]; then
                        POSITIONAL_DIR="$1"
                    fi
                    ;;
            esac
            shift || true
        done

        DIR="${DIR_FLAG:-$POSITIONAL_DIR}"

        if [ -z "$DIR" ]; then
            DIR="/plugins"
            echo "Directory not given; defaulting to /plugins. Use --dir to select target directory."
        fi

        if [ -z "$FILE" ] || [ ! -f "$FILE" ]; then
            echo "Error: File not found: $FILE"
            exit 1
        fi

        UPLOAD_URL=$(curl -s -H "Authorization: Bearer $API_KEY" \
            -H "Accept: application/json" \
            "$PANEL_URL/api/client/servers/$SERVER/files/upload?directory=$(python3 -c 'import urllib.parse; print(urllib.parse.quote("'"$DIR"'"))')" | python3 -c "import json,sys; print(json.load(sys.stdin)['attributes']['url'])")

        curl -X POST "$UPLOAD_URL&directory=$(python3 -c 'import urllib.parse; print(urllib.parse.quote("'"$DIR"'"))')" \
            -F "files=@$FILE"
        echo "Uploaded: $FILE to $SERVER/$DIR"
        ;;
```

- [ ] **Step 2: Run test to verify it passes**

Run: `bash tests/pteroctl_upload_tests.sh`
Expected: PASS with “All tests passed”.

- [ ] **Step 3: Commit**

```bash
git add tests/pteroctl_upload_tests.sh pteroctl
git commit -m "feat: improve upload defaults"
```

### Task 3: Consolidate CLI and update docs

**Files:**
- Delete: `pteroctl_fixed`
- Modify: `docs/README.md`
- Modify: `docs/UPLOAD_PLUGIN.md`
- Modify: `docs/pterodactyl-api.md`

- [ ] **Step 1: Update docs to reflect default `/plugins` and flags**

Apply these edits:

`docs/README.md`
```diff
-./pteroctl upload myplugin.jar /plugins
+./pteroctl upload myplugin.jar  # defaults to /plugins
+./pteroctl upload myplugin.jar --dir /plugins
```

`docs/UPLOAD_PLUGIN.md`
```diff
-./pteroctl -s 9323a5c9 upload plugins/myplugin.jar /plugins
+./pteroctl -s 9323a5c9 upload plugins/myplugin.jar  # defaults to /plugins
+./pteroctl -s 9323a5c9 upload plugins/myplugin.jar --dir /plugins
```

`docs/pterodactyl-api.md`
```diff
-./pteroctl upload <file> [dir]       # Upload file to server
+./pteroctl upload <file> [dir]       # Defaults to /plugins
+./pteroctl upload <file> --dir <dir> # Explicit directory
```

- [ ] **Step 2: Remove the duplicate CLI**

Run: `git rm pteroctl_fixed`

- [ ] **Step 3: Commit**

```bash
git add docs/README.md docs/UPLOAD_PLUGIN.md docs/pterodactyl-api.md
git commit -m "chore: consolidate pteroctl cli"
```

---

## Self-Review
- Spec coverage: upload defaults, flags, clear message, docs updated, and `pteroctl_fixed` removed.
- Placeholder scan: no TODO/TBD.
- Type consistency: shell variables and flag names consistent.
