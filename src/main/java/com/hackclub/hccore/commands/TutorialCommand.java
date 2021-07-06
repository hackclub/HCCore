package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialCommand implements CommandExecutor {
    private static final String[] MESSAGE = {ChatColor.RED + "Greetings! Welcome to the Hack Club vanilla Minecraft server!", "You can use /nick to set your nick name and /color to set your chat and name colors", ChatColor.RED + "Rules:", "Be nice.",  "No griefing or stealing", "No mods that give an unfair advantage", "Follow Hackclub CoC", "If you want to contribute to plugin development head on over to the GitHub: " + ChatColor.RED + "https://github.com/hackclub/HCCore/. ", "Type /tutorial to see this at any time and /discord to join the Discord!" + ChatColor.BLUE + ChatColor.BOLD + "Also checkout the modded server!"};

    private final HCCorePlugin plugin;

    public TutorialCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        } else {
            for (String message : TutorialCommand.MESSAGE) {
                sender.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + message);
            }
        }

        return true;
    }
}
