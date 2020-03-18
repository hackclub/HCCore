package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TheoCommand implements CommandExecutor {
    private static final String THEO = "Hey guys, absolutely not ok";

    private final HCCorePlugin plugin;

    public TheoCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.chat(TheoCommand.THEO);
        } else {
            player.chat(String.join(" ", args) + " " + TheoCommand.THEO);
        }

        return true;
    }
}
