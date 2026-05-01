# Pteroctl Watch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `watch` command to pteroctl for live log tailing + command input in one terminal.

**Architecture:** Use bash + curl + python3 polling. Track log position between polls. Use `stty` for non-blocking input or simple `read` at prompt.

**Tech Stack:** Bash, curl, python3, pteroctl API

---

### Task 1: Add watch case to pteroctl

**Files:**
- Modify: `pteroctl`

- [ ] **Step 1: Write implementation**

Add `watch)` case after `download)` case:

```bash
watch)
    require_server
    POLL_INTERVAL=1
    INITIAL_LINES=50
    
    while [ "$#" -gt 0 ]; do
        case "$1" in
            --delay|-d)
                shift
                POLL_INTERVAL="${1:-1}"
                ;;
            --lines|-l)
                shift
                INITIAL_LINES="${1:-50}"
                ;;
            *)
                ;;
        esac
        shift || true
    done

    LAST_SIZE=0
    
    echo "=== Watching server: $SERVER ==="
    echo "=== Press Ctrl+C to exit ==="
    echo ""
    
    _fetch_log() {
        curl -s -H "Authorization: Bearer $API_KEY" \
            -H "Accept: application/octet-stream" \
            "$PANEL_URL/api/client/servers/$SERVER/files/download?file=/logs/latest.log" | python3 -c "
import json,sys,urllib.request
data=json.load(sys.stdin)
url=data['attributes']['url']
print(urllib.request.urlopen(url).read().decode('utf-8'))"
    }
    
    INITIAL=$(_fetch_log | tail -n "$INITIAL_LINES")
    echo "$INITIAL"
    LAST_SIZE=$(echo "$INITIAL" | wc -c)
    
    while true; do
        sleep "$POLL_INTERVAL" || true
        
        LOG_CONTENT=$(_fetch_log)
        NEW_SIZE=$(echo "$LOG_CONTENT" | wc -c)
        
        if [ "$NEW_SIZE" -gt "$LAST_SIZE" ]; then
            echo "$LOG_CONTENT" | tail -c +"$LAST_SIZE"
            LAST_SIZE="$NEW_SIZE"
        fi
        
        if read -t 1 -p "> " cmd 2>/dev/null; then
            if [ -n "$cmd" ]; then
                curl -s -X POST -H "Authorization: Bearer $API_KEY" \
                    -H "Content-Type: application/json" \
                    -d "{\"command\":\"$cmd\"}" \
                    "$PANEL_URL/api/client/servers/$SERVER/command" >/dev/null
            fi
        fi
    done
    ;;
```

- [ ] **Step 2: Test manually**

Run: `./pteroctl watch`
- Should show last 50 lines
- Wait >1s, see new lines appear
- Type command, see response in logs
- Ctrl+C to exit

- [ ] **Step 3: Commit**

```bash
git add pteroctl
git commit -m "feat: add watch command for live monitoring"
```

---

## Self-Review
- Spec coverage: watch command, poll interval, initial lines
- Placeholder scan: no TODOs
- Type consistency: variable names consistent