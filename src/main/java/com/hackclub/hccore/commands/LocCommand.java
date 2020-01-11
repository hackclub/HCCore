package com.hackclub.hccore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class LocCommand implements TabExecutor {
    private final HCCorePlugin plugin;

    public LocCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this");
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;
        PlayerData data = this.plugin.getDataManager().getData(player);
        switch (args[0].toLowerCase()) {
            // /loc del <name>
            case "del": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (!data.getSavedLocations().containsKey(args[1])) {
                    player.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                data.getSavedLocations().remove(args[1]);
                player.sendMessage(
                        ChatColor.GREEN + "Removed " + args[1] + " from saved locations");
                break;
            }
            // /loc get <name>
            case "get": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (!data.getSavedLocations().containsKey(args[1])) {
                    player.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                Location savedLocation = data.getSavedLocations().get(args[1]);
                player.sendMessage(args[1] + ": " + savedLocation.getWorld().getName() + " @ "
                        + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
                        + savedLocation.getBlockZ());
                break;
            }
            // /loc list
            case "list": {
                Map<String, Location> savedLocations = data.getSavedLocations();
                if (savedLocations.isEmpty()) {
                    player.sendMessage("You have no saved locations");
                    break;
                }

                player.sendMessage(ChatColor.AQUA + "Your saved locations:");
                for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
                    Location savedLocation = entry.getValue();
                    player.sendMessage("- " + entry.getKey() + ": "
                            + savedLocation.getWorld().getName() + " @ " + savedLocation.getBlockX()
                            + ", " + savedLocation.getBlockY() + ", " + savedLocation.getBlockZ());
                }
                break;
            }
            // /loc save <name>
            case "save": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (data.getSavedLocations().containsKey(args[1])) {
                    player.sendMessage(ChatColor.RED + "A location with that name already exists");
                    break;
                }

                Location currentLocation = player.getLocation();
                data.getSavedLocations().put(args[1], currentLocation);
                player.sendMessage(ChatColor.GREEN + "Added " + args[1] + " ("
                        + currentLocation.getWorld().getName() + " @ " + currentLocation.getBlockX()
                        + ", " + currentLocation.getBlockY() + ", " + currentLocation.getBlockZ()
                        + ") to saved locations");
                break;
            }
            // /loc share <name> <player>
            case "share": {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED
                            + "Please specify the location name and the player you want to share it with");
                    break;
                }
                if (!data.getSavedLocations().containsKey(args[1])) {
                    player.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }
                // TODO: Add share functionality
                break;
            }
            // /loc <name>, alias for /loc save
            default:
                return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias,
            String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<String>();
        switch (args.length) {
            // Complete subcommand
            case 1: {
                List<String> subcommands = Arrays.asList("del", "get", "list", "save", "share");
                StringUtil.copyPartialMatches(args[0], subcommands, completions);
                break;
            }
            // Complete location name for everything but /loc list and /loc save
            case 2: {
                if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("save")) {
                    break;
                }

                Player player = (Player) sender;
                PlayerData data = this.plugin.getDataManager().getData(player);
                for (Map.Entry<String, Location> entry : data.getSavedLocations().entrySet()) {
                    if (StringUtil.startsWithIgnoreCase(entry.getKey(), args[1])) {
                        completions.add(entry.getKey());
                    }
                }
                break;
            }
            // Complete online player name for /loc share
            case 3: {
                if (!args[0].equalsIgnoreCase("share")) {
                    break;
                }

                for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                    if (StringUtil.startsWithIgnoreCase(player.getName(), args[2])) {
                        completions.add(player.getName());
                    }
                }
                break;
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
