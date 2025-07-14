package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.spawn.OverworldCommandMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnGroundMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnHurtMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnRadiusMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnSkyMessage;
import com.hackclub.hccore.playermessages.spawn.SpawnTeleportMessage;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

@RegisteredCommand
public class SpawnCommand extends AbstractCommand {

  @SuppressWarnings("deprecation")
  @Command("spawn")
  public void execute(
      final @NotNull Player sender
  ) {
    if (sender.getGameMode() == GameMode.SURVIVAL || sender.getGameMode() == GameMode.ADVENTURE) {
      // Player needs to be in an Overworld
      if (sender.getWorld().getEnvironment() != Environment.NORMAL) {
        sender.sendMessage(OverworldCommandMessage.get());
        return;
      }

      // Player needs to be within the allowed radius from spawn
      int distanceFromSpawn = (int) sender.getLocation()
          .distance(sender.getWorld().getSpawnLocation());
      int allowedRadius = this.plugin.getConfig().getInt("settings.spawn-command.allowed-radius");
      if (distanceFromSpawn > allowedRadius) {
        sender.sendMessage(
            SpawnRadiusMessage.get(allowedRadius, distanceFromSpawn - allowedRadius));
        return;
      }

      // Player needs to be on the ground
      // doesn't affect too much if client can spoof it.
      if (!sender.isOnGround()) {
        sender.sendMessage(SpawnGroundMessage.get());
        return;
      }

      // Player needs to have sky access (no blocks above them at all)
      Block highestBlock = sender.getWorld().getHighestBlockAt(sender.getLocation());
      if (sender.getLocation().getY() < highestBlock.getY()) {
        sender.sendMessage(SpawnSkyMessage.get());
        return;
      }

      // Can't be used while in damage cooldown period
      PlayerData data = this.plugin.getDataManager().getData(sender);
      long currentTime = System.currentTimeMillis();
      long secondsSinceLastDamaged = (currentTime - data.getLastDamagedAt()) / 1000;
      int damageCooldown = this.plugin.getConfig().getInt("settings.spawn-command.damage-cooldown");
      if (secondsSinceLastDamaged < damageCooldown) {
        sender.sendMessage(SpawnHurtMessage.get(damageCooldown - secondsSinceLastDamaged));
        return;
      }
    }

    sender.teleport(sender.getWorld().getSpawnLocation());
    sender.sendMessage(SpawnTeleportMessage.get());
  }
}
