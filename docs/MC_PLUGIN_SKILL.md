# Plugin Development Skill

Load this skill when the user asks to implement a Minecraft plugin feature.

## Overview

This skill defines the workflow for rapidly developing and hotloading Minecraft plugins onto a Pterodactyl server.

## Workflow

```
Goal → Copy Example → Edit Code → Compile → Debug/Recompile → Upload → Hotload → Announce (if specified)
```

## Step by Step

### 1. ASK: Which server?

**Always ask the user which server to use** before starting:
- `9323a5c9` - isk (main server)
- Check available: `./pteroctl servers`

### 2. Copy Example Template

```bash
cp -r example <plugin-name>
cd <plugin-name>
```

Update:
- `pom.xml` - groupId: `net.azox.<pluginname>`, artifactId, name
- All `.java` files - update package to `net.azox.<pluginname>`
- Main class name matches plugin name
- `plugin.yml` - update name, main, commands

### 3. Write Code

Follow AGENTS.md coding standards:
- Package: `net.azox.{pluginname}`
- Use `this` when accessing instance members
- Use `final` where possible
- Null checks on all parameters
- Static instance pattern: `Plugin.getInstance()`
- No single-letter variable names

### 4. Compile

```bash
mvn clean package
```

If compilation fails:
- Read error message
- Fix the error
- Recompile with `mvn clean package`
- Repeat until successful

### 5. Upload to Server

```bash
./pteroctl -s <server-uuid> upload target/<pluginname>-1.0.0.jar /plugins
```

### 6. Hotload with PlugManX

```bash
./pteroctl -s <server-uuid> cmd "/plm load <pluginname>"
```

**Note:** `/plm load` gives NO response. Just accept it worked and test.

### 7. Announce (if specified)

Only if user says to announce:
```bash
./pteroctl -s <server-uuid> cmd '/tellraw @a [{"text":"Loaded New Plugin: <PluginName>","color":"yellow"}]'
```

## Configuration

API keys and settings are in `.env` (gitignored). The `pteroctl` script reads from it.

**Important:** Always verify environment variables are loaded correctly:
```bash
# Check if variables are set
echo "API Key: $PTERO_API_KEY"
echo "Panel URL: $PTERO_PANEL_URL"
echo "Default Server: $PTERO_DEFAULT_SERVER"

# If empty, source the .env file explicitly:
export $(cat .env | xargs)
```

## Server Notes

- **NEVER stop/restart server unless ABSOLUTELY needed** - disrupts all players
- **If server is offline:** Start with `./pteroctl -s <uuid> start`, then wait 60s
- **Server start time:** Can take 500+ seconds with many plugins, ~300s typical, <100s lightweight

## Testing

After loading, run the plugin's command to verify:
```bash
./pteroctl -s <server-uuid> cmd /<pluginname>
```

## Example Task

User: "implement a plugin that makes all players invisible"

1. Ask server: isk (9323a5c9)
2. `cp -r example invisible`
3. Edit code to add PlayerVisibilityListener
4. `mvn clean package`
5. `./pteroctl -s 9323a5c9 upload target/invisible-1.0.0.jar /plugins`
6. `./pteroctl -s 9323a5c9 cmd "/plm load invisible"`
7. `/invisible` to test

## Error Handling

If plugin fails to load:
- Check panel console for errors
- Fix code
- Recompile
- Upload and load again

## Troubleshooting 404 Errors

If you encounter 404 errors when using pteroctl:

1. **Verify environment variables are loaded:**
   ```bash
   echo "API Key: $PTERO_API_KEY"
   echo "Panel URL: $PTERO_PANEL_URL"
   ```
   If these show empty values, explicitly source the .env file:
   ```bash
   export $(cat .env | xargs)
   ```

2. **Test API connectivity directly:**
   ```bash
   curl -v -H "Authorization: Bearer $PTERO_API_KEY" \
        -H "Content-Type: application/json" \
        "$PTERO_PANEL_URL/api/application/servers"
   ```
   Check that you get a valid JSON response, not an HTML error page.

3. **Check server-specific endpoints:**
   ```bash
   # Replace SERVER_ID with your actual server ID
   curl -v -H "Authorization: Bearer $PTERO_API_KEY" \
        -H "Content-Type: application/json" \
        "$PTERO_PANEL_URL/api/client/servers/SERVER_ID/resources"
   ```

4. **Use explicit variable passing when needed:**
   ```bash
   PTERO_API_KEY=your_key PTERO_PANEL_URL=https://panel.azox.net ./pteroctl status
   ```

5. **Verify the server ID is correct:**
   - Run `./pteroctl servers` to see available servers
   - Check that the ID you're using matches one listed
   - The default server ID is in `.env` as `PTERO_DEFAULT_SERVER`