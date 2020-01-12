package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public AFKCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        // Toggle player's AFK status
        PlayerData data = this.plugin.getDataManager().getData(player);
        data.setAfk(!data.isAfk());
        if (data.isAfk()) {
            player.sendMessage("You are now AFK");
            player.sendTitle(ChatColor.RED + ChatColor.BOLD.toString() + "You are AFK",
                    "Run /afk again to turn it off", 10, 999999, 20);
        } else {
            player.sendMessage("You are no longer AFK");
            player.sendTitle(ChatColor.RED + ChatColor.BOLD.toString() + "You are AFK",
                    "Run /afk again to turn it off", 0, 1, 20);
        }

        return true;
    }
}
