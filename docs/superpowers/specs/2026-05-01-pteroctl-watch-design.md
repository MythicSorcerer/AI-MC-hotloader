# Pteroctl Live Monitor Design

## Goal
Add live monitoring to pteroctl: tail running logs and run commands in real-time, integrated in one terminal view.

## Scope
- Add `watch` command to pteroctl
- Live log streaming via API polling (1s interval)
- Command input line at bottom
- Ctrl+C to exit
- Optional: RCON mode for faster commands (future)

Out of scope:
- Full RCON daemon integration
- Multiple simultaneous server watchers
- Web UI

## Current Behavior
- `log` / `getlog` fetches latest.log once
- `cmd` / `run` sends one command, returns 204

## Proposed Behavior
### Command: watch
```
./pteroctl watch             # watch default server
./pteroctl -s <uuid> watch  # watch specific server
```

### UI Layout
- Top ~70%: Live log output (auto-scrolling)
- Bottom ~30%: Command input prompt with `> `

### Behavior
- Poll `latest.log` via API every 1 second
- Show only NEW lines since last poll (track position)
- On command input, POST to `/command` API
- Response shown in log stream (server echoes)
- Ctrl+C clears screen and exits

### Options
- `--delay <n>` - Poll interval in seconds (default: 1)
- `--lines <n>` - Initial lines to show (default: 50)
- `--no-scroll` - Disable auto-scroll (show all new)
- `--quiet` - Hide command prompts

## Implementation
### Architecture
- Bash + python3 for log streaming
- Track log file position between polls
- Use `read` for command input (non-blocking)
- Clear screen on exit

### API Calls
- GET `/files/download?file=/logs/latest.log` (poll)
- POST `/command` (command input)

## Testing
- Manual:
  - `./pteroctl watch` shows live logs
  - Type command, see response in logs
  - Ctrl+C exits cleanly