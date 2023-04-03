package com.hackclub.hccore.commands;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playerMessages.LinkedSlackMessage;
import com.hackclub.hccore.playerMessages.UnlinkedSlackMessage;
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
      sender.sendMessage(text("Slack integration is not enabled").color(RED));
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    switch (args[0]) {
      case "info" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(text("You must be a player to use this").color(RED));
          return true;
        }
        PlayerData playerData = this.plugin.getDataManager().getData(player);
        String slackId = playerData.getSlackId();
        if (slackId == null) {
          // unlinked, show slack join + link info
          sender.sendMessage(UnlinkedSlackMessage.get());

        } else {
          // linked, show paired account info
          sender.sendMessage(LinkedSlackMessage.get(player)); //TODO
        }

        return true;
      }
      case "link" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(text("You must be a player to use this").color(RED));
          return true;
        }

        if (args.length != 2) {
          return false;
        }

        try {
          if (this.plugin.getSlackBot().getUserInfo(args[1]) == null) {
            player.sendMessage(text("That Slack ID is invalid!").color(RED));
            return true;
          }

          boolean sent = this.plugin.getSlackBot()
              .sendVerificationMessage(args[1], player.getName(), player.getUniqueId().toString());

          if (sent) {
            player.sendMessage(
                text("A verification message has been sent to your Slack account").color(GREEN));
          } else {
            player.sendMessage(
                text("An error occurred while sending the verification message").color(RED));
          }
        } catch (IOException e) {
          player.sendMessage(text("An error occurred while linking your account").color(RED));
          return true;
        }
        return true;
      }
      case "unlink" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(text("You must be a player to use this").color(RED));
          return true;
        }

        PlayerData playerData = this.plugin.getDataManager().getData(player);
        playerData.setSlackId(null);
        player.sendMessage(
            text("Your Slack account has been unlinked").color(GREEN));
        return true;
      }
      case "lookup" -> {
        if (args.length != 2) {
          return false;
        }
        OfflinePlayer lookupPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (!lookupPlayer.hasPlayedBefore()) {
          sender.sendMessage(text("That player has not played on this server!").color(RED));
          return true;
        }
        PlayerData lookupData = this.plugin.getDataManager().getData(lookupPlayer);
        String slackId = lookupData.getSlackId();
        if (slackId == null) {
          sender.sendMessage(text("That player has not linked their Slack account").color(RED));
          return true;
        }
        try {
          User slackUser = this.plugin.getSlackBot().getUserInfo(slackId);

          if (slackUser == null) {
            sender.sendMessage(text("That player has not linked their Slack account").color(RED));
            return true;
          }

          sender.sendMessage(text("Slack username for %s: %s".formatted(lookupPlayer.getName(),
              slackUser.getProfile().getDisplayName())).color(GREEN));
        } catch (IOException e) {
          sender.sendMessage(text("There was an error while looking up that player").color(RED));
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
