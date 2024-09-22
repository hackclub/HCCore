package com.hackclub.hccore.commands.messaging;

import com.comphenix.protocol.PacketType.Play;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playermessages.messages.PrivateMessage;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReplyCommand implements CommandExecutor {

  private HCCorePlugin plugin;

  public ReplyCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length < 1) return false;

    //TODO: Allow console to send messages as "Console" in red.
    if (!(sender instanceof Player sendingPlayer)) {
      sender.sendMessage(MustBePlayerMessage.get());
      return true;
    }

    Player recipientPlayer;
    try {
      recipientPlayer = sender.getServer().getPlayer(this.plugin.getDataManager().getData(sendingPlayer).getLastPlayerChattingWith());
    } catch (Exception e) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return true;
    }

    if (recipientPlayer == null) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return true;
    }

    final Component senderMessage = PrivateMessage.get("You", recipientPlayer.getName(), String.join(" ", args), null, this.plugin.getDataManager().getData(recipientPlayer).getNameColor());
    final Component receiverMessage = PrivateMessage.get(sendingPlayer.getName(), "You", String.join(" ", args), this.plugin.getDataManager().getData(sendingPlayer).getNameColor(), null);

    this.plugin.getDataManager().getData(recipientPlayer).setLastPlayerChattingWith(sendingPlayer.getUniqueId());
    this.plugin.getDataManager().getData(sendingPlayer).setLastPlayerChattingWith(recipientPlayer.getUniqueId());

    recipientPlayer.sendMessage(receiverMessage);
    sendingPlayer.sendMessage(senderMessage);

    return true;
  }
}
