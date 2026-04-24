package net.azox.example.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ExampleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                            final String label, final String[] args) {
        if (sender == null) {
            return false;
        }
        
        if (!sender.hasPermission("example.use")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }
        
        sender.sendMessage("§aExample plugin is running!");
        
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            sender.sendMessage("§7Welcome, §f" + player.getName() + "§7!");
        }
        
        return true;
    }
}