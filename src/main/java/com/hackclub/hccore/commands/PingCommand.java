package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.ping.PingFailMessage;
import com.hackclub.hccore.playermessages.ping.PingMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class PingCommand extends AbstractCommand {

  @Command("ping [player]")
  public void execute(
      final @NotNull CommandSender sender,
      final @Nullable @Argument("player") Player target
  ) {
    if (target == null) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage(MustBePlayerMessage.get());
        return;
      }
      int ping = player.getPing();
      // Failed for some reason
      if (ping == -1) {
        sender.sendMessage(PingFailMessage.get("your"));
        return;
      }

      sender.sendMessage(PingMessage.get("Your", ping));
      return;
    }

    PlayerData data = this.plugin.getDataManager().getData(target);
    int ping = target.getPing();
    // Failed for some reason
    if (ping == -1) {
      sender.sendMessage(PingFailMessage.get(data.getUsableName() + "'s"));
      return;
    }

    sender.sendMessage(PingMessage.get(data.getUsableName() + "'s", ping));
  }
}
