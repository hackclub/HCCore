package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFKCommand implements CommandExecutor {

  private final HCCorePlugin plugin;

  public AFKCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatColor.RED + "You must be a player to use this");
      return true;
    }

    // Toggle player's AFK status
    PlayerData data = this.plugin.getDataManager().getData(player);
    data.setAfk(!data.isAfk());

    return true;
  }
}
