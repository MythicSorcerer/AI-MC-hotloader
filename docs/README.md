# ISK AI - Server Documentation

## Overview
This repo manages the ISK Minecraft server plugins and deployment.

## Quick Commands
```bash
cd /home/xt/isk-ai

# Server control
./pteroctl -s 9323a5c9 status      # Check status
./pteroctl -s 9323a5c9 restart    # Restart server
./pteroctl -s 9323a5c9 cmd <cmd>  # Run command

# Upload plugin
./pteroctl upload myplugin.jar /plugins
```

## Plugins (15 installed)
| Plugin | Purpose |
|--------|---------|
| AuthMe | Authentication |
| Chunky | Chunk pre-generation |
| DiscordSRV | Discord bridge |
| Geyser-Spigot | Bedrock support |
| LagFixer | Performance |
| LsNightVision | Night vision toggle |
| NoCreeperCraters | Prevent craters |
| Orebfuscator | Chunk obfuscation |
| PacketEvents | Packet handling |
| ProtocolLib | Protocol lib |
| PlugManX | Plugin management |
| Vault | Permissions API |
| + 3 custom | isk-eco, IskCmd, IskCtrl |

## Docs
- [pterodactyl-api.md](pterodactyl-api.md) - Full API reference
- [MC_PLUGIN_SKILL.md](MC_PLUGIN_SKILL.md) - Plugin dev workflow

## Server
- Panel: panel.azox.net
- UUID: 9323a5c9
- Purpur 1.21.11, 6GB RAM