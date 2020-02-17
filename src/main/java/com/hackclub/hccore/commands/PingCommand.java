package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public PingCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        int ping = this.getPing(player);
        // Failed for some reason
        if (ping == -1) {
            player.sendMessage(ChatColor.RED + "Failed to get ping");
            return true;
        }

        player.sendMessage("Your ping is " + ping + "ms");

        return true;
    }

    private int getPing(Player player) {
        int ping = -1;
        try {
            // Use reflection because there's no Player#getPing method
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ping;
    }
}
