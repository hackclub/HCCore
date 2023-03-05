package com.hackclub.hccore.listeners;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    this.plugin.getDataManager().getData(player).setLastDamagedAt(System.currentTimeMillis());
  }

  @EventHandler
  public void onPlayerDeath(final PlayerDeathEvent event) {
    String message = event.getDeathMessage();
    if (message == null) {
      return;
    }
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
    Player player = event.getPlayer();

    // Apply the player's chat color to the message and translate color codes

    PlayerData data = this.plugin.getDataManager().getData(player);
    TextColor messageColor = data.getMessageColor();
//    TextColor nameColor = data.getNameColor();

    Component nameComponent = player.displayName().hoverEvent(
        net.kyori.adventure.text.event.HoverEvent.showEntity(player.getType(), player.getUniqueId(),
            player.name()));

    Component arrowComponent = Component.text(" » ").color(NamedTextColor.GOLD);

    Component chatMsgComponent = Component.text().color(messageColor)
        .append(legacyAmpersand().deserialize(event.getMessage())).build();
    this.plugin.getServer().broadcast(
        Component.empty().append(nameComponent).append(arrowComponent).append(chatMsgComponent));

    // TODO: find out how to do chat without cancelling the event
    // it seems that the new event, AsyncChatEvent takes / receives direct components
    // only remaining thing would be to replace formatting codes
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOWEST) // Runs foremost
  public void onPlayerJoin(final PlayerJoinEvent event) {
    Player player = event.getPlayer();
    this.plugin.getDataManager().registerPlayer(player);
    // Set the initial active time
    this.plugin.getDataManager().getData(player)
        .setLastActiveAt(System.currentTimeMillis());

    // NOTE: Title isn't cleared when the player leaves the server
    player.clearTitle();
    event.joinMessage(player.displayName().color(NamedTextColor.YELLOW).appendSpace()
        .append(Component.text("joined the game")));
    plugin.advancementTab.showTab(event.getPlayer());
  }

  @EventHandler
  public void onPlayerLogin(final PlayerLoginEvent event) {
    if (event.getResult() == PlayerLoginEvent.Result.ALLOWED
        || event.getResult() == PlayerLoginEvent.Result.KICK_OTHER) {
      return;
    }

    String message;
    switch (event.getResult()) {
      case KICK_BANNED ->
          message = ChatColor.RED + ChatColor.BOLD.toString() + "You’ve been banned :(\n\n"
              + ChatColor.RESET + ChatColor.WHITE
              + "If you believe this was a mistake, please DM " + ChatColor.AQUA
              + "@alx or @eli " + ChatColor.WHITE + "on Slack.";
      case KICK_FULL ->
          message = ChatColor.RED + ChatColor.BOLD.toString() + "The server is full!\n\n"
              + ChatColor.RESET + ChatColor.WHITE
              + "Sorry, it looks like there’s no more room. Please try again in ~20 minutes.";
      case KICK_WHITELIST ->
          message = ChatColor.RED + ChatColor.BOLD.toString() + "You’re not whitelisted!\n\n"
              + ChatColor.RESET + ChatColor.WHITE + "Join " + ChatColor.AQUA
              + "#minecraft " + ChatColor.WHITE + "on Slack and ping " + ChatColor.AQUA
              + "@alx or @eli " + ChatColor.WHITE + "to be added.";
      default -> message = event.getKickMessage();
    }
    event.setKickMessage(message);
  }

  @EventHandler
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    // Reset lastDamagedAt
    this.plugin.getDataManager().getData(event.getPlayer()).setLastDamagedAt(0);
  }

  @EventHandler(priority = EventPriority.MONITOR) // Runs very last
  public void onPlayerQuit(final PlayerQuitEvent event) {
    // NOTE: Title isn't cleared when the player leaves the server
    // event.getPlayer().resetTitle();
    event.quitMessage(event.getPlayer().displayName().color(NamedTextColor.YELLOW).appendSpace()
        .append(Component.text("left the game")));

    this.plugin.getDataManager().unregisterPlayer(event.getPlayer());
  }
}
