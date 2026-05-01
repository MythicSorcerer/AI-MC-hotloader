# Pteroctl Cmd Log Tail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `pteroctl cmd` print up to 20 log lines after the command timestamp by default, with opt-out and limit flags.

**Architecture:** Keep `pteroctl` as a bash script. Capture a timestamp before sending the command, then call the existing `getlog` endpoint and filter lines after the timestamp. Add `--no-logs` and `--logs <n>` parsing inside the `cmd` case only, without affecting global args.

**Tech Stack:** Bash, python3.

---

### Task 1: Add failing tests for cmd log tailing

**Files:**
- Modify: `tests/pteroctl_upload_tests.sh`

- [ ] **Step 1: Write the failing test**

Append this block to the test file after the existing `cmd` test:

```bash
LOG_MARKER="[$(date +"%H:%M:%S")] [Server thread/INFO]: Summoned new Pig"
export PTERO_DEFAULT_SERVER="serverid"

STUB_LOG="$TMP_DIR/log.txt"
printf "%s\n" "$LOG_MARKER" > "$STUB_LOG"

STUB_CURL="$TMP_DIR/curl"
cat > "$STUB_CURL" <<'EOF'
#!/usr/bin/env bash
if [[ "$*" == *"/resources"* ]]; then
  echo "200"
  exit 0
fi
if [[ "$*" == *"/command"* ]]; then
  printf "\n204"
  exit 0
fi
if [[ "$*" == *"/files/download"* ]]; then
  echo '{"attributes":{"url":"file://LOG_PLACEHOLDER"}}'
  exit 0
fi
echo "OK"
EOF
sed -i "s|LOG_PLACEHOLDER|$STUB_LOG|g" "$STUB_CURL"
chmod +x "$STUB_CURL"

PATH="$TMP_DIR:$PATH"

run_cmd "$REPO_ROOT/pteroctl" cmd "summon pig"
assert_contains "Summoned new Pig" "$OUTPUT"
```

- [ ] **Step 2: Run test to verify it fails**

Run: `bash tests/pteroctl_upload_tests.sh`
Expected: FAIL because `cmd` does not print log lines yet.

### Task 2: Implement cmd log tailing

**Files:**
- Modify: `pteroctl`

- [ ] **Step 1: Write minimal implementation**

Update the `cmd)` case to:
- Capture `CMD_TS=$(date +"%H:%M:%S")` before sending.
- Parse `--no-logs` and `--logs <n>` from the command args.
- After successful 204, if logs enabled:
  - call `getlog` endpoint (same as `getlog` case)
  - filter lines with timestamp >= `CMD_TS`
  - print up to `LOG_LIMIT` lines
  - if fewer lines found, print “(Output cut off at N lines)”

Replace the `cmd)` block with:

```bash
    cmd)
        require_server
        shift
        LOGS_ENABLED=1
        LOG_LIMIT=20
        CMD_ARGS=()

        while [ "$#" -gt 0 ]; do
            case "$1" in
                --no-logs)
                    LOGS_ENABLED=0
                    ;;
                --logs)
                    shift
                    if [ -z "${1:-}" ]; then
                        echo "Error: --logs requires a number"
                        exit 1
                    fi
                    LOG_LIMIT="$1"
                    ;;
                *)
                    CMD_ARGS+=("$1")
                    ;;
            esac
            shift || true
        done

        COMMAND="${CMD_ARGS[*]}"
        if [ -z "$COMMAND" ]; then
            echo "Error: no command provided"
            exit 1
        fi

        CMD_TS=$(date +"%H:%M:%S")
        RESPONSE=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $API_KEY" \
            -H "Content-Type: application/json" \
            -X POST -d "{\"command\":\"$COMMAND\"}" \
            "$PANEL_URL/api/client/servers/$SERVER/command")
        STATUS="${RESPONSE##*$'\n'}"
        BODY="${RESPONSE%$'\n'*}"
        if [ "$STATUS" != "204" ]; then
            echo "Error: command failed (HTTP $STATUS)."
            if [ -n "$BODY" ]; then
                echo "Response: $BODY"
            fi
            exit 1
        fi
        echo "Sent: $COMMAND"

        if [ "$LOGS_ENABLED" -eq 1 ]; then
            LOG_RESPONSE=$(curl -s -H "Authorization: Bearer $API_KEY" \
                -H "Accept: application/octet-stream" \
                "$PANEL_URL/api/client/servers/$SERVER/files/download?file=/logs/latest.log" | python3 -c "
import json,sys,urllib.request
data=json.load(sys.stdin)
url=data['attributes']['url']
print(urllib.request.urlopen(url).read().decode('utf-8'))")
            LOG_RESPONSE="$LOG_RESPONSE" LOG_LIMIT="$LOG_LIMIT" CMD_TS="$CMD_TS" python3 - <<'PY'
import os
import sys

log_text = os.environ.get("LOG_RESPONSE", "")
limit = int(os.environ.get("LOG_LIMIT", "20"))
ts = os.environ.get("CMD_TS", "")
printed = 0

for line in log_text.splitlines():
    if not line.startswith("["):
        continue
    end = line.find("]")
    if end == -1:
        continue
    stamp = line[1:end]
    if stamp >= ts:
        print(line)
        printed += 1
        if printed >= limit:
            print(f"(Output cut off at {limit} lines)")
            break
PY
        fi
        ;;
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `bash tests/pteroctl_upload_tests.sh`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add pteroctl tests/pteroctl_upload_tests.sh
git commit -m "feat: tail logs after cmd"
```

---

## Self-Review
- Spec coverage: default log tailing, limits, and opt-out.
- Placeholder scan: no TODO/TBD.
- Type consistency: flags and variable names consistent.
