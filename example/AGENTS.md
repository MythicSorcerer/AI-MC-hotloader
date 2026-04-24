# Plugin Development Guide

Development workflow and coding standards for Azox plugins.

## Tech Stack

- **Build Tool:** Maven
- **Server API:** Spigot API 1.21.11
- **Java Version:** 17+
- **Runtime:** Minecraft Spigot Server 1.21.11

## Package Convention

- **Package:** `net.azox.{pluginname}`
- Example: `net.azox.example`

## Project Structure

```
example/
├── pom.xml
└── src/main/
    ├── java/net/azox/example/
    │   ├── Example.java         # Main class (matches plugin name)
    │   ├── commands/           # Command executors
    │   ├── listeners/         # Event listeners
    │   ├── managers/         # Manager classes
    │   ├── models/          # Data models
    │   └── utils/           # Utility classes
    └── resources/
        └── plugin.yml
```

## Development Workflow

### Important: Ask User Which Server

**Always ask the user which server to use** before starting work. Available servers:
- `97f5b764` - plugin testing (for testing new plugins)
- `e8337f4a` - azox (main server)

### 1. Build

```bash
cd example
mvn clean package
```

### 2. Deploy to Server

```bash
# Use -s flag to specify server (default is plugin testing)
./pteroctl -s 97f5b764 upload target/example-1.0.0.jar /plugins

# Or use the default (plugin testing)
./pteroctl upload target/example-1.0.0.jar /plugins
```

### 3. Load Plugin (in-game)

```
/plm load <pluginname>
```

**Note:** PlugManX `/plm load` gives NO success/failure response. Just run it and test the plugin. Check panel console for errors if needed.

### 4. Test

```
/<pluginname>
```

## Coding Standards

### Variable Naming

Use fully qualified names - avoid single letters:

```java
// Good
private Player player;
private Location location;

// Bad
private Player p;
private Location loc;
```

### Use "this"

Always use `this` when accessing instance members:

```java
this.getServer().broadcastMessage("Hello");
this.getLogger().info("Message");
```

### Use "final" Where Possible

```java
public void onPlayerJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
}
```

### Null Safety

Always check for null:

```java
@Override
public boolean onCommand(final CommandSender sender, final Command command,
                            final String label, final String[] args) {
    if (sender == null) {
        return false;
    }
    // ...
}
```

### Static Instance Pattern

Store static instance in main class, access via getter:

```java
// Main class
private static Example instance;

public static Example getInstance() {
    return instance;
}

// In other classes
final Example plugin = Example.getInstance();
if (plugin == null) {
    return false;
}
```

## Quick Commands

```bash
# Build
mvn clean package

# Upload (specify server with -s)
./pteroctl -s 97f5b764 upload target/example-1.0.0.jar /plugins
./pteroctl upload target/example-1.0.0.jar /plugins  # uses default (97f5b764)

# In-game (no response - just run it)
/plm load example
/plm unload example
/plm reload example

# Then test with command
/example
```

## Important Notes

- **Server Start Time:** Can take 500+ seconds with many heavy plugins, ~300s typical, <100s for lightweight
- **If server is not running:** Start it with `start` command, wait 60s before any other actions
- **NEVER stop/restart the server unless ABSOLUTELY needed** - It disrupts all players
- Use `/plm load`, `/plm unload`, `/plm reload` for plugin changes instead
- `/plm load <plugin>` gives NO response - just accept it worked and test the plugin
- If plugin fails to load, check panel console for errors
- Always verify functionality after loading
- If plugin should announce on load (user specifies), add:
```
/tellraw @a [{"text":"Loaded New Plugin: <PluginName>","color":"yellow"}]
```