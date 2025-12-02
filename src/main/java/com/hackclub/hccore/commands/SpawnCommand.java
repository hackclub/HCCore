package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.ArgumentlessCommand;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.spawn.OverworldCommandMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnGroundMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnHurtMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnRadiusMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnSkyMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnTeleportMessage;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand extends ArgumentlessCommand implements CommandExecutor {

  private final HCCorePlugin plugin;

  public SpawnCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(MustBePlayerMessage.get());
      return true;
    }

    Location safeSpawn = player.getWorld().getSpawnLocation();
    int spawnX = (int) safeSpawn.getX();
    int spawnZ = (int) safeSpawn.getZ();
    World world = player.getWorld();

    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      // Player needs to be in an Overworld
      if (player.getWorld().getEnvironment() != Environment.NORMAL) {
        sender.sendMessage(OverworldCommandMessage.get());
        return true;
      }

      // Player needs to be within the allowed radius from spawn
      int distanceFromSpawn = (int) player.getLocation()
          .distance(player.getWorld().getSpawnLocation());
      int allowedRadius = this.plugin.getConfig().getInt("settings.spawn-command.allowed-radius");
      if (distanceFromSpawn > allowedRadius) {
        sender.sendMessage(
            SpawnRadiusMessage.get(allowedRadius, distanceFromSpawn - allowedRadius));
        return true;
      }

      // Player needs to be on the ground
      // doesn't affect too much if client can spoof it.
      if (!player.isOnGround()) {
        sender.sendMessage(SpawnGroundMessage.get());
        return true;
      }

      // Player needs to have sky access (no blocks above them at all)
      Block highestBlock = player.getWorld().getHighestBlockAt(player.getLocation());
      if (player.getLocation().getY() < highestBlock.getY()) {
        sender.sendMessage(SpawnSkyMessage.get());
        return true;
      }

      // Can't be used while in damage cooldown period
      PlayerData data = this.plugin.getDataManager().getData(player);
      long currentTime = System.currentTimeMillis();
      long secondsSinceLastDamaged = (currentTime - data.getLastDamagedAt()) / 1000;
      int damageCooldown = this.plugin.getConfig().getInt("settings.spawn-command.damage-cooldown");
      if (secondsSinceLastDamaged < damageCooldown) {
        sender.sendMessage(SpawnHurtMessage.get(damageCooldown - secondsSinceLastDamaged));
        return true;
      }

      // Finds a safe spawn location near the world spawn to counter holes/pits/etc
      Block highestSpawnBlock = world.getHighestBlockAt(spawnX, spawnZ);
      int safeY = -1;

      // Search downward from the highest block to find a safe spawn point
      for (int y = highestSpawnBlock.getY(); y > world.getMinHeight(); y--) {
        Block blockAtY = world.getBlockAt(spawnX, y, spawnZ);
        Block blockBelow = world.getBlockAt(spawnX, y - 1, spawnZ);
        Block blockAbove = world.getBlockAt(spawnX, y + 1, spawnZ);

        // make sure the block below is solid and not liquid (could be lava), and the block at Y and above are passable
        if (blockBelow.getType().isSolid() &&
            !blockBelow.isLiquid() &&
            blockAtY.isPassable() &&
            blockAbove.isPassable()) {
          safeY = y;
          break;
        }
      }

      // update safeSpawn if a safe Y was found
      if (safeY != -1) {
        safeSpawn = new Location(world, spawnX + 0.5, safeY, spawnZ + 0.5);
      }

      // otherwise, no safe spot found, just use the original spawn location
    }

    player.teleport(safeSpawn);
    sender.sendMessage(SpawnTeleportMessage.get());
    return true;
  }
}
