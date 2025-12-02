package com.hackclub.hccore.commands.messaging;

import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playermessages.messages.PrivateMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

@RegisteredCommand
public class ReplyCommand extends AbstractCommand {

  @Command("r|reply <message>")
  public void execute(
      final @NotNull Player sender,
      final @NotNull @Argument("message") @Greedy String message
  ) {
    Player recipientPlayer;
    try {
      recipientPlayer = sender.getServer().getPlayer(this.plugin.getDataManager().getData(sender).getLastPlayerChattingWith());
    } catch (Exception e) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return;
    }

    if (recipientPlayer == null) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return;
    }

    final TextColor senderColor = dataManager.getData(sender).getNameColor();
    final TextColor recipientColor = dataManager.getData(recipientPlayer).getNameColor();

    final Component senderMessage = PrivateMessage.get("You", recipientPlayer.getName(), message, null, recipientColor);
    final Component receiverMessage = PrivateMessage.get(sender.getName(), "You", message, senderColor, null);

    plugin.getDataManager().getData(recipientPlayer).setLastPlayerChattingWith((sender).getUniqueId());
    plugin.getDataManager().getData(sender).setLastPlayerChattingWith(recipientPlayer.getUniqueId());

    sender.sendMessage(senderMessage);
    recipientPlayer.sendMessage(receiverMessage);
  }
}
