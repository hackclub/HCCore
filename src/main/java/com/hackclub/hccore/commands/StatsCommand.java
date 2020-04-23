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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        // /stats
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage("Your stats:");
                this.sendStatistics(sender, (Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            }
            return true;
        }

        // /stats [player]
        Player targetPlayer = sender.getServer().getPlayerExact(args[0]);
        if (targetPlayer != null) {
            PlayerData data = this.plugin.getDataManager().getData(targetPlayer);
            sender.sendMessage(data.getUsableName() + "’s stats:");
            this.sendStatistics(sender, targetPlayer);
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
        }

        Collections.sort(completions);
        return completions;
    }

    private void sendStatistics(CommandSender sender, Player player) {
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
    }
}
