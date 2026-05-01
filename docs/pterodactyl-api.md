# Pterodactyl Panel API Reference

## Base URL
```
https://panel.azox.net/api/client/servers/{SERVER_UUID}
```
- Server UUID: `9323a5c9` (isk)

## Authentication
```bash
API_KEY="ptlc_kPxhdNoDn7Qo9UEPV4t8a5A1gM2ZAopYjazU9wap7r2"
HEADER="Authorization: Bearer $API_KEY"
HEADER_JSON="Authorization: Bearer $API_KEY -H 'Accept: Application/vnd.pterodactyl.v1+json' -H 'Content-Type: application/json'"
```

---

## Common Operations

### 1. Start/Stop/Restart Server
```bash
# Power actions (via pteroctl script - API returns 403)
./pteroctl -s 9323a5c9 restart
./pteroctl -s 9323a5c9 start
./pteroctl -s 9323a5c9 stop

# Via API (requires special permissions - usually blocked)
curl -X POST -H "$HEADER_JSON" -d '{"action":"start"}' "$PANEL_URL/api/client/servers/$SERVER/power"
```

### 2. Get Server Status
```bash
curl -s -H "$HEADER" -H "Accept: Application/vnd.pterodactyl.v1+json" \
  "$PANEL_URL/api/client/servers/$SERVER" | python3 -c "import json,sys; print(json.load(sys.stdin)['attributes']['status']['state'])"
```

### 3. Run Console Command
```bash
# Via pteroctl (recommended)
./pteroctl -s 9323a5c9 cmd say hello world

# Via API
curl -X POST -H "$HEADER_JSON" -d '{"command":"say hello"}' \
  "$PANEL_URL/api/client/servers/$SERVER/command"
```

### 4. List Files in Directory
```bash
curl -s -H "$HEADER" -H "Accept: Application/vnd.pterodactyl.v1+json" \
  "$PANEL_URL/api/client/servers/$SERVER/files/list?directory=%2Fplugins" | python3 -c "
import json,sys
d=json.load(sys.stdin)
for f in d['data']:
    a=f['attributes']
    print(a['name'])"
```

### 5. Upload File to Server
```bash
# Step 1: Get upload URL (include target directory in query)
UPLOAD_URL=$(curl -s -H "$HEADER" -H "Accept: Application/vnd.pterodactyl.v1+json" \
  "$PANEL_URL/api/client/servers/$SERVER/files/upload?directory=%2Fplugins" | python3 -c "import json,sys; print(json.load(sys.stdin)['attributes']['url'])")

# Step 2: Upload - MUST include directory param in POST URL too
curl -X POST "${UPLOAD_URL}&directory=%2Fplugins" \
  -F "files=@myfile.jar" \
  -F "directory=/plugins"
```

### 6. Download File from Server
```bash
# Step 1: Get signed URL
SIGNED_URL=$(curl -s -H "$HEADER" -H "Accept: application/json" \
  "$PANEL_URL/api/client/servers/$SERVER/files/download?file=%2Fplugins%2FPlugManX%2Fconfig.yml" | \
  python3 -c "import json,sys; print(json.load(sys.stdin)['attributes']['url'])")

# Step 2: Fetch the actual file content
curl -sL "$SIGNED_URL"
```

### 7. Delete File from Server
```bash
curl -X POST -H "$HEADER" -H "Accept: Application/vnd.pterodactyl.v1+json" \
  -H "Content-Type: application/json" \
  -d '{"root": "/plugins", "files": ["oldfile.jar"]}' \
  "$PANEL_URL/api/client/servers/$SERVER/files/delete"
```

### 8. Rename/Move File
```bash
curl -X PUT -H "$HEADER" -H "Accept: Application/vnd.pterodactyl.v1+json" \
  -H "Content-Type: application/json" \
  -d '{"root": "/", "files": [{"from": "oldname.jar", "to": "/plugins/newname.jar"}]}' \
  "$PANEL_URL/api/client/servers/$SERVER/files/rename"
```

---

## Pteroctl CLI Tool

Located at: `/home/xt/isk-ai/pteroctl`

### Usage
```bash
cd /home/xt/isk-ai

# Server management
./pteroctl -s 9323a5c9 status          # Check server state
./pteroctl -s 9323a5c9 start           # Start server
./pteroctl -s 9323a5c9 stop            # Stop server  
./pteroctl -s 9323a5c9 restart         # Restart server

# Run commands
./pteroctl -s 9323a5c9 cmd <command>    # Run console command

# File operations
./pteroctl upload <file> [dir]       # Upload file to server
./pteroctl getlog                   # Get latest.log contents

# List servers
./pteroctl servers                 # List available servers
```

---

## Modrinth API

### Base URL
```
https://api.modrinth.com/v2
```

