package com.hackclub.hccore;

import com.google.gson.annotations.Expose;
import com.hackclub.hccore.events.player.PlayerAFKStatusChangeEvent;
import com.hackclub.hccore.utils.gson.GsonUtil;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Team;

public class PlayerData {

  public static final int MAX_NICKNAME_LENGTH = 32;
  private final HCCorePlugin plugin;
  public final Player player;
  public final OfflinePlayer offlinePlayer;
  private final File dataFile;

  // Session data (will be cleared when player quits)
  private boolean isAfk = false;
  private long lastDamagedAt = 0;
  private long lastActiveAt = 0;

  // Persistent data
  @Expose
  private String nickname = null;
  @Expose
  private String slackId = null;
  @Expose
  private TextColor nameColor = NamedTextColor.WHITE;
  @Expose
  private TextColor messageColor = NamedTextColor.GRAY;
  @Expose
  private Map<String, Location> savedLocations = new LinkedHashMap<>();
  @Expose
  private UUID lastPlayerChattingWith = null;

  public PlayerData(HCCorePlugin plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
    this.offlinePlayer = player;
    this.dataFile =
        new File(plugin.getDataManager().getDataFolder(), player.getUniqueId() + ".json");
  }

  public PlayerData(HCCorePlugin plugin, OfflinePlayer offlinePlayer) {
    this.plugin = plugin;
    this.player = offlinePlayer.getPlayer();
    this.offlinePlayer = offlinePlayer;
    this.dataFile =
        new File(plugin.getDataManager().getDataFolder(), offlinePlayer.getUniqueId() + ".json");
  }

  public boolean isAfk() {
    return this.isAfk;
  }

  public void setAfk(boolean afk) {
    if (this.player != null) {
      this.isAfk = afk;
      this.updateDisplayedName();

      TextColor newColor = afk ? NamedTextColor.GRAY : this.getNameColor();
      Team team = this.getTeam();
      if (team != null) {
        team.color(NamedTextColor.nearestTo(newColor));
      }

      Event event = new PlayerAFKStatusChangeEvent(this.player, afk);
      this.player.getServer().getPluginManager().callEvent(event);
    }
  }

  public long getLastDamagedAt() {
    return this.lastDamagedAt;
  }

  public void setLastDamagedAt(long damagedAt) {
    this.lastDamagedAt = damagedAt;
  }

  public long getLastActiveAt() {
    return this.lastActiveAt;
  }

  public void setLastActiveAt(long activeAt) {
    this.lastActiveAt = activeAt;
  }

  public String getNickname() {
    return this.nickname;
  }

  public void setNickname(String nickname) {
    // Validate length if a nickname was specified
    if (nickname != null && nickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      return;
    }

    String oldProfileName = this.getProfileName();
    this.nickname = nickname;
    this.updateDisplayedName();

    // The scoreboard team entry must match the name the client sees in the player info packet,
    // otherwise the team's color and suffix won't apply to the player.
    Team team = this.getTeam();
    if (team != null) { // is null during pre-login event
      team.removeEntry(oldProfileName);
      team.addEntry(this.getProfileName());
    }
  }

  // The name carried in the player's GameProfile. GameProfile names are limited to 16 UTF-16
  // units, so longer nicknames are split: the first 16 units go here and the rest is appended
  // via the scoreboard team suffix (see updateTeamDecorations) so the full nickname is still
  // shown on the vanilla name tag above the player's head. The split never lands between the two
  // halves of a surrogate pair, so supplementary characters are kept intact.
  public String getProfileName() {
    String nickname = this.getNickname();
    if (nickname != null) {
      return nickname.substring(0, getSplitIndex(nickname));
    }
    return this.offlinePlayer.getName();
  }

  public String getNicknameOverflow() {
    String nickname = this.getNickname();
    if (nickname == null) {
      return "";
    }
    return nickname.substring(getSplitIndex(nickname));
  }

  private static int getSplitIndex(String nickname) {
    if (nickname.length() <= 16) {
      return nickname.length();
    }
    return Character.isHighSurrogate(nickname.charAt(15)) ? 15 : 16;
  }

  @SuppressWarnings("unused")
  public String getSlackId() {
    return this.slackId;
  }

  public void setSlackId(String id) {
    this.slackId = id;
  }

  public TextColor getNameColor() {
    return this.nameColor;
  }

  public void setNameColor(TextColor color) {
    this.nameColor = (color != null) ? color : NamedTextColor.WHITE;
    this.updateDisplayedName();

    Team team = this.getTeam();
    if (team != null) { // is null during pre-login event
      team.color(NamedTextColor.nearestTo(this.getNameColor()));
    }
  }

  public TextColor getMessageColor() {
    return this.messageColor;
  }

  public void setMessageColor(TextColor color) {
    this.messageColor = (color != null) ? color : NamedTextColor.GRAY;
  }

  public Map<String, Location> getSavedLocations() {
    return this.savedLocations;
  }

  public Team getTeam() {
    if (this.player != null) {
      return this.player.getServer().getScoreboardManager().getMainScoreboard()
          .getTeam(this.player.getName());
    } else {
      return null;
    }
  }

  public void load() {
    try {
      this.dataFile.getParentFile().mkdirs();

      if (!this.dataFile.exists()) {
        this.plugin.getLogger().log(Level.INFO,
            "No data file found for " + this.offlinePlayer.getName() + ", creating…");
        this.dataFile.createNewFile();
        this.save();
      }

      // Populate this instance
      PlayerData data = GsonUtil.getInstance().fromJson(new FileReader(this.dataFile),
          PlayerData.class);
      this.setNickname(data.nickname);
      this.setSlackId(data.slackId);
      this.setNameColor(data.nameColor);
      this.setMessageColor(data.messageColor);
      this.setLastPlayerChattingWith(data.lastPlayerChattingWith);
      this.savedLocations = data.savedLocations;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void save() {
    try {
      this.dataFile.getParentFile().mkdirs();

      FileWriter writer = new FileWriter(this.dataFile);
      GsonUtil.getInstance().toJson(this, writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getUsableName() {
    return this.getNickname() != null ? this.getNickname() : this.offlinePlayer.getName();
  }

  public TextComponent getDisplayedName() {
    TextColor color;
    String name;
    if (this.isAfk()) {
      color = NamedTextColor.GRAY;
      name = this.getUsableName() + " (AFK)";
    } else {
      color = this.getNameColor();
      name = this.getUsableName();
    }

    return Component.text(name).color(color);
  }

  private void updateDisplayedName() {
    if (this.player != null) {
      TextComponent builtDisplayName = this.getDisplayedName();
      this.player.displayName(builtDisplayName);
      this.player.playerListName(builtDisplayName);
      this.updateTeamDecorations();
      this.refreshNameTag();
    }
  }

  private void updateTeamDecorations() {
    Team team = this.getTeam();
    if (team == null) {
      return;
    }

    TextColor color = this.isAfk() ? NamedTextColor.GRAY : this.getNameColor();
    String suffixText = this.getNicknameOverflow() + (this.isAfk() ? " (AFK)" : "");
    team.suffix(Component.text(suffixText).color(color));
  }

  private void refreshNameTag() {
    if (this.player != null) {
      for (Player onlinePlayer : this.player.getServer().getOnlinePlayers()) {
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

  public void setLastPlayerChattingWith(UUID lastPlayerChattingWith) {
    this.lastPlayerChattingWith = lastPlayerChattingWith;
  }

  public UUID getLastPlayerChattingWith() {
    return this.lastPlayerChattingWith;
  }
}
