package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    if (!(sender instanceof Player player)) {
      sender.sendMessage(
          Component.text("You must be a player to use this").color(NamedTextColor.RED));
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    if (args[0].equals("link")) {
      if (args.length != 2) {
        return false;
      }

      try {
        if (!this.plugin.getSlackBot().isUserIdValid(args[1])) {
          player.sendMessage(Component.text("That Slack ID is invalid!").color(NamedTextColor.RED));
          return true;
        }

        this.plugin.getSlackBot().sendVerificationMessage(args[1], player.getName());

        player.sendMessage(
            Component.text("A verification message has been sent to your Slack account")
                .color(NamedTextColor.GREEN));
      } catch (IOException e) {
        player.sendMessage(Component.text("An error occurred while linking your account")
            .color(NamedTextColor.RED));
        return true;
      }

      return true;
    } else if (args[0].equals("unlink")) {
      PlayerData playerData = this.plugin.getDataManager().getData(player);
      playerData.setSlackId(null);
      player.sendMessage(
          Component.text("Your Slack account has been unlinked").color(NamedTextColor.GREEN));
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return null;
    }

    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
        List<String> subcommands = Arrays.asList("link", "unlink");
        StringUtil.copyPartialMatches(args[0], subcommands, completions);
    }

    Collections.sort(completions);
    return completions;
  }
}
