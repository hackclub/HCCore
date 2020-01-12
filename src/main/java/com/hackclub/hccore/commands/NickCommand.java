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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
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

        String newNickname = String.join(" ", args);
        final int MAX_NICKNAME_LENGTH = 16;
        if (newNickname.length() > MAX_NICKNAME_LENGTH) {
            player.sendMessage(ChatColor.RED + "Your nickname can’t be longer than "
                    + MAX_NICKNAME_LENGTH + " characters");
            return true;
        }

        this.plugin.getDataManager().getData(player).setNickname(newNickname);
        player.sendMessage(
                ChatColor.GREEN + "Your nickname was set to " + ChatColor.AQUA + newNickname);

        return true;
    }
}
