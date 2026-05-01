# Upload Plugin - Quick Guide

## Upload a New Plugin

### 1. Download the jar
Put the `.jar` file in `/home/xt/isk-ai/plugins/`

### 2. Upload to server
```bash
cd /home/xt/isk-ai
./pteroctl -s 9323a5c9 upload plugins/myplugin.jar /plugins
```

### 3. Load it (no restart needed)
```bash
./pteroctl -s 9323a5c9 cmd "/plm load myplugin"
```

### 4. Check it loaded
```bash
./pteroctl -s 9323a5c9 getlog | grep -i "myplugin"
```

---

## Replace/Update a Plugin

### 1. Upload new jar
```bash
cd /home/xt/isk-ai
./pteroctl -s 9323a5c9 upload plugins/myplugin.jar /plugins
```

### 2. Reload it
```bash
./pteroctl -s 9323a5c9 cmd "/plm load myplugin"
```

If that doesn't work, restart:
```bash
./pteroctl -s 9323a5c9 restart
```

### 3. Check logs
```bash
./pteroctl -s 9323a5c9 getlog | grep -i "myplugin\|error"
```

---

## Delete Old Plugin

Get the list of files first:
```bash
curl -s -H "Authorization: Bearer ptlc_kPxhdNoDn7Qo9UEPV4t8a5A1gM2ZAopYjazU9wap7r2" \
  -H "Accept: Application/vnd.pterodactyl.v1+json" \
  "https://panel.azox.net/api/client/servers/9323a5c9/files/list?directory=%2Fplugins" | \
  python3 -c "import json,sys; [print(f['attributes']['name']) for f in json.load(sys.stdin)['data']]"
```

Delete:
```bash
curl -s -X POST \
  -H "Authorization: Bearer ptlc_kPxhdNoDn7Qo9UEPV4t8a5A1gM2ZAopYjazU9wap7r2" \
  -H "Accept: Application/vnd.pterodactyl.v1+json" \
  -H "Content-Type: application/json" \
  -d '{"root": "/plugins", "files": ["old-plugin.jar"]}' \
  "https://panel.azox.net/api/client/servers/9323a5c9/files/delete"
```

---

## Troubleshooting

**Plugin not loading:**
```bash
./pteroctl -s 9323a5c9 getlog | grep -i "error\|exception\|fail"
```

**Server stuck stopping:**
```bash
./pteroctl -s 9323a5c9 start  # Wait 90s
```

**Hotload failed, try full restart:**
```bash
./pteroctl -s 9323a5c9 restart
# Wait 90s for startup
```

---

## One-Liner Summary

| Task | Command |
|------|---------|
| Upload | `./pteroctl upload name.jar /plugins` |
| Hotload | `./pteroctl cmd "/plm load name"` |
| Restart | `./pteroctl restart` |
| Check logs | `./pteroctl getlog` |
| Delete file | `curl -X POST ...files/delete` |