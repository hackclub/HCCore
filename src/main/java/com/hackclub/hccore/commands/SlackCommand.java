package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.PlayerHasntPlayedMessage;
import com.hackclub.hccore.playermessages.slack.InvalidSlackMessage;
import com.hackclub.hccore.playermessages.slack.LinkErrorMessage;
import com.hackclub.hccore.playermessages.slack.LinkedSlackMessage;
import com.hackclub.hccore.playermessages.slack.LookupErrorMessage;
import com.hackclub.hccore.playermessages.slack.PlayerNotLinkedMessage;
import com.hackclub.hccore.playermessages.slack.SlackUsernameMessage;
import com.hackclub.hccore.playermessages.slack.UnlinkedSlackMessage;
import com.hackclub.hccore.playermessages.slack.VerificationErrorMessage;
import com.hackclub.hccore.playermessages.slack.VerificationSentMessage;
import com.slack.api.model.User;
import java.io.IOException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
@Command("slack")
public class SlackCommand extends AbstractCommand {

  @Command("link [slackid]")
  public void executeLink(
      final @NotNull Player sender,
      final @Nullable @Argument("slackid") String slackid
  ) {
    try {
      if (this.plugin.getSlackBot().getUserInfo(slackid) == null) {
        sender.sendMessage(InvalidSlackMessage.get());
        return;
      }

      boolean sent = this.plugin.getSlackBot()
          .sendVerificationMessage(slackid, sender.getName(), sender.getUniqueId().toString());

      if (sent) {
        sender.sendMessage(VerificationSentMessage.get());
      } else {
        sender.sendMessage(VerificationErrorMessage.get());
      }
    } catch (IOException e) {
      sender.sendMessage(LinkErrorMessage.get());
      return;
    }
  }

  @Command("info")
  public void executeInfo(
      final @NotNull Player sender
  ) {
    PlayerData playerData = this.plugin.getDataManager().getData(sender);
    String slackId = playerData.getSlackId();
    if (slackId == null) {
      // unlinked, show slack join + link info
      sender.sendMessage(UnlinkedSlackMessage.get());

    } else {
      // linked, show paired account info
      sender.sendMessage(LinkedSlackMessage.get(sender));
    }
  }

  @Command("lookup <player>")
  public void executeLookup(
      final @NotNull CommandSender sender,
      final @NotNull @Argument("player") OfflinePlayer player
  ) {
    if (!player.hasPlayedBefore()) {
      sender.sendMessage(PlayerHasntPlayedMessage.get());
      return;
    }
    PlayerData lookupData = this.plugin.getDataManager().getData(player);
    String slackId = lookupData.getSlackId();
    if (slackId == null) {
      sender.sendMessage(PlayerNotLinkedMessage.get());
      return;
    }
    try {
      User slackUser = this.plugin.getSlackBot().getUserInfo(slackId);

      if (slackUser == null) {
        sender.sendMessage(PlayerNotLinkedMessage.get());
        return;
      }

      sender.sendMessage(SlackUsernameMessage.get(player.getName(),
          slackUser.getProfile().getDisplayName()));
    } catch (IOException e) {
      sender.sendMessage(LookupErrorMessage.get());
    }
  }
}
