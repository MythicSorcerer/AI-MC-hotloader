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

### 1. Build

```bash
cd example
mvn clean package
```

### 2. Deploy to Server

```bash
./pteroctl upload 97f5b764 target/example-1.0.0.jar /plugins
```

### 3. Load Plugin (in-game)

```
/plm load example
```

### 4. Test

```
/example
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

# Upload
./pteroctl upload 97f5b764 target/example-1.0.0.jar /plugins

# In-game
/plm load example
/plm unload example
/plm reload example
```