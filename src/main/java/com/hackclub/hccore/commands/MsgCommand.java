package com.hackclub.hccore.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class MsgCommand implements TabExecutor {
    private final HCCorePlugin plugin;

    public MsgCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
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
}
