package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playerMessages.MustBePlayerMessage;
import com.hackclub.hccore.playerMessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playerMessages.stats.IncludePlayerStatMessage;
import com.hackclub.hccore.playerMessages.stats.InvalidStatMessage;
import com.hackclub.hccore.playerMessages.stats.SpecificStatMessage;
import com.hackclub.hccore.playerMessages.stats.StatMessage;
import com.hackclub.hccore.playerMessages.stats.UnsupportedStatMessage;
import com.hackclub.hccore.playerMessages.stats.YourStatsMessage;
import com.hackclub.hccore.utils.TimeUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements TabExecutor {

  private static final List<String> STATISTIC_NAMES = Arrays.stream(Statistic.values())
      .map(statistic -> statistic.name().toLowerCase()).toList();

  private final HCCorePlugin plugin;

  public StatsCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    boolean extended = false;

    // /stats
    if (args.length == 0) {
      if (sender instanceof Player) {
        sender.sendMessage(YourStatsMessage.get("Your"));
        this.sendStatistics(sender, (Player) sender, false);
      } else {
        sender.sendMessage(MustBePlayerMessage.get());
      }
      return true;
    }

    if (args.length > 1) {
      switch (args[1].toLowerCase()) {
        case "extended" -> // /stats <player> extended
            extended = true;
        case "only" -> { // /stats <player> only <statistic>
          if (args.length < 3) {
            sender.sendMessage(IncludePlayerStatMessage.get());
            return true;
          }
          if (!STATISTIC_NAMES.contains(args[2].toLowerCase())) {
            sender.sendMessage(InvalidStatMessage.get());
            return true;
          }
          Statistic specificStat = Statistic.valueOf(args[2].toUpperCase());
          if (specificStat.isSubstatistic()) {
            sender.sendMessage(UnsupportedStatMessage.get());
            // TODO support it(?)
            return true;
          }
          Player player = sender.getServer().getPlayerExact(args[0]);
          if (player != null) {
            PlayerData data = this.plugin.getDataManager().getData(player);
            sender.sendMessage(SpecificStatMessage.get(data.getUsableName(), args[2].toLowerCase(),
                String.valueOf(player.getStatistic(specificStat))));
          } else {
            sender.sendMessage(NoOnlinePlayerMessage.get());
          }
          return true;
        }
        default -> {
          return false;
        }
      }
    }

    // /stats <player>
    Player targetPlayer = sender.getServer().getPlayerExact(args[0]);
    if (targetPlayer != null) {
      PlayerData data = this.plugin.getDataManager().getData(targetPlayer);
      sender.sendMessage(YourStatsMessage.get(data.getUsableName()));
      this.sendStatistics(sender, targetPlayer, extended);
    } else {
      sender.sendMessage(NoOnlinePlayerMessage.get());
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    List<String> completions = new ArrayList<>();

    switch (args.length) {
      case 1 -> {
        for (Player player : sender.getServer().getOnlinePlayers()) {
          if (StringUtil.startsWithIgnoreCase(player.getName(), args[0])) {
            completions.add(player.getName());
          }
        }
      }
      case 2 -> {
        List<String> subcommands = Arrays.asList("extended", "only");
        StringUtil.copyPartialMatches(args[1], subcommands, completions);
      }
      case 3 -> {
        // Only send statistic name suggestions in /stats <player> only
        if (!args[1].equalsIgnoreCase("only")) {
          break;
        }
        for (Statistic statistic : Statistic.values()) {
          if (StringUtil.startsWithIgnoreCase(statistic.name(), args[2])) {
            completions.add(statistic.name().toLowerCase());
          }
        }
      }
    }

    Collections.sort(completions);
    return completions;
  }

  private void sendStatistics(CommandSender sender, Player player, Boolean extended) {
    sender.sendMessage(
        StatMessage.get("Deaths", String.valueOf(player.getStatistic(Statistic.DEATHS))));
    sender.sendMessage(
        StatMessage.get("Mob kills", String.valueOf(player.getStatistic(Statistic.MOB_KILLS))));
    sender.sendMessage(StatMessage.get("Player kills",
        String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS))));
    sender.sendMessage(
        StatMessage.get("Time played",
            TimeUtil.toPrettyTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE))));
    sender.sendMessage(StatMessage.get("Time since last death", TimeUtil.toPrettyTime(
        player.getStatistic(Statistic.TIME_SINCE_DEATH))));

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sender.sendMessage(
        StatMessage.get("Registered since", dateFormat.format(new Date(player.getFirstPlayed()))));

    if (extended) {
      sender.sendMessage(
          StatMessage.get("Distance by elytra",
              toSIPrefix(player.getStatistic(Statistic.AVIATE_ONE_CM))
                  + "m"));
      sender.sendMessage(
          StatMessage.get("Distance by minecart",
              toSIPrefix(player.getStatistic(Statistic.MINECART_ONE_CM))
                  + "m"));
      sender.sendMessage(
          StatMessage.get("Distance by horse",
              toSIPrefix(player.getStatistic(Statistic.HORSE_ONE_CM)) + "m"));
      sender.sendMessage(
          StatMessage.get("Distance walked",
              toSIPrefix(player.getStatistic(Statistic.WALK_ONE_CM)) + "m"));
      sender.sendMessage(StatMessage.get("Damage taken",
          String.valueOf(player.getStatistic(Statistic.DAMAGE_TAKEN))));
      sender.sendMessage(StatMessage.get("Damage dealt",
          String.valueOf(player.getStatistic(Statistic.DAMAGE_DEALT))));
      sender.sendMessage(
          StatMessage.get("Times jumped", String.valueOf(player.getStatistic(Statistic.JUMP))));
      sender.sendMessage(
          StatMessage.get("Raids won", String.valueOf(player.getStatistic(Statistic.RAID_WIN))));
      sender.sendMessage(
          StatMessage.get("Diamonds picked up",
              String.valueOf(player.getStatistic(Statistic.PICKUP, Material.DIAMOND))));
    }
  }

  // converts numbers to their SI prefix laden counterparts
  private static String toSIPrefix(double number) {
    if (number < 100) {
      return number + " c";
    } else if (number < 100000) {
      number = Math.round(number / 100);
      return String.valueOf(number);
    } else if (number >= 100000) {
      // Divides by 1000 to allow for two significant digits
      number = Math.round(number / 1000);
      // Divides by 100 to finally get to km
      number /= 100;
      return String.format("%.2f k", number);
    }
    return null;
  }
}
