package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShrugCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public ShrugCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        final String SHRUG = "¯\\_(ツ)_/¯";
        if (args.length == 0) {
            player.chat(SHRUG);
        } else {
            player.chat(String.join(" ", args) + " " + SHRUG);
        }

        return true;
    }
}
