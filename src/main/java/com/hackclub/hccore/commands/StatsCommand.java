package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.stats.IncludePlayerStatMessage;
import com.hackclub.hccore.playermessages.stats.SpecificStatMessage;
import com.hackclub.hccore.playermessages.stats.StatMessage;
import com.hackclub.hccore.playermessages.stats.UnsupportedStatMessage;
import com.hackclub.hccore.playermessages.stats.YourStatsMessage;
import com.hackclub.hccore.utils.TimeUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsCommand extends AbstractCommand {

  @Command("stats [player] [option] [statistic]")
  public void execute(
      final @NotNull CommandSender sender,
      final @Nullable Player target,
      final @Nullable Option option,
      final @Nullable Statistic statistic
  ) {
    boolean extended = false;

    // /stats
    if (target == null) {
      if (sender instanceof Player) {
        sender.sendMessage(YourStatsMessage.get("Your"));
        this.sendStatistics(sender, (Player) sender, false);
      } else {
        sender.sendMessage(MustBePlayerMessage.get());
      }

      return;
    }

    if (option != null) {
      switch (option) {
        case EXTENDED -> // /stats <player> extended
            extended = true;
        case ONLY -> { // /stats <player> only <statistic>
          if (statistic == null) {
            sender.sendMessage(IncludePlayerStatMessage.get());
            return;
          }

          if (statistic.isSubstatistic()) {
            sender.sendMessage(UnsupportedStatMessage.get());
            // TODO support it(?)
            return;
          }
          PlayerData data = this.plugin.getDataManager().getData(target);
          sender.sendMessage(SpecificStatMessage.get(data.getUsableName(), option.name().toLowerCase(),
              String.valueOf(target.getStatistic(statistic))));
        }
      }
    } else {
      PlayerData data = this.plugin.getDataManager().getData(target);
      sender.sendMessage(YourStatsMessage.get(data.getUsableName()));
      this.sendStatistics(sender, target, extended);
    }
  }

  public enum Option {
    EXTENDED,ONLY
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
