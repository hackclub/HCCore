package com.hackclub.hccore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class DataManager {
    private final HCCorePlugin plugin;
    private String dataFolder;
    private Map<UUID, PlayerData> players = new HashMap<>();

    public DataManager(HCCorePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder() + File.separator + "players";

        // Create players folder if it doesn't exist already
        File folder = new File(this.dataFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public String getDataFolder() {
        return this.dataFolder;
    }

    public PlayerData getData(Player player) {
        return this.players.get(player.getUniqueId());
    }

    public void registerPlayer(Player player) {
        this.players.put(player.getUniqueId(), new PlayerData(this.plugin, player));
        this.getData(player).load();
    }

    public void registerAll() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.registerPlayer(player);
        }
    }

    public void unregisterPlayer(Player player) {
        this.getData(player).save();
        this.players.remove(player.getUniqueId());
    }

    public void unregisterAll() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.unregisterPlayer(player);
        }
    }
}
