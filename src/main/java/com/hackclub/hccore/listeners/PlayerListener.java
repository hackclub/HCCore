package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    private final HCCorePlugin plugin;

    public PlayerListener(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        this.plugin.getDataManager().getData(player).setLastDamagedAt(System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        event.setDeathMessage(event.getDeathMessage().replace(event.getEntity().getName(),
                event.getEntity().getDisplayName()));
    }

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.WHITE + "%s " + ChatColor.GOLD + "» " + ChatColor.GRAY + "%s");

        // Apply the player's chat color to the message and translate color codes
        PlayerData data = this.plugin.getDataManager().getData(event.getPlayer());
        ChatColor playerColor = data.getMessageColor();
        event.setMessage(
                playerColor + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.plugin.getDataManager().registerPlayer(event.getPlayer());

        // NOTE: Title isn't cleared when the player leaves the server
        event.getPlayer().resetTitle();
        event.setJoinMessage(event.getJoinMessage().replace(event.getPlayer().getName(),
                event.getPlayer().getDisplayName()));
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        // Reset lastDamagedAt
        this.plugin.getDataManager().getData(event.getPlayer()).setLastDamagedAt(0);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // NOTE: Title isn't cleared when the player leaves the server
        // event.getPlayer().resetTitle();
        event.setQuitMessage(event.getQuitMessage().replace(event.getPlayer().getName(),
                event.getPlayer().getDisplayName()));

        this.plugin.getDataManager().unregisterPlayer(event.getPlayer());
    }
}
