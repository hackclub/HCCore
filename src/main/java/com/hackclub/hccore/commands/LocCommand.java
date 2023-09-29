package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playerMessages.MustBePlayerMessage;
import com.hackclub.hccore.playerMessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playerMessages.loc.HasLocationMessage;
import com.hackclub.hccore.playerMessages.loc.LocationAddMessage;
import com.hackclub.hccore.playerMessages.loc.LocationExistsMessage;
import com.hackclub.hccore.playerMessages.loc.LocationGetMessage;
import com.hackclub.hccore.playerMessages.loc.LocationListMessage;
import com.hackclub.hccore.playerMessages.loc.LocationNotFoundMessage;
import com.hackclub.hccore.playerMessages.loc.LocationRemovedMessage;
import com.hackclub.hccore.playerMessages.loc.LocationRenamedMessage;
import com.hackclub.hccore.playerMessages.loc.NoLocationsMessage;
import com.hackclub.hccore.playerMessages.loc.RecipSharedMessage;
import com.hackclub.hccore.playerMessages.loc.SelfShareMessage;
import com.hackclub.hccore.playerMessages.loc.SendSharedMessage;
import com.hackclub.hccore.playerMessages.loc.SpecifyLocationMessage;
import com.hackclub.hccore.playerMessages.loc.SpecifyShareMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class LocCommand implements TabExecutor {

  private final HCCorePlugin plugin;

  public LocCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(MustBePlayerMessage.get());
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    PlayerData data = this.plugin.getDataManager().getData(player);
    String locationName = String.join("_", Arrays.copyOfRange(args, 1, args.length));
    switch (args[0].toLowerCase()) {
      // /loc del <name>
      case "del" -> {
        if (args.length < 2) {
          sender.sendMessage(SpecifyLocationMessage.get());
          break;
        }

        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(LocationNotFoundMessage.get());
          break;
        }

        data.getSavedLocations().remove(locationName);
        sender.sendMessage(LocationRemovedMessage.get(locationName));
      }

      // /loc get <name>
      case "get" -> {
        if (args.length < 2) {
          sender.sendMessage(SpecifyLocationMessage.get());
          break;
        }
        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(LocationNotFoundMessage.get());
          break;
        }

        Location savedLocation = data.getSavedLocations().get(locationName);
        sender.sendMessage(LocationGetMessage.get(locationName, savedLocation.getWorld().getName(),
            savedLocation.getBlockX(), savedLocation.getBlockY(), savedLocation.getBlockZ()));
      }

      // /loc list
      case "list" -> {
        Map<String, Location> savedLocations = data.getSavedLocations();
        if (savedLocations.isEmpty()) {
          sender.sendMessage(NoLocationsMessage.get());
          break;
        }

        sender.sendMessage(LocationListMessage.get(savedLocations));
      }

      // /loc rename <old name> <new name>
      case "rename" -> {
        if (args.length < 3) {
          sender.sendMessage(this.plugin.getCommand("loc").getUsage());
          break;
        }
        String oldName = args[1];
        String newName = String.join("_", Arrays.copyOfRange(args, 2, args.length));
        Location targetLoc = data.getSavedLocations().get(oldName);
        if (!data.getSavedLocations().containsKey(oldName)) {
          sender.sendMessage(LocationNotFoundMessage.get());
          break;
        }
        if (data.getSavedLocations().containsKey(newName)) {
          sender.sendMessage(LocationExistsMessage.get());
          break;
        }
        data.getSavedLocations().put(newName, targetLoc);
        data.getSavedLocations().remove(oldName);
        sender.sendMessage(LocationRenamedMessage.get(oldName, newName));
      }

      // /loc save <name>
      case "save" -> {
        if (args.length < 2) {
          sender.sendMessage(SpecifyLocationMessage.get());
          break;
        }
        if (data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(LocationExistsMessage.get());
          break;
        }

        Location currentLocation = player.getLocation();
        data.getSavedLocations().put(locationName, currentLocation);
        sender.sendMessage(
            LocationAddMessage.get(locationName, currentLocation.getWorld().getName(),
                currentLocation));
      }

      // /loc share <name> <player>
      case "share" -> {
        if (args.length < 3) {
          sender.sendMessage(SpecifyShareMessage.get());
        }
        locationName = args[1];
        String recipientName = args[2];

        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(LocationNotFoundMessage.get());
          break;
        }
        Location sendLocation = data.getSavedLocations().get(locationName);
        // Get the player we're sending to
        Player recipient = sender.getServer().getPlayer(recipientName);
        if (recipient == null) {
          sender.sendMessage(NoOnlinePlayerMessage.get());
          break;
        }
        if (recipientName.equals(player.getName())) {
          sender.sendMessage(SelfShareMessage.get());
          break;
        }
        PlayerData recipData = this.plugin.getDataManager().getData(recipient);
        String shareLocName = player.getName() + " " + locationName;

        if (recipData.getSavedLocations().containsKey(player.getName() + ":" + shareLocName)) {
          sender.sendMessage(HasLocationMessage.get(recipientName, locationName));
          break;
        }

        player.sendMessage(SendSharedMessage.get(locationName, recipientName));
        player.sendMessage(RecipSharedMessage.get(player.getName(), locationName,
            sendLocation.getWorld().getName(), sendLocation.getBlockX(), sendLocation.getBlockY(),
            sendLocation.getBlockZ()));
        recipData.getSavedLocations().put(player.getName() + ":" + locationName, sendLocation);
      }
      default -> {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return null;
    }

    List<String> completions = new ArrayList<>();
    switch (args.length) {
      // Complete subcommand
      case 1 -> {
        List<String> subcommands = Arrays.asList("del", "get", "list", "rename", "save", "share");
        StringUtil.copyPartialMatches(args[0], subcommands, completions);
      }

      // Complete location name for everything but /loc list and /loc save
      case 2 -> {
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
      }

      // Complete online player name for /loc share
      case 3 -> {
        if (!args[0].equalsIgnoreCase("share")) {
          break;
        }

        for (Player player : sender.getServer().getOnlinePlayers()) {
          if (StringUtil.startsWithIgnoreCase(player.getName(), args[2])) {
            completions.add(player.getName());
          }
        }
      }
    }

    Collections.sort(completions);
    return completions;
  }
}
