package com.hackclub.hccore.commands.messaging;

import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.enums.Color;
import com.hackclub.hccore.playermessages.messages.PrivateMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

public class MessageCommand extends AbstractCommand {

  @Command("message|msg|w|whisper <player> <message>")
  public void execute(
      final @NotNull CommandSender sender,
      final @NotNull @Argument("player") Player target,
      final @NotNull @Argument("message") @Greedy String message
  ) {
    if (sender instanceof Player) {
      final TextColor senderColor = dataManager.getData((Player) sender).getNameColor();
      final TextColor targetColor = dataManager.getData(target).getNameColor();

      final Component senderMessage = PrivateMessage.get("You", target.getName(), message, null, targetColor);
      final Component receiverMessage = PrivateMessage.get(sender.getName(), "You", message, senderColor, null);

      plugin.getDataManager().getData(target).setLastPlayerChattingWith(((Player) sender).getUniqueId());
      plugin.getDataManager().getData((Player) sender).setLastPlayerChattingWith(target.getUniqueId());

      sender.sendMessage(senderMessage);
      target.sendMessage(receiverMessage);
    } else {
      final TextColor targetColor = dataManager.getData(target).getNameColor();

      final Component senderMessage = PrivateMessage.get("You", target.getName(), message, null, targetColor);
      final Component receiverMessage = PrivateMessage.get("Console", "You", message, Color.RED.getTextColor(), null);

      sender.sendMessage(senderMessage);
      target.sendMessage(receiverMessage);
    }
  }
}
