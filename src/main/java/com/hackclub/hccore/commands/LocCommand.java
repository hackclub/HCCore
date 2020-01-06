package com.hackclub.hccore.commands;

import java.util.Map;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// TODO: Move subcommands into separate classes
public class LocCommand implements CommandExecutor {
    private final HCCorePlugin plugin;

    public LocCommand(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
                if (!data.hasSavedLocation(args[1])) {
                    player.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }
                data.removeSavedLocation(args[1]);
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
                if (!data.hasSavedLocation(args[1])) {
                    player.sendMessage(ChatColor.RED + "No location with that name was found");
                    break;
                }
                Location savedLocation = data.getSavedLocation(args[1]);
                player.sendMessage(args[1] + ": " + savedLocation.getWorld().getName() + " @ "
                        + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
                        + savedLocation.getBlockZ());
                break;
            }
            // /loc list
            case "list": {
                Map<String, Location> savedLocations = data.getSavedLocations();
                if (savedLocations.size() == 0) {
                    player.sendMessage("You have no saved locations");
                    break;
                }

                player.sendMessage(ChatColor.AQUA + "Your saved locations:");
                for (String locationName : savedLocations.keySet()) {
                    Location savedLocation = savedLocations.get(locationName);
                    player.sendMessage("- " + locationName + ": "
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
                if (data.hasSavedLocation(args[1])) {
                    player.sendMessage(ChatColor.RED + "A location with that name already exists");
                    break;
                }
                Location currentLocation = player.getLocation();
                data.addSavedLocation(args[1], currentLocation);
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
                if (!data.hasSavedLocation(args[1])) {
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
}
