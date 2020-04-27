package com.hackclub.hccore.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.utils.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class StatsCommand implements TabExecutor {
    private final HCCorePlugin plugin;

    public StatsCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        // set extended to false by default
        boolean extended = false;
        // initialize specificStat variable
        Statistic specificStat = null;

        ArrayList<String> STATISTIC_NAMES = new ArrayList<String>();

        for (Statistic stat : Statistic.values()) {
            STATISTIC_NAMES.add(stat.name());
        }



        if ((args.length == 0) || (args[0].contains("extended"))) {
            if ((args.length == 0)) {
                extended = false;
            } else if (args[0].contains("extended")) {
                extended = true;
            }
            if (sender instanceof Player) {
                sender.sendMessage("Your stats:");
                this.sendStatistics(sender, (Player) sender, extended);
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            }
            return true;
        }

        if (args.length > 1) {
            switch (args[1]) {

                case "extended": // /stats [player] extended
                    extended = true;
                    break;

                case "only": // /stats [player] only [Statistic]
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED
                                + "You must include both a statistic and a player name");
                        return true;
                    }
                    if (!(STATISTIC_NAMES.contains(args[2].toUpperCase()))) {
                        sender.sendMessage(ChatColor.RED + "Not a valid statistic");
                        return true;
                    }
                    specificStat = Statistic.valueOf(args[2].toUpperCase());
                    if (specificStat.isSubstatistic()) {
                        sender.sendMessage(
                                ChatColor.RED + "This statistic is not currently supported");
                        return true;
                    }

                    Player player = sender.getServer().getPlayerExact(args[0]);
                    if (player != null) {
                        PlayerData data = this.plugin.getDataManager().getData(player);
                        sender.sendMessage(data.getUsableName() + "’s stat:");
                        sender.sendMessage(
                                specificStat + " = " + player.getStatistic(specificStat));
                    } else {
                        sender.sendMessage(
                                ChatColor.RED + "No online player with that name was found");
                    }
                    return true;

                default:
                    return false;

            }

        } else {
            extended = false;
        }

        // /stats [player]
        Player targetPlayer = sender.getServer().getPlayerExact(args[0]);
        if (targetPlayer != null) {
            PlayerData data = this.plugin.getDataManager().getData(targetPlayer);
            sender.sendMessage(data.getUsableName() + "’s stats:");
            this.sendStatistics(sender, targetPlayer, extended);
        } else {
            sender.sendMessage(ChatColor.RED + "No online player with that name was found");
        }

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias,
            String[] args) {
        List<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (StringUtil.startsWithIgnoreCase(player.getName(), args[0])) {
                    completions.add(player.getName());
                }
            }
            completions.add("extended");
        }

        if (args.length == 2) {
            completions.add("extended");
            completions.add("only");
        }
        if ((args.length == 3)) {
            for (Statistic statistic : Statistic.values()) {
                if (StringUtil.startsWithIgnoreCase(statistic.name(), args[2])) {
                    completions.add(statistic.name().toLowerCase());
                }
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private void sendStatistics(CommandSender sender, Player player, Boolean extended) {
        sender.sendMessage("- Deaths: " + player.getStatistic(Statistic.DEATHS));
        sender.sendMessage("- Mob kills: " + player.getStatistic(Statistic.MOB_KILLS));
        sender.sendMessage("- Player kills: " + player.getStatistic(Statistic.PLAYER_KILLS));
        sender.sendMessage("- Time played: "
                + TimeUtil.toPrettyTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE)));
        sender.sendMessage("- Time since last death: "
                + TimeUtil.toPrettyTime(player.getStatistic(Statistic.TIME_SINCE_DEATH)));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sender.sendMessage(
                "- Registered since: " + dateFormat.format(new Date(player.getFirstPlayed())));

        if (extended) {
            sender.sendMessage("- Distance by elytra: "
                    + formatStatistic((double) player.getStatistic(Statistic.AVIATE_ONE_CM)) + "m");
            sender.sendMessage("- Distance by minecart: "
                    + formatStatistic((double) player.getStatistic(Statistic.MINECART_ONE_CM))
                    + "m");
            sender.sendMessage("- Distance walked: "
                    + formatStatistic((double) player.getStatistic(Statistic.WALK_ONE_CM)) + "m");
            sender.sendMessage("- Damage taken: " + player.getStatistic(Statistic.DAMAGE_TAKEN));
            sender.sendMessage("- Damage dealt: " + player.getStatistic(Statistic.DAMAGE_DEALT));
            sender.sendMessage("- Times jumped: " + player.getStatistic(Statistic.JUMP));
            sender.sendMessage("- Raids won: " + player.getStatistic(Statistic.RAID_WIN));
            sender.sendMessage("- Diamonds gained: "
                    + player.getStatistic(Statistic.PICKUP, Material.DIAMOND));

        }
    }

    private String formatStatistic(double stat) {
        if (stat < 100) {
            return (String.valueOf(stat) + " c");

        } else if (stat < 100000) {
            stat = Math.round(stat / 100);
            return (String.valueOf(stat));

        } else if (stat >= 100000) {
            // Divides by 1000 to allow for two significant digits
            stat = Math.round(stat / 1000);
            // Divides by 100 to finally get to km
            stat = (stat / 100);
            return (String.format("%,.2f k", stat));

        }
        return null;
    }
}
