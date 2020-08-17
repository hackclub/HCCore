package com.hackclub.hccore.commands;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ThumbsDownCommand implements CommandExecutor {
    private static final String thumbsDown = ChatColor.BOLD.toString() + "👎";

    private final HCCorePlugin plugin;
    
    public ThumbsDownCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.chat(ThumbsDownCommand.thumbsDown);
        } else {
            player.chat(String.join(" ", args) + " " + ThumbsDownCommand.thumbsDown);
        }

        return true;
    }

}