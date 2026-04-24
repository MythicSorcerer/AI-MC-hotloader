# pteroctl - Pterodactyl Server Control CLI

Control your Pterodactyl game servers via the command line.

## Setup

```bash
# Make executable
chmod +x pteroctl

# Optionally add to PATH
ln -s ~/ptero/pteroctl ~/bin/pteroctl
# or
sudo ln -s /home/xt/ptero/pteroctl /usr/local/bin/pteroctl
```

## Configuration

Edit the top of `pteroctl` to set your API key and panel URL:
```bash
API_KEY="ptlc_..."        # Your Pterodactyl API key
PANEL_URL="https://panel.azox.net"  # Your panel URL
```

## Usage

```bash
./pteroctl <command> [arguments]
```

### Server Management

```bash
# List all servers
./pteroctl list

# Check server status
./pteroctl status <uuid>

# Start a server
./pteroctl start <uuid>

# Stop a server
./pteroctl stop <uuid>

# Restart a server
./pteroctl restart <uuid>

# Force stop (kill)
./pteroctl kill <uuid>
```

### Console Commands

```bash
# Send a command to server console
./pteroctl cmd <uuid> <command>

# Examples:
./pteroctl cmd 97f5b764 say hello world
./pteroctl cmd 97f5b764 /op PlayerName
./pteroctl cmd 97f5b764 give PlayerName diamond 64
./pteroctl cmd 97f5b764 tp PlayerName PlayerName2
```

### File Management

```bash
# Upload a file to server
./pteroctl upload <uuid> <local-file> [remote-dir]

# Examples:
./pteroctl upload 97f5b764 myplugin.jar /plugins
./pteroctl upload 97f5b764 config.yml /
./pteroctl upload 97f5b764 world.zip /backups
```

## API Keys

### Getting an API Key

1. Go to your panel (e.g., https://panel.azox.net)
2. Click your username → Account Settings
3. Go to API Credentials
4. Create New API Key
5. Use the key starting with `ptlc_` (Application/Admin key)

### Key Types

| Prefix | Type | Access |
|--------|------|-------|
| `ptlc_` | Application | Admin panel management |
| `ptla_` | Client | User-level server control |

For controlling servers (cmd, start/stop, upload), you need a Client API key (`ptlc_` prefix works for both).

## Server UUID

Find server UUID from `./pteroctl list`:
```
 1 | azox                 | e8337f4a
10 | test server          | 0128d99d
11 | plugin testing       | 97f5b764
12 | uriah server         | b03c2811
```

Use the short identifier (rightmost column) - e.g., `97f5b764`

## Examples

### Full Workflow: Deploy a Plugin

```bash
# 1. Check server is running
./pteroctl status 97f5b764

# 2. Stop server
./pteroctl stop 97f5b764

# 3. Upload new plugin
./pteroctl upload 97f5b764 MyPlugin-v1.0.jar /plugins

# 4. Restart server
./pteroctl restart 97f5b764

# 5. Send command to verify
./pteroctl cmd 97f5b764 plugins
```

### Full Workflow: Update Config

```bash
# 1. Download current config (via panel UI or SFTP)

# 2. Edit locally

# 3. Upload
./pteroctl upload 97f5b764 server.properties /

# 4. Reload or restart
./pteroctl cmd 97f5b764 reload
```

## Troubleshooting

### Server not responding to commands
- Server may be stopped: `./pteroctl status <uuid>`
- Check if console is actually running

### Upload fails
- Max file size: 100MB (check wings config for increase)
- Check directory exists

### 403 Forbidden
- API key may be wrong or expired
- Try creating a new key in panel

### 404 Not Found
- Check server UUID is correct
- Server may be deleted

## API Reference

This tool wraps the Pterodactyl Client API:

| Endpoint | Use |
|---------|-----|
| `GET /api/application/servers` | List servers |
| `GET /api/client/servers/{uuid}/resources` | Server status |
| `POST /api/client/servers/{uuid}/power` | Start/stop/restart |
| `POST /api/client/servers/{uuid}/command` | Send console cmd |
| `GET /api/client/servers/{uuid}/files/upload` | Get upload URL |
| `POST /api/client/servers/{uuid}/files/list` | List files |

Full docs: https://pterodactyl-api-docs.netvpx.com/