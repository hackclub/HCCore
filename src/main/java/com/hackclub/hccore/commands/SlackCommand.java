package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.PlayerHasntPlayedMessage;
import com.hackclub.hccore.playermessages.slack.InvalidSlackMessage;
import com.hackclub.hccore.playermessages.slack.LinkErrorMessage;
import com.hackclub.hccore.playermessages.slack.LinkedSlackMessage;
import com.hackclub.hccore.playermessages.slack.LookupErrorMessage;
import com.hackclub.hccore.playermessages.slack.PlayerNotLinkedMessage;
import com.hackclub.hccore.playermessages.slack.SlackDisabledMessage;
import com.hackclub.hccore.playermessages.slack.SlackUsernameMessage;
import com.hackclub.hccore.playermessages.slack.UnlinkedSlackMessage;
import com.hackclub.hccore.playermessages.slack.VerificationErrorMessage;
import com.hackclub.hccore.playermessages.slack.VerificationSentMessage;
import com.slack.api.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class SlackCommand implements TabExecutor {

  private final HCCorePlugin plugin;

  public SlackCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (this.plugin.getSlackBot() == null) {
      sender.sendMessage(SlackDisabledMessage.get());
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    switch (args[0]) {
      case "info" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(MustBePlayerMessage.get());
          return true;
        }
        PlayerData playerData = this.plugin.getDataManager().getData(player);
        String slackId = playerData.getSlackId();
        if (slackId == null) {
          // unlinked, show slack join + link info
          sender.sendMessage(UnlinkedSlackMessage.get());

        } else {
          // linked, show paired account info
          player.sendMessage(LinkedSlackMessage.get(player));
        }

        return true;
      }
      case "link" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(MustBePlayerMessage.get());
          return true;
        }

        if (args.length != 2) {
          return false;
        }

        try {
          if (this.plugin.getSlackBot().getUserInfo(args[1]) == null) {
            player.sendMessage(InvalidSlackMessage.get());
            return true;
          }

          boolean sent = this.plugin.getSlackBot()
              .sendVerificationMessage(args[1], player.getName(), player.getUniqueId().toString());

          if (sent) {
            player.sendMessage(VerificationSentMessage.get());
          } else {
            player.sendMessage(VerificationErrorMessage.get());
          }
        } catch (IOException e) {
          player.sendMessage(LinkErrorMessage.get());
          return true;
        }
        return true;
      }
      case "lookup" -> {
        if (args.length != 2) {
          return false;
        }
        OfflinePlayer lookupPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (!lookupPlayer.hasPlayedBefore()) {
          sender.sendMessage(PlayerHasntPlayedMessage.get());
          return true;
        }
        PlayerData lookupData = this.plugin.getDataManager().getData(lookupPlayer);
        String slackId = lookupData.getSlackId();
        if (slackId == null) {
          sender.sendMessage(PlayerNotLinkedMessage.get());
          return true;
        }
        try {
          User slackUser = this.plugin.getSlackBot().getUserInfo(slackId);

          if (slackUser == null) {
            sender.sendMessage(PlayerNotLinkedMessage.get());
            return true;
          }

          sender.sendMessage(SlackUsernameMessage.get(lookupPlayer.getName(),
              slackUser.getProfile().getDisplayName()));
        } catch (IOException e) {
          sender.sendMessage(LookupErrorMessage.get());
          return true;
        }
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return null;
    }

    List<String> completions = new ArrayList<>();
    switch (args.length) {
      case 1 -> {
        List<String> subcommands = Arrays.asList("info", "link", "unlink", "lookup");
        StringUtil.copyPartialMatches(args[0], subcommands, completions);
      }
      case 2 -> {
        if (args[0].equalsIgnoreCase("lookup")) {
          for (Player player : sender.getServer().getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(player.getName(), args[1])) {
              completions.add(player.getName());
            }
          }
        }
      }
    }

    Collections.sort(completions);
    return completions;
  }
}
