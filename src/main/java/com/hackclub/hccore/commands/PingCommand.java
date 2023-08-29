package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playerMessages.MustBePlayerMessage;
import com.hackclub.hccore.playerMessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playerMessages.ping.PingFailMessage;
import com.hackclub.hccore.playerMessages.ping.PingMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements TabExecutor {

  private final HCCorePlugin plugin;

  public PingCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    // /ping
    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage(MustBePlayerMessage.get());
        return true;
      }

      int ping = player.getPing();
      // Failed for some reason
      if (ping == -1) {
        sender.sendMessage(PingFailMessage.get("your"));
        return true;
      }

      sender.sendMessage(PingMessage.get("Your", ping));
      return true;
    }

    // /ping [player]
    Player targetPlayer = sender.getServer().getPlayerExact(args[0]);
    if (targetPlayer != null) {
      PlayerData data = this.plugin.getDataManager().getData(targetPlayer);
      int ping = targetPlayer.getPing();
      // Failed for some reason
      if (ping == -1) {
        sender.sendMessage(PingFailMessage.get(data.getUsableName() + "'s"));
        return true;
      }

      sender.sendMessage(PingMessage.get(data.getUsableName() + "'s", ping));
    } else {
      sender.sendMessage(NoOnlinePlayerMessage.get());
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
      for (Player player : sender.getServer().getOnlinePlayers()) {
        if (StringUtil.startsWithIgnoreCase(player.getName(), args[0])) {
          completions.add(player.getName());
        }
      }
    }

    Collections.sort(completions);
    return completions;
  }
}
