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

    // set extended to false by default
    Boolean extended = false;
    // initialize specificStat variable
    String specificStat = "";


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        // /stats [player] [tag]
        Player targetPlayer = null;

        if ((args.length == 0)) {
            extended = false;
            if (sender instanceof Player) {
                sender.sendMessage("Your stats:");
                this.sendStatistics(sender, (Player) sender, extended);
            } else {
                sender.sendMessage("You must be a player to use this");
            }
            return true;

        }
        if ((args.length == 1) && (args[0] == "extended")) {
            extended = true;
            if (sender instanceof Player) {
                sender.sendMessage("Your stats:");
                this.sendStatistics(sender, (Player) sender, extended);
            } else {
                sender.sendMessage("You must be a player to use this");
            }
            return true;

        }

        switch (args[1]) {

            case "extended": // /stats [player] extended
                extended = true;
                targetPlayer = sender.getServer().getPlayerExact(args[0]);
                break;


            case "only": // /stats [player] only [Statistic]
                if (args.length != 3) {
                    return false;

                }
                specificStat = args[2];
                Player player = sender.getServer().getPlayerExact(args[0]);
                if (player != null) {
                    PlayerData data = this.plugin.getDataManager().getData(player);
                    sender.sendMessage(data.getUsableName() + "’s stat:");
                    sender.sendMessage(specificStat + " = "
                            + player.getStatistic(Statistic.valueOf(specificStat)));
                } else {
                    sender.sendMessage(ChatColor.RED + "No online player with that name was found");
                }
                return true;

            default: // /stats [player]
                extended = false;
                targetPlayer = sender.getServer().getPlayerExact(args[0]);
                break;

        }

        // /stats [player]

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
        if (args.length == 3) {
            for (Statistic statistic : Statistic.values()) {
                if (StringUtil.startsWithIgnoreCase(statistic.name(), args[2])) {
                    completions.add(statistic.name());
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
            sender.sendMessage(
                    "- Distance by Elytra: " + player.getStatistic(Statistic.AVIATE_ONE_CM));
            sender.sendMessage(
                    "- Distance by Minecart: " + player.getStatistic(Statistic.MINECART_ONE_CM));

        }
    }

    public String formatStatistic(Integer stat, String type) {
        if (type == "distance") {
            if (stat < 100) {
                return (String.valueOf(stat) + " cm");

            } else if (stat < 100000) {
                stat = Math.round(stat / 100);
                return (String.valueOf(stat) + " m");

            } else {
                // Divides by 1000 to allow for two significant digits
                stat = Math.round(stat / 1000);
                // Divides by 100 to finally get to km
                stat = (stat / 100);
                return (String.format("%,", stat) + " km");

            }

        }
        return "Error";



    }

}


