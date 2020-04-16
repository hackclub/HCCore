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
        String locationName = String.join("_", Arrays.copyOfRange(args, 1, args.length));
        switch (args[0].toLowerCase()) {
            // /loc del <name>
            case "del": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                
                if (!data.getSavedLocations().containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                data.getSavedLocations().remove(locationName);
                sender.sendMessage(
                        ChatColor.GREEN + "Removed " + locationName + " from saved locations");
                break;
            }
            // /loc get <name>
            case "get": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (!data.getSavedLocations().containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                Location savedLocation = data.getSavedLocations().get(locationName);
                sender.sendMessage(locationName + ": " + savedLocation.getWorld().getName() + " @ "
                        + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
                        + savedLocation.getBlockZ());
                break;
            }
            // /loc list
            case "list": {
                Map<String, Location> savedLocations = data.getSavedLocations();
                if (savedLocations.isEmpty()) {
                    sender.sendMessage("You have no saved locations");
                    break;
                }

                sender.sendMessage(
                        ChatColor.AQUA + "Your saved locations (" + savedLocations.size() + "):");
                for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
                    Location savedLocation = entry.getValue();
                    sender.sendMessage("- " + entry.getKey() + ": "
                            + savedLocation.getWorld().getName() + " @ " + savedLocation.getBlockX()
                            + ", " + savedLocation.getBlockY() + ", " + savedLocation.getBlockZ());
                }
                break;
            }
            // /loc rename <old name> <new name>
            case "rename": {
                if (args.length < 3) {
                    sender.sendMessage("/loc rename <old name> <new name>");
                    break;
                }
                String oldName = args[1];
                String newName = String.join("_", Arrays.copyOfRange(args, 2, args.length));
                Location targetLoc = data.getSavedLocations().get(oldName);
                if (!data.getSavedLocations().containsKey(oldName)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }
                if (data.getSavedLocations().containsKey(newName)) {
                    sender.sendMessage(ChatColor.RED + "A location with that name already exists");
                    break;
                }
                data.getSavedLocations().put(newName, targetLoc);
                data.getSavedLocations().remove(oldName);
                sender.sendMessage(ChatColor.GREEN + "Renamed from " + oldName + "to " + newName);
                break;
            }
            // /loc save <name>
            case "save": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (data.getSavedLocations().containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED + "A location with that name already exists");
                    break;
                }

                Location currentLocation = player.getLocation();
                data.getSavedLocations().put(locationName, currentLocation);
                sender.sendMessage(ChatColor.GREEN + "Added " + locationName + " ("
                        + currentLocation.getWorld().getName() + " @ " + currentLocation.getBlockX()
                        + ", " + currentLocation.getBlockY() + ", " + currentLocation.getBlockZ()
                        + ") to saved locations");
                break;
            }
            // /loc share <name> <player>
            case "share": {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED
                            + "Please specify the location name and the player you want to share it with");
                    break;
                }
                if (!data.getSavedLocations().containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }
                // TODO: Add share functionality
                break;
            }
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
                List<String> subcommands =
                        Arrays.asList("del", "get", "list", "rename", "save", "share");
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

                for (Player player : sender.getServer().getOnlinePlayers()) {
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
