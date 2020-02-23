package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public SpawnCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;

        // Player needs to be in the Overworld
        if (player.getWorld().getEnvironment() != Environment.NORMAL) {
            sender.sendMessage(ChatColor.RED + "You can only use this command in the Overworld");
            return true;
        }

        // Player needs to be within the allowed radius from spawn
        int distanceFromSpawn =
                (int) player.getLocation().distance(player.getWorld().getSpawnLocation());
        final int ALLOWED_RADIUS = 2000;
        if (distanceFromSpawn > ALLOWED_RADIUS) {
            sender.sendMessage(ChatColor.RED + "You need to be within " + ALLOWED_RADIUS
                    + " blocks from spawn to use this command. Currently, you’re "
                    + (distanceFromSpawn - ALLOWED_RADIUS) + " blocks too far.");
            return true;
        }

        // Player needs to be on the ground
        if (!player.isOnGround()) {
            sender.sendMessage(
                    ChatColor.RED + "You need to be standing on the ground to use this command");
            return true;
        }

        // Player needs to have sky access (no blocks above them at all)
        Block highestBlock = player.getWorld().getHighestBlockAt(player.getLocation());
        if (player.getLocation().getY() < highestBlock.getY()) {
            sender.sendMessage(
                    ChatColor.RED + "You need to have direct sky access to use this command");
            return true;
        }

        // Can't be used while in damage cooldown period
        PlayerData data = this.plugin.getDataManager().getData(player);
        long currentTime = System.currentTimeMillis();
        long secondsSinceLastDamaged = (currentTime - data.getLastDamagedAt()) / 1000;
        final int DAMAGE_WAIT_TIME = 20;
        if (secondsSinceLastDamaged < DAMAGE_WAIT_TIME) {
            sender.sendMessage(
                    ChatColor.RED + "You were hurt recently—you’ll be able to use this command in "
                            + (DAMAGE_WAIT_TIME - secondsSinceLastDamaged) + " seconds");
            return true;
        }

        player.teleport(player.getWorld().getSpawnLocation());
        sender.sendMessage(ChatColor.GREEN + "You’ve been teleported to the world spawn");

        return true;
    }
}
