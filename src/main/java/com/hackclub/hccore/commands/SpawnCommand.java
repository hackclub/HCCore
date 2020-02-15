package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
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
            player.sendMessage(ChatColor.RED + "You can only use this command in the Overworld");
            return true;
        }

        // Player needs to be on the ground
        if (!player.isOnGround()) {
            player.sendMessage(
                    ChatColor.RED + "You need to be standing on the ground to use this command");
            return true;
        }

        // Can't be used while in damage cooldown period
        PlayerData data = this.plugin.getDataManager().getData(player);
        long currentTime = System.currentTimeMillis();
        long secondsSinceLastDamaged = (currentTime - data.getLastDamagedAt()) / 1000;
        final int DAMAGE_WAIT_TIME = 20;
        if (secondsSinceLastDamaged < DAMAGE_WAIT_TIME) {
            player.sendMessage(
                    ChatColor.RED + "You were hurt recently—you’ll be able to use this command in "
                            + (DAMAGE_WAIT_TIME - secondsSinceLastDamaged) + " seconds");
            return true;
        }

        Location spawnLocation = this.plugin.getServer().getWorld("world").getSpawnLocation();
        player.teleport(spawnLocation);
        player.sendMessage(ChatColor.GREEN + "You’ve been teleported to spawn");

        return true;
    }
}
