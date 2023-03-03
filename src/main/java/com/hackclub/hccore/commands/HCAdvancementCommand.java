package com.hackclub.hccore.commands;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HCAdvancementCommand implements TabExecutor {
    private final HCCorePlugin plugin;

    public HCAdvancementCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You must be an operator to use this");
            return true;
        }

        Advancement adv = this.plugin.tab.getAdvancement(new AdvancementKey(this.plugin, args[2]));
        if (adv == null) {
            return false;
        }

        Player player = this.plugin.getServer().getPlayer(args[1]);
        if (player == null) {
            return false;
        }

        if (args[0].equalsIgnoreCase("grant")) {
            adv.grant(player);
        } else if (args[0].equalsIgnoreCase("revoke")) {
            adv.revoke(player);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("grant", "revoke");
        } else if (args.length == 2) {
            return this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 3) {
            return this.plugin.tab.getAdvancements().stream().map(Advancement::getKey).map(AdvancementKey::toString).map(str -> str.split(":")[1]).collect(Collectors.toList());
        }

        return null;
    }
}
