package net.azox.hotpotato;

import net.azox.hotpotato.commands.HotPotatoCommand;
import net.azox.hotpotato.listeners.PlayerInteractionListener;
import net.azox.hotpotato.managers.HotPotatoManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotPotato extends JavaPlugin {

    private static HotPotato instance;

    private HotPotatoManager hotPotatoManager;
    private FileConfiguration config;
    private int distance;
    private int minY;
    private int timerDuration;
    private String predeathMessage;
    private boolean resetTimerOnTransfer;
    private int increaseTimerOnTransfer;
    private int glowDistance;
    private int glowMinY;
    private int randomChance;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.reloadConfig();
        this.config = this.getConfig();

        this.loadConfig();

        this.hotPotatoManager = new HotPotatoManager(this);

        this.getServer().getPluginManager().registerEvents(new PlayerInteractionListener(this), this);

        this.getCommand("hotpotato").setExecutor(new HotPotatoCommand(this));
        this.getCommand("hpt").setExecutor(new HotPotatoCommand(this));

        this.getLogger().info("HotPotato plugin enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("HotPotato plugin disabled!");
    }

    private void loadConfig() {
        this.distance = this.config.getInt("distance", 200);
        this.minY = this.config.getInt("min-y", 10);
        this.timerDuration = this.config.getInt("timer-duration", 420);
        this.predeathMessage = this.config.getString("predeath-message", "The timer on the hot potato reached zero while {player} was holding it.");
        this.resetTimerOnTransfer = this.config.getBoolean("reset-timer-on-transfer", false);
        this.increaseTimerOnTransfer = this.config.getInt("increase-timer-on-transfer", 0);
        this.glowDistance = this.config.getInt("glow-distance", 500);
        this.glowMinY = this.config.getInt("glow-min-y", 10);
        this.randomChance = this.config.getInt("random-chance", 0);
    }

    public void reloadPluginConfig() {
        this.reloadConfig();
        this.config = this.getConfig();
        this.loadConfig();
        this.hotPotatoManager.reloadConfig();
    }

    public static HotPotato getInstance() {
        return instance;
    }

    public HotPotatoManager getHotPotatoManager() {
        return hotPotatoManager;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public int getDistance() {
        return distance;
    }

    public int getMinY() {
        return minY;
    }

    public int getTimerDuration() {
        return timerDuration;
    }

    public String getPredeathMessage() {
        return predeathMessage;
    }

    public boolean isResetTimerOnTransfer() {
        return resetTimerOnTransfer;
    }

    public int getIncreaseTimerOnTransfer() {
        return increaseTimerOnTransfer;
    }

    public int getGlowDistance() {
        return glowDistance;
    }

    public int getGlowMinY() {
        return glowMinY;
    }

    public int getRandomChance() {
        return randomChance;
    }
}