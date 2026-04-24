package net.azox.example.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (event == null) {
            return;
        }
        
        final var player = event.getPlayer();
        if (player == null) {
            return;
        }
        
        player.sendMessage("§aWelcome to the server, §f" + player.getName() + "§a!");
    }
}