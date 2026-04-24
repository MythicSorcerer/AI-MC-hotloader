package net.azox.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import net.azox.example.commands.ExampleCommand;
import net.azox.example.listeners.PlayerJoinListener;

/**
 * Example - Main plugin class.
 * 
 * A hot-reloadable Bukkit/Spigot plugin compatible with PlugManX.
 */
public final class Example extends JavaPlugin {

    private static Example instance;

    @Override
    public void onEnable() {
        instance = this;
        
        final PluginManager pluginManager = this.getServer().getPluginManager();
        
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        this.getCommand("example").setExecutor(new ExampleCommand());
        
        this.getLogger().info("Example v" + this.getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Example disabled!");
        instance = null;
    }

    public static Example getInstance() {
        return instance;
    }
}