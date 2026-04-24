package net.azox.hotpotato.managers;

import net.azox.hotpotato.HotPotato;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HotPotatoManager {

    private final HotPotato plugin;
    private final Map<Player, UUID> glowedPlayers;
    
    private Player potatoHolder;
    private BukkitTask timerTask;
    private int remainingTime;

    public HotPotatoManager(final HotPotato plugin) {
        this.plugin = plugin;
        this.glowedPlayers = new HashMap<>();
    }

    public void triggerPotato() {
        if (this.potatoHolder != null) {
            this.plugin.getLogger().info("Potato is already active!");
            return;
        }

        final Player eligible = this.findEligiblePlayer();
        if (eligible == null) {
            this.plugin.getLogger().info("No eligible player found!");
            return;
        }

        this.givePotato(eligible);
    }

    public Player findEligiblePlayer() {
        final List<Player> onlinePlayers = new ArrayList<>();
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player == null) continue;
            if (player.isDead()) continue;
            if (!player.isOnline()) continue;
            if (player.getLocation().getBlockY() < this.plugin.getMinY()) continue;
            onlinePlayers.add(player);
        }

        if (onlinePlayers.size() < 2) {
            return null;
        }

        for (int i = 0; i < onlinePlayers.size(); i++) {
            for (int j = i + 1; j < onlinePlayers.size(); j++) {
                final Player a = onlinePlayers.get(i);
                final Player b = onlinePlayers.get(j);
                final double dist = a.getLocation().distance(b.getLocation());
                if (dist <= this.plugin.getDistance()) {
                    if (onlinePlayers.size() == 2) {
                        return onlinePlayers.get(ThreadLocalRandom.current().nextInt(2));
                    }
                }
            }
        }

        double bestTotalDistance = Double.MAX_VALUE;
        Player bestPlayer = null;

        for (final Player candidate : onlinePlayers) {
            double totalDistance = 0.0;
            for (final Player other : onlinePlayers) {
                if (candidate.equals(other)) continue;
                totalDistance += candidate.getLocation().distance(other.getLocation());
            }
            if (totalDistance < bestTotalDistance) {
                bestTotalDistance = totalDistance;
                bestPlayer = candidate;
            }
        }

        if (bestPlayer != null) {
            return bestPlayer;
        }

        return onlinePlayers.get(ThreadLocalRandom.current().nextInt(onlinePlayers.size()));
    }

    public boolean isEligible() {
        return findEligiblePlayer() != null;
    }

    private void givePotato(final Player player) {
        this.potatoHolder = player;

        final ItemStack potato = new ItemStack(Material.POISONOUS_POTATO, 1);
        final ItemMeta meta = potato.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Hot Potato");
            final List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Don't hold this for too long!");
            meta.setLore(lore);
            potato.setItemMeta(meta);
        }

        final ItemStack leftover = player.getInventory().addItem(potato).get(0);
        if (leftover != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        player.sendMessage(ChatColor.RED + "You received the HOT POTATO! Throw it to someone else quickly!");
        this.plugin.getServer().broadcastMessage(ChatColor.RED + "A Hot Potato has appeared!");

        this.remainingTime = this.plugin.getTimerDuration();
        this.startTimer();

        this.updateGlowEffects();
    }

    private void startTimer() {
        this.timerTask = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> {
            if (this.potatoHolder == null || this.potatoHolder.isDead() || !this.potatoHolder.isOnline()) {
                this.cancelTimer();
                return;
            }

            this.remainingTime--;

            if (this.remainingTime <= 0) {
                this.explodePotato();
            }
        }, 20L, 20L);
    }

    private void cancelTimer() {
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }

    private void explodePotato() {
        if (this.potatoHolder == null) return;

        final Player holder = this.potatoHolder;
        final String holderName = holder.getName();
        
        this.clearGlowEffects();
        this.removePotato(holder);
        this.cancelTimer();
        this.potatoHolder = null;

        final String predeath = this.plugin.getPredeathMessage().replace("{player}", holderName);
        this.plugin.getServer().broadcastMessage(predeath);

        holder.setHealth(0.0);
        
        final Location explosionLoc = holder.getLocation();
        holder.getWorld().createExplosion(explosionLoc, 4.0f, false, false, holder);

        this.plugin.getServer().broadcastMessage(ChatColor.RED + holderName + " was blown up by the hot potato!");
    }

    private void removePotato(final Player player) {
        final ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];
            if (item != null && item.getType() == Material.POISONOUS_POTATO) {
                final ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Hot Potato")) {
                    contents[i] = null;
                    player.getInventory().setContents(contents);
                    player.updateInventory();
                    return;
                }
            }
        }
    }

    public void transferPotato(final Player from, final Player to) {
        this.removePotato(from);
        
        this.potatoHolder = to;
        
        final ItemStack potato = new ItemStack(Material.POISONOUS_POTATO, 1);
        final ItemMeta meta = potato.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Hot Potato");
            final List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Don't hold this for too long!");
            meta.setLore(lore);
            potato.setItemMeta(meta);
        }

        final ItemStack leftover = to.getInventory().addItem(potato).get(0);
        if (leftover != null) {
            to.getWorld().dropItemNaturally(to.getLocation(), leftover);
        }

        to.sendMessage(ChatColor.RED + "You received the HOT POTATO! Throw it to someone else quickly!");
        
        this.updateGlowEffects();
    }

    public void attemptTransfer(final Player passer, final Player receiver) {
        if (this.potatoHolder == null) return;
        if (!this.potatoHolder.equals(passer)) return;

        if (this.plugin.isResetTimerOnTransfer()) {
            this.remainingTime = this.plugin.getTimerDuration();
        } else if (this.plugin.getIncreaseTimerOnTransfer() > 0) {
            this.remainingTime += this.plugin.getIncreaseTimerOnTransfer();
        }

        this.transferPotato(passer, receiver);
    }

    private void updateGlowEffects() {
        this.clearGlowEffects();
        
        if (this.potatoHolder == null) return;

        final Location holderLoc = this.potatoHolder.getLocation();
        
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player == null) continue;
            if (player.equals(this.potatoHolder)) continue;
            if (player.isDead()) continue;
            if (!player.isOnline()) continue;
            
            final Location playerLoc = player.getLocation();
            if (playerLoc.getBlockY() < this.plugin.getGlowMinY()) continue;
            
            final double distance = holderLoc.distance(playerLoc);
            if (distance <= this.plugin.getGlowDistance()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1, true, false));
                this.glowedPlayers.put(player, player.getUniqueId());
            }
        }
    }

    private void clearGlowEffects() {
        for (final Player player : this.glowedPlayers.keySet()) {
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
        this.glowedPlayers.clear();
    }

    public boolean hasPotato(final Player player) {
        return this.potatoHolder != null && this.potatoHolder.equals(player);
    }

    public Player getPotatoHolder() {
        return potatoHolder;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void reloadConfig() {
    }
}