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

    /**
     * Convert args from startIndex to args.length into a
     *  space-separated string.
     * @param args List of arguments
     * @param startIndex The argument to start concatenating from
     * @return Concatenated, space-separated argument string.
     */
    private String getArgsAsString(String[] args, int startIndex) {
        if (startIndex > args.length - 1) return ""; // Invalid starting index
        if (args.length < 2) return ""; // Only one arg, hence nothing to concat
        String arguments = args[startIndex];
        for (int i = startIndex+1; i < args.length; ++i) {
            arguments = String.join(" ", arguments, args[i]);
        }
        return arguments;
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
        String argString =  getArgsAsString(args, 1);
        switch (args[0].toLowerCase()) {
            // /loc del <name>
            case "del": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                
                if (!data.getSavedLocations().containsKey(argString)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                data.getSavedLocations().remove(argString);
                sender.sendMessage(
                        ChatColor.GREEN + "Removed " + argString + " from saved locations");
                break;
            }
            // /loc get <name>
            case "get": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (!data.getSavedLocations().containsKey(argString)) {
                    sender.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }

                Location savedLocation = data.getSavedLocations().get(argString);
                sender.sendMessage(argString + ": " + savedLocation.getWorld().getName() + " @ "
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
            // /loc save <name>
            case "save": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Please specify the location name");
                    break;
                }
                if (data.getSavedLocations().containsKey(argString)) {
                    sender.sendMessage(ChatColor.RED + "A location with that name already exists");
                    break;
                }

                Location currentLocation = player.getLocation();
                data.getSavedLocations().put(argString, currentLocation);
                sender.sendMessage(ChatColor.GREEN + "Added " + argString + " ("
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
                if (!data.getSavedLocations().containsKey(argString)) {
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
