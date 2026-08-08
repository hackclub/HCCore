package com.hackclub.hccore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class DataManager {

  private final HCCorePlugin plugin;
  private final String dataFolder;
  private final Map<UUID, PlayerData> onlinePlayers = new HashMap<>();

  public DataManager(HCCorePlugin plugin) {
    this.plugin = plugin;
    this.dataFolder = plugin.getDataFolder() + File.separator + "players";

    File folder = new File(this.dataFolder);
    folder.mkdirs();
  }

  public String getDataFolder() {
    return this.dataFolder;
  }

  public PlayerData getData(Player player) {
    return this.onlinePlayers.get(player.getUniqueId());
  }

  public PlayerData getData(OfflinePlayer offlinePlayer) {
    PlayerData data = this.onlinePlayers.get(offlinePlayer.getUniqueId());

    if (data == null) {
      data = new PlayerData(this.plugin, offlinePlayer);
      data.load();
    }

    return data;
  }

  public PlayerData findData(Predicate<? super PlayerData> predicate) {
    PlayerData onlineMatch = this.onlinePlayers.values().stream().filter(predicate).findFirst()
        .orElse(null);
    if (onlineMatch != null) {
      return onlineMatch;
    }

    File[] files = new File(this.dataFolder).listFiles(
        (directory, name) -> name.endsWith(".json"));
    if (files == null) {
      return null;
    }

    Set<UUID> onlineUuids = new HashSet<>(this.onlinePlayers.keySet());
    for (File file : files) {
      String filename = file.getName();
      try {
        UUID uuid = UUID.fromString(filename.substring(0, filename.length() - ".json".length()));
        if (onlineUuids.contains(uuid)) {
          continue;
        }

        PlayerData data = this.getData(this.plugin.getServer().getOfflinePlayer(uuid));
        if (predicate.test(data)) {
          return data;
        }
      } catch (IllegalArgumentException exception) {
        this.plugin.getLogger().log(Level.WARNING,
            "Ignoring player data file with an invalid UUID: " + filename);
      }
    }

    return null;
  }

  public List<PlayerData> findDataMany(Predicate<? super PlayerData> predicate) {
    List<PlayerData> onlineMatches = this.onlinePlayers.values().stream().filter(predicate).toList();
    List<PlayerData> matches = new ArrayList<>(onlineMatches);

    File[] files = new File(this.dataFolder).listFiles(
        (directory, name) -> name.endsWith(".json"));
    if (files == null) {
      return matches;
    }

    Set<UUID> onlineUuids = new HashSet<>(this.onlinePlayers.keySet());
    for (File file : files) {
      String filename = file.getName();
      try {
        UUID uuid = UUID.fromString(filename.substring(0, filename.length() - ".json".length()));
        if (onlineUuids.contains(uuid)) {
          continue;
        }

        PlayerData data = this.getData(this.plugin.getServer().getOfflinePlayer(uuid));
        if (predicate.test(data)) {
          matches.add(data);
        }
      } catch (IllegalArgumentException exception) {
        this.plugin.getLogger().log(Level.WARNING,
            "Ignoring player data file with an invalid UUID: " + filename);
      }
    }

    return matches;
  }

  public void registerPlayer(Player player) {
    this.onlinePlayers.put(player.getUniqueId(), new PlayerData(this.plugin, player));

    // Register player's team
    Scoreboard mainScoreboard = player.getServer().getScoreboardManager().getMainScoreboard();
    player.setScoreboard(mainScoreboard);
    // Unregister existing teams in the player's name
    Team playerTeam = mainScoreboard.getTeam(player.getName());
    if (playerTeam != null) {
      playerTeam.unregister();
    }
    playerTeam = mainScoreboard.registerNewTeam(player.getName());
    playerTeam.addEntry(player.getName());

    // Load in player data for use
    this.getData(player).load();
  }

  public void registerAll() {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.registerPlayer(player);
    }
  }

  public void unregisterPlayer(Player player) {
    this.getData(player).save();

    this.getData(player).getTeam().unregister();

    this.onlinePlayers.remove(player.getUniqueId());
  }

  public void unregisterAll() {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.unregisterPlayer(player);
    }
  }
}
