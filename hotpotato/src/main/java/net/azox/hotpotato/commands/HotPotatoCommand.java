package net.azox.hotpotato.commands;

import net.azox.hotpotato.HotPotato;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HotPotatoCommand implements CommandExecutor, TabExecutor {

    private final HotPotato plugin;

    public HotPotatoCommand(final HotPotato plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                            final String label, final String[] args) {
        if (sender == null) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /hotpotato <trigger|check>");
            return true;
        }

        final String sub = args[0].toLowerCase();

        switch (sub) {
            case "trigger": {
                if (!sender.hasPermission("hotpotato.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                this.plugin.getHotPotatoManager().triggerPotato();
                sender.sendMessage(ChatColor.GREEN + "Hot potato triggered!");
                return true;
            }
            case "check": {
                if (!sender.hasPermission("hotpotato.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                final boolean eligible = this.plugin.getHotPotatoManager().isEligible();
                if (eligible) {
                    sender.sendMessage(ChatColor.GREEN + "There IS an eligible player to receive the hot potato!");
                    final Player target = this.plugin.getHotPotatoManager().findEligiblePlayer();
                    if (target != null) {
                        sender.sendMessage(ChatColor.YELLOW + "Eligible player: " + target.getName());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "There is NO eligible player to receive the hot potato.");
                    sender.sendMessage(ChatColor.GRAY + "Requirements: 2+ players online, within " + 
                            this.plugin.getDistance() + " blocks, both above Y=" + this.plugin.getMinY());
                }
                return true;
            }
            case "reload": {
                if (!sender.hasPermission("hotpotato.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                this.plugin.reloadPluginConfig();
                sender.sendMessage(ChatColor.GREEN + "HotPotato config reloaded!");
                return true;
            }
            default: {
                sender.sendMessage(ChatColor.RED + "Usage: /hotpotato <trigger|check|reload>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command,
                                      final String label, final String[] args) {
        if (sender == null) {
            return null;
        }

        if (args.length == 1) {
            final List<String> completions = new ArrayList<>();
            completions.add("trigger");
            completions.add("check");
            if (sender.hasPermission("hotpotato.admin")) {
                completions.add("reload");
            }
            return completions;
        }

        return null;
    }
}