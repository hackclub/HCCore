package com.hackclub.hccore.commands;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.ArgumentlessCommand;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
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
      sender.sendMessage(text("You must be a player to use this").color(RED));
      return true;
    }

    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      // Player needs to be in an Overworld
      if (player.getWorld().getEnvironment() != Environment.NORMAL) {
        sender.sendMessage(text("You can only use this command in the Overworld").color(RED));
        return true;
      }

      // Player needs to be within the allowed radius from spawn
      int distanceFromSpawn = (int) player.getLocation()
          .distance(player.getWorld().getSpawnLocation());
      int allowedRadius = this.plugin.getConfig().getInt("settings.spawn-command.allowed-radius");
      if (distanceFromSpawn > allowedRadius) {
        sender.sendMessage(text("You need to be within").color(RED).appendSpace()
            .append(text(allowedRadius).decorate(BOLD)).appendSpace()
            .append(text("blocks from spawn to use this command.")).appendNewline()
            .append(text("Currently, you’re")).appendSpace()
            .append(text((distanceFromSpawn - allowedRadius)).decorate(BOLD)).appendSpace()
            .append(text("blocks too far.")));
        return true;
      }

      // Player needs to be on the ground
      // doesn't affect too much if client can spoof it.
      if (!player.isOnGround()) {
        sender.sendMessage(
            text("You need to be standing on the ground to use this command").color(RED));
        return true;
      }

      // Player needs to have sky access (no blocks above them at all)
      Block highestBlock = player.getWorld().getHighestBlockAt(player.getLocation());
      if (player.getLocation().getY() < highestBlock.getY()) {
        sender.sendMessage(
            text("You need be directly under the sky to use this command").color(RED));
        return true;
      }

      // Can't be used while in damage cooldown period
      PlayerData data = this.plugin.getDataManager().getData(player);
      long currentTime = System.currentTimeMillis();
      long secondsSinceLastDamaged = (currentTime - data.getLastDamagedAt()) / 1000;
      int damageCooldown = this.plugin.getConfig().getInt("settings.spawn-command.damage-cooldown");
      if (secondsSinceLastDamaged < damageCooldown) {
        sender.sendMessage(
            text("You were hurt recently — you’ll be able to use this command in").color(RED)
                .appendSpace().append(text((damageCooldown - secondsSinceLastDamaged)))
                .appendSpace().append(text("seconds")));
        return true;
      }
    }

    player.teleport(player.getWorld().getSpawnLocation());
    sender.sendMessage(text("You’ve been teleported to the world spawn").color(GREEN));
    return true;
  }
}
