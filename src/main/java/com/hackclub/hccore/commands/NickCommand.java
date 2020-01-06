package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NickCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public NickCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            this.plugin.getDataManager().getData(player).setNickname(null);
            player.sendMessage(ChatColor.GREEN + "Your nickname has been reset!");
            return true;
        }

        String newName = String.join(" ", args);
        this.plugin.getDataManager().getData(player).setNickname(newName);
        player.sendMessage(
                ChatColor.GREEN + "Your nickname was set to " + ChatColor.AQUA + newName);

        return true;
    }
}
