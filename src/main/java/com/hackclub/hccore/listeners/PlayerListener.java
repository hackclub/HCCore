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
import org.bukkit.event.player.PlayerLoginEvent;
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
        String message = event.getDeathMessage();
        message = message.replace(event.getEntity().getName(),
                ChatColor.stripColor(event.getEntity().getDisplayName()));

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            message = message.replace(killer.getName(),
                    ChatColor.stripColor(killer.getDisplayName()));
        }

        event.setDeathMessage(message);
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
        event.setJoinMessage(ChatColor.YELLOW
                + ChatColor.stripColor(event.getPlayer().getDisplayName()) + " joined the game");
    }

    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED
                || event.getResult() == PlayerLoginEvent.Result.KICK_OTHER) {
            return;
        }

        String message = null;
        switch (event.getResult()) {
            case KICK_BANNED:
                message = ChatColor.RED + ChatColor.BOLD.toString() + "You’ve been banned :(\n\n"
                        + ChatColor.RESET + ChatColor.WHITE
                        + "If you believe this was a mistake, please DM " + ChatColor.AQUA
                        + "@Luke or @ifvictr " + ChatColor.WHITE + "on Slack.";
                break;
            case KICK_FULL:
                message = ChatColor.RED + ChatColor.BOLD.toString() + "The server is full!\n\n"
                        + ChatColor.RESET + ChatColor.WHITE
                        + "Sorry, it looks like there’s no more room. Please try again in ~20 minutes.";
                break;
            case KICK_WHITELIST:
                message = ChatColor.RED + ChatColor.BOLD.toString() + "You’re not whitelisted!\n\n"
                        + ChatColor.RESET + ChatColor.WHITE + "Join " + ChatColor.AQUA
                        + "#minecraft " + ChatColor.WHITE + "on Slack and ping "
                        + ChatColor.AQUA + "@ifvictr " + ChatColor.WHITE + "or " + ChatColor.AQUA
                        + "@Luke " + ChatColor.WHITE + "to be added.";
                break;
        }
        event.setKickMessage(message);
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
        event.setQuitMessage(ChatColor.YELLOW
                + ChatColor.stripColor(event.getPlayer().getDisplayName()) + " left the game");

        this.plugin.getDataManager().unregisterPlayer(event.getPlayer());
    }
}
