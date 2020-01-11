package com.hackclub.hccore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import com.google.gson.annotations.Expose;
import com.hackclub.hccore.utils.gson.GsonUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerData {
    private final HCCorePlugin plugin;
    private final Player player;
    private final File dataFile;

    // Session data (will be cleared when player quits)
    private boolean isAfk = false;
    private long lastDamagedAt = 0;

    // Persistent data
    @Expose
    private String nickname = null;
    @Expose
    private String slackId = null;
    @Expose
    private Map<String, Location> savedLocations = new LinkedHashMap<>();

    public PlayerData(HCCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dataFile = new File(plugin.getDataManager().getDataFolder(),
                player.getUniqueId().toString() + ".json");
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
        return this.nickname;
    }

    public void setNickname(String name) {
        this.nickname = name;
        this.updateDisplayedName();
    }

    public String getSlackId() {
        return this.slackId;
    }

    public void setSlackId(String id) {
        this.slackId = id;
    }

    public Map<String, Location> getSavedLocations() {
        return this.savedLocations;
    }

    public void load() {
        try {
            if (!this.dataFile.exists()) {
                this.plugin.getLogger().log(Level.INFO,
                        "No data file found for " + this.player.getName() + ", creating…");
                this.dataFile.createNewFile();
                this.save();
            }

            // Populate this instance
            PlayerData playerData = GsonUtil.getInstance().fromJson(new FileReader(this.dataFile),
                    PlayerData.class);
            this.setNickname(playerData.nickname);
            this.savedLocations = playerData.getSavedLocations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.dataFile.getParentFile().mkdirs(); // In case parent directory is missing

            FileWriter writer = new FileWriter(this.dataFile);
            GsonUtil.getInstance().toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayedName() {
        String format = "%s";
        if (this.isAfk()) {
            format = ChatColor.GRAY + format + " (AFK)";
        }

        // Fallback to username if there's no nickname set
        String name = this.getNickname() != null ? this.getNickname() : this.player.getName();
        return String.format(format, name);
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
