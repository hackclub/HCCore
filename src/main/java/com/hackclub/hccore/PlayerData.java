package com.hackclub.hccore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerData {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private final transient HCCorePlugin plugin;
    private final transient Player player;
    private transient File dataFile;

    // Session data (will be cleared when player quits)
    private transient boolean isAfk = false;
    private transient long lastDamagedAt = 0;

    // Persistent data
    // TODO: Save on every write or save on leave?
    private String nickname = null;
    // private String slackId = null;
    private Map<String, Location> savedLocations = new HashMap<>();

    public PlayerData(HCCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dataFile = new File(plugin.getDataManager().getDataFolder(),
                player.getUniqueId().toString() + ".json");

        // Set defaults
        this.nickname = player.getName();
    }

    public boolean isAfk() {
        return this.isAfk;
    }

    public void setAfk(boolean afk) {
        this.isAfk = afk;
        this.updateDisplayedName();
    }

    public long getLastDamagedAt() {
        return this.lastDamagedAt;
    }

    public void setLastDamagedAt(long damagedAt) {
        this.lastDamagedAt = damagedAt;
    }

    public String getNickname() {
        // TODO: Read from player file
        return this.nickname;
    }

    public void setNickname(String name) {
        // TODO: Write player file
        this.nickname = name != null ? name : this.player.getName();
        this.updateDisplayedName();
    }

    public Map<String, Location> getSavedLocations() {
        // TODO: Read from file
        return this.savedLocations;
    }

    public Location getSavedLocation(String name) {
        // TODO: Read from file
        return this.savedLocations.get(name);
    }

    public boolean hasSavedLocation(String name) {
        return this.savedLocations.containsKey(name);
    }

    public void addSavedLocation(String name, Location location) {
        // TODO: Add from file
        this.savedLocations.put(name, location);
    }

    public void removeSavedLocation(String name) {
        // TODO: Remove from file
        this.savedLocations.remove(name);
    }

    public void load() {
        try {
            if (!this.dataFile.exists()) {
                this.plugin.getLogger().log(Level.INFO,
                        "No data file found for " + this.player.getName() + ", creating…");
                this.dataFile.createNewFile();
            }

            PlayerData playerData =
                    PlayerData.GSON.fromJson(new FileReader(this.dataFile), PlayerData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            PlayerData.GSON.toJson(this, new FileWriter(this.dataFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayedName() {
        String format = "%s";
        if (this.isAfk()) {
            format = ChatColor.GRAY + format + " (AFK)";
        }
        return String.format(format, this.getNickname());
    }

    private void updateDisplayedName() {
        String builtDisplayName = this.getDisplayedName();
        this.player.setDisplayName(builtDisplayName);
        this.player.setPlayerListName(builtDisplayName);
        this.refreshNameTag();
    }

    private void refreshNameTag() {
        for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.equals(this.player) || !onlinePlayer.canSee(this.player)) {
                continue;
            }
            // Showing the player again after they've been hidden causes a Player Info packet to be
            // sent, which is then intercepted by NameChangeListener. The actual modification of the
            // name tag takes place there.
            onlinePlayer.hidePlayer(this.plugin, this.player);
            onlinePlayer.showPlayer(this.plugin, this.player);
        }
    }
}
