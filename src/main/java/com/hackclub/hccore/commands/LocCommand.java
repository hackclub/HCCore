package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.NoOnlinePlayerMessage;
import com.hackclub.hccore.playermessages.loc.HasLocationMessage;
import com.hackclub.hccore.playermessages.loc.LocationAddMessage;
import com.hackclub.hccore.playermessages.loc.LocationExistsMessage;
import com.hackclub.hccore.playermessages.loc.LocationGetMessage;
import com.hackclub.hccore.playermessages.loc.LocationListMessage;
import com.hackclub.hccore.playermessages.loc.LocationNotFoundMessage;
import com.hackclub.hccore.playermessages.loc.LocationRemovedMessage;
import com.hackclub.hccore.playermessages.loc.LocationRenamedMessage;
import com.hackclub.hccore.playermessages.loc.NoLocationsMessage;
import com.hackclub.hccore.playermessages.loc.RecipSharedMessage;
import com.hackclub.hccore.playermessages.loc.SelfShareMessage;
import com.hackclub.hccore.playermessages.loc.SendSharedMessage;
import com.hackclub.hccore.playermessages.loc.SpecifyLocationMessage;
import com.hackclub.hccore.playermessages.loc.SpecifyShareMessage;
import com.hackclub.hccore.utils.ComponentBuilder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command("loc|locations")
public class LocCommand extends AbstractCommand{

  @Command("del [name]")
  public void executeDel(
      final @NotNull Player sender,
      @Nullable @Argument(value = "name", suggestions = "locations") @Greedy String name
  ) {
    if (name == null) {
      sender.sendMessage(SpecifyLocationMessage.get());
      return;
    }
    PlayerData data = this.plugin.getDataManager().getData(sender);
    name = name.replaceAll(" ", "_");
    if (!data.getSavedLocations().containsKey(name)) {
      sender.sendMessage(LocationNotFoundMessage.get());
      return;
    }

    data.getSavedLocations().remove(name);
    sender.sendMessage(LocationRemovedMessage.get(name));
  }
  
  @Command("get [name]")
  public void executeGet(
      final @NotNull Player sender,
      @Nullable @Argument(value = "name", suggestions = "locations") @Greedy String name
  ) {
    if (name == null) {
      sender.sendMessage(SpecifyLocationMessage.get());
      return;
    }
    PlayerData data = this.plugin.getDataManager().getData(sender);
    name = name.replaceAll(" ", "_");
    if (!data.getSavedLocations().containsKey(name)) {
      sender.sendMessage(LocationNotFoundMessage.get());
      return;
    }

    Location savedLocation = data.getSavedLocations().get(name);
    sender.sendMessage(LocationGetMessage.get(name, savedLocation.getWorld().getName(),
        savedLocation.getBlockX(), savedLocation.getBlockY(), savedLocation.getBlockZ()));
  }
  
  @Command("list")
  public void executeList(
      final @NotNull Player sender
  ) {
    PlayerData data = this.plugin.getDataManager().getData(sender);
    Map<String, Location> savedLocations = data.getSavedLocations();
    if (savedLocations.isEmpty()) {
      sender.sendMessage(NoLocationsMessage.get());
      return;
    }

    sender.sendMessage(LocationListMessage.get(savedLocations));
  }
  
  @Command("rename [old] [new]")
  public void executeRename(
      final @NotNull Player sender,
      @Nullable @Argument(value = "old", suggestions = "locations") String name,
      @Nullable @Argument(value = "new") @Greedy String newName
  ) {
    if (name == null || newName == null) {
      ComponentBuilder.newBuilder()
          .red("Usage: /loc rename <old name> <new name>")
          .send(sender);
      return;
    }
    PlayerData data = this.plugin.getDataManager().getData(sender);
    newName = newName.replaceAll(" ", "_");
    Location targetLoc = data.getSavedLocations().get(name);
    if (!data.getSavedLocations().containsKey(name)) {
      sender.sendMessage(LocationNotFoundMessage.get());
      return;
    }
    if (data.getSavedLocations().containsKey(newName)) {
      sender.sendMessage(LocationExistsMessage.get());
      return;
    }
    data.getSavedLocations().put(newName, targetLoc);
    data.getSavedLocations().remove(name);
    sender.sendMessage(LocationRenamedMessage.get(name, newName));
  }
  
  @Command("save [name]")
  public void executeSave(
      final @NotNull Player sender,
      @Nullable @Argument(value = "old", suggestions = "locations") String name
  ) {
    if (name == null) {
      sender.sendMessage(SpecifyLocationMessage.get());
      return;
    }
    PlayerData data = this.plugin.getDataManager().getData(sender);
    name = name.replaceAll(" ", "_");
    if (data.getSavedLocations().containsKey(name)) {
      sender.sendMessage(LocationExistsMessage.get());
      return;
    }

    Location currentLocation = sender.getLocation();
    data.getSavedLocations().put(name, currentLocation);
    sender.sendMessage(
        LocationAddMessage.get(name, currentLocation.getWorld().getName(),
            currentLocation));
  }
  
  @Command("share [name] [player]")
  public void executeShare(
      final @NotNull Player sender,
      @Nullable @Argument(value = "name", suggestions = "locations") String name,
      @Nullable @Argument(value = "player") Player target
  ) {
    if (name == null || target == null) {
      sender.sendMessage(SpecifyShareMessage.get());
      return;
    }
    PlayerData data = this.plugin.getDataManager().getData(sender);
    if (!data.getSavedLocations().containsKey(name)) {
      sender.sendMessage(LocationNotFoundMessage.get());
      return;
    }
    Location sendLocation = data.getSavedLocations().get(name);
    // Get the player we're sending to
    Player recipient = sender.getServer().getPlayer(name);
    if (recipient == null) {
      sender.sendMessage(NoOnlinePlayerMessage.get());
      return;
    }
    if (target.getName().equals(sender.getName())) {
      sender.sendMessage(SelfShareMessage.get());
      return;
    }
    PlayerData recipData = this.plugin.getDataManager().getData(recipient);
    String shareLocName = sender.getName() + " " + name;

    if (recipData.getSavedLocations().containsKey(sender.getName() + ":" + shareLocName)) {
      sender.sendMessage(HasLocationMessage.get(target.getName(), name));
      return;
    }

    sender.sendMessage(SendSharedMessage.get(name, target.getName()));
    sender.sendMessage(RecipSharedMessage.get(sender.getName(), name,
        sendLocation.getWorld().getName(), sendLocation.getBlockX(), sendLocation.getBlockY(),
        sendLocation.getBlockZ()));
    recipData.getSavedLocations().put(sender.getName() + ":" + name, sendLocation);
  }
  
  

  @Suggestions("locations")
  public Set<String> suggestLocations(
      final CommandContext<Player> context,
      final String input
  ) {
    Player player = context.sender();
    PlayerData data = plugin.getDataManager().getData(player);
    return data.getSavedLocations().keySet().stream()
        .filter(key -> key.toLowerCase().startsWith(input.toLowerCase()))
        .collect(Collectors.toSet());
  }
}
