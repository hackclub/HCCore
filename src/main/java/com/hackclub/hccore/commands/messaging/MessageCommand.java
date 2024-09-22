package com.hackclub.hccore.commands.messaging;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playermessages.messages.PrivateMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageCommand implements CommandExecutor {

  private HCCorePlugin plugin;

  public MessageCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length < 2) return false;

    //TODO: Allow console to send messages as "Console" in red.
    if (!(sender instanceof Player sendingPlayer)) {
      sender.sendMessage(MustBePlayerMessage.get());
      return true;
    }

    final Player recipientPlayer = sender.getServer().getPlayerExact(args[0]);

    if (recipientPlayer == null) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return true;
    }

    final Component senderMessage = PrivateMessage.get("You", recipientPlayer.getName(), String.join(" ", args).substring(args[0].length() + 1), null, this.plugin.getDataManager().getData(recipientPlayer).getNameColor());
    final Component receiverMessage = PrivateMessage.get(sendingPlayer.getName(), "You", String.join(" ", args).substring(args[0].length() + 1), this.plugin.getDataManager().getData(sendingPlayer).getNameColor(), null);

    this.plugin.getDataManager().getData(recipientPlayer).setLastPlayerChattingWith(sendingPlayer.getUniqueId());
    this.plugin.getDataManager().getData(sendingPlayer).setLastPlayerChattingWith(recipientPlayer.getUniqueId());

    recipientPlayer.sendMessage(receiverMessage);
    sendingPlayer.sendMessage(senderMessage);

    return true;
  }
}
