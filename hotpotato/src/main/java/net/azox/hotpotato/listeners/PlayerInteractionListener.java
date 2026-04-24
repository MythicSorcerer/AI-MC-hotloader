package net.azox.hotpotato.listeners;

import net.azox.hotpotato.HotPotato;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractionListener implements Listener {

    private final HotPotato plugin;

    public PlayerInteractionListener(final HotPotato plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final org.bukkit.entity.Entity clicked = event.getRightClicked();

        if (!(clicked instanceof Player)) {
            return;
        }

        final Player target = (Player) clicked;

        if (this.plugin.getHotPotatoManager().hasPotato(player)) {
            this.plugin.getHotPotatoManager().attemptTransfer(player, target);
        }
    }
}