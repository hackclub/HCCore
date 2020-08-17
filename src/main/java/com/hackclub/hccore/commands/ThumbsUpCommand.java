package com.hackclub.hccore.commands;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ThumbsUpCommand implements CommandExecutor {
    private static final String thumbsUp = ChatColor.BOLD.toString() + "👍";

    private final HCCorePlugin plugin;
    
    public ThumbsUpCommand(HCCorePlugin plugin) {
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
            player.chat(ThumbsUpCommand.thumbsUp);
        } else {
            player.chat(String.join(" ", args) + " " + ThumbsUpCommand.thumbsUp);
        }

        return true;
    }

}