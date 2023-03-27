package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.slack.api.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
      sender.sendMessage(
          Component.text("Slack integration is not enabled").color(NamedTextColor.RED));
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    switch (args[0]) {
      case "link" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(
              Component.text("You must be a player to use this").color(NamedTextColor.RED));
          return true;
        }

        if (args.length != 2) {
          return false;
        }

        try {
          if (this.plugin.getSlackBot().getUserInfo(args[1]) == null) {
            player.sendMessage(
                Component.text("That Slack ID is invalid!").color(NamedTextColor.RED));
            return true;
          }

          boolean sent = this.plugin.getSlackBot().sendVerificationMessage(args[1], player.getName(), player.getUniqueId().toString());

          if (sent) {
            player.sendMessage(
                Component.text("A verification message has been sent to your Slack account")
                    .color(NamedTextColor.GREEN));
          } else {
            player.sendMessage(
                Component.text("An error occurred while sending the verification message")
                    .color(NamedTextColor.RED));
          }
        } catch (IOException e) {
          player.sendMessage(Component.text("An error occurred while linking your account")
              .color(NamedTextColor.RED));
          return true;
        }
        return true;
      }
      case "unlink" -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(
              Component.text("You must be a player to use this").color(NamedTextColor.RED));
          return true;
        }

        PlayerData playerData = this.plugin.getDataManager().getData(player);
        playerData.setSlackId(null);
        player.sendMessage(
            Component.text("Your Slack account has been unlinked").color(NamedTextColor.GREEN));
        return true;
      }
      case "lookup" -> {
        if (args.length != 2) {
          return false;
        }
        OfflinePlayer lookupPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (!lookupPlayer.hasPlayedBefore()) {
          sender.sendMessage(Component.text("That player has not played on this server!").color(NamedTextColor.RED));
          return true;
        }
        PlayerData lookupData = this.plugin.getDataManager().getData(lookupPlayer);
        String slackId = lookupData.getSlackId();
        if (slackId == null) {
          sender.sendMessage(Component.text("That player has not linked their Slack account")
              .color(NamedTextColor.RED));
          return true;
        }
        try {
          User slackUser = this.plugin.getSlackBot().getUserInfo(slackId);

          if (slackUser == null) {
            sender.sendMessage(Component.text("That player has not linked their Slack account")
                .color(NamedTextColor.RED));
            return true;
          }

          sender.sendMessage(Component.text(
                  "Slack username for %s: %s".formatted(lookupPlayer.getName(),
                      slackUser.getProfile().getDisplayName()))
              .color(NamedTextColor.GREEN));
        } catch (IOException e) {
          sender.sendMessage(Component.text("There was an error while looking up that player")
              .color(NamedTextColor.RED));
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
        List<String> subcommands = Arrays.asList("link", "unlink", "lookup");
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