### Search for Plugins
```bash
# Search plugins for Paper server, MC 1.21
curl -s "https://api.modrinth.com/v2/search?query=night%20vision&facets=%5B%5B%22categories%3Apaper%22%5D%5D" | \
  python3 -c "import json,sys; d=json.load(sys.stdin); [print(h['project_id'], h['title']) for h in d['hits']]"
```

### Get Plugin Versions
```bash
# Get all versions for a project
curl -s "https://api.modrinth.com/v2/project/{PROJECT_ID}/version" | python3 -c "
import json,sys
for v in json.load(sys.stdin):
    print(v['version_number'], v.get('loaders', []))"

# Filter by game version
curl -s "https://api.modrinth.com/v2/project/{PROJECT_ID}/version?game_version=1.21" | \
  python3 -c "import json,sys; d=json.load(sys.stdin); print(d[0]['files'][0]['url'])"

# Filter by loader (paper/spigot/purpur)
curl -s "https://api.modrinth.com/v2/project/{PROJECT_ID}/version?game_version=1.21" | \
  python3 -c "
import json,sys
d=json.load(sys.stdin)
for v in d:
    if 'paper' in v.get('loaders', []):
        print(v['files'][0]['url'])
        break"
```

### Get Project Info
```bash
# Get project ID from name
curl -s "https://api.modrinth.com/v2/project/{slug}" | python3 -c "
import json,sys; print(json.load(sys.stdin)['id'])"

# Get latest version number
curl -s "https://api.modrinth.com/v2/project/{slug}" | python3 -c "
import json,sys; print(json.load(sys.stdin)['latest_version'])"
```

### Download Plugin from Modrinth
```bash
cd /home/xt/isk-ai/plugins

# Direct download using URL from API
curl -sL -o output.jar "https://cdn.modrinth.com/data/{PROJECT_ID}/versions/{VERSION_ID}/{FILENAME}.jar"

# Or use the files[*].url directly from API response
curl -sL -o plugin.jar "https://cdn.modrinth.com/data/wKkoqHrH/versions/gGuSAbZ6/Geyser-Spigot.jar"
```

### Known Project IDs
| Plugin | Project ID |
|--------|-----------|
| ProtocolLib | spiRhyWG |
| AuthMeReloaded | NF6Ryz82 |
| Geyser | wKkoqHrH |
| LagFixer | fD1X4Kwd |
| NoCreeperCraters | Lx7VcXLd |
| Chunky | fALzjamp |
| Orebfuscator | ore |
| LsNightVision | 5WI5Clzr |

---

## SpigotMC Download (Alternative)

Some plugins not on Modrinth - download from Spigot:

```bash
# Download from Spigot resource page
curl -sL -o plugin.jar "https://www.spigotmc.org/resources/{RESOURCE_ID}/download"

# Example: PlugManX
curl -sL -o PlugManX.jar "https://www.spigotmc.org/resources/plugmanx.88139/download"
```

Note: Spigot often blocks curl with Cloudflare. Use Modrinth when available.

---

## GitHub Releases (Fallback)

For plugins hosted on GitHub:

```bash
# Download latest release
curl -sL -o plugin.jar "https://github.com/{USER}/{REPO}/releases/latest/download/{FILENAME}"

# Download specific release
curl -sL -o plugin.jar "https://github.com/{USER}/{REPO}/releases/download/{TAG}/{FILENAME}"

# Examples
curl -sL -o protocollib.jar "https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar"
curl -sL -o authme.jar "https://github.com/AuthMe/AuthMeReloaded/releases/download/5.6.0/AuthMe-5.6.0.jar"
```

---

## Troubleshooting

### Files Uploaded to Wrong Directory
Upload API ignores `?directory=` param. Files go to root `/` instead:
1. Upload to `/` (default)
2. Move using rename API:
```bash
curl -X PUT -H "$HEADER" -H "Content-Type: application/json" \
  -d '{"root": "/", "files": [{"from": "myfile.jar", "to": "/plugins/myfile.jar"}]}' \
  "$PANEL_URL/api/client/servers/$SERVER/files/rename"
```

### Cloudflare Blocking Downloads
Use these sources in order:
1. **Modrinth API** (bypasses Cloudflare)
2. **GitHub Releases** (usually works)
3. **Pterodactyl Upload** (works via panel, not direct download)
4. Manual download (browser)

### Plugin Won't Load
- Check if valid JAR: `file plugin.jar`
- Check compatibility: MC version, server fork (Paper/Purpur)
- Check logs: `./pteroctl getlog | grep -i error`

### File Download Returns 404
- URL encode the path: `%2F` for `/`
- Check file exists: list directory first

---

## Server Info

| Property | Value |
|----------|-------|
| Server Name | isk |
| UUID | 9323a5c9 |
| Panel URL | panel.azox.net |
| Node | azox |
| Minecraft | Purpur 1.21.11 |
| RAM | 6GB |
| Port | 25565 |
| Bedrock Port | 19132 (via Geyser) |