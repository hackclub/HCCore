package com.hackclub.hccore.listeners;

import static net.kyori.adventure.text.Component.text;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.enums.Emotes;
import com.hackclub.hccore.playermessages.WelcomeMessage;
import com.hackclub.hccore.playermessages.player.BanMessage;
import com.hackclub.hccore.playermessages.player.ChatMessage;
import com.hackclub.hccore.playermessages.player.JoinMessage;
import com.hackclub.hccore.playermessages.player.LeaveMessage;
import com.hackclub.hccore.playermessages.player.MustLinkMessage;
import com.hackclub.hccore.playermessages.player.ServerFullMessage;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

  private final HCCorePlugin plugin = HCCorePlugin.getInstance();

  @EventHandler
  public void onEntityDamage(final EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    this.plugin.getDataManager().getData(player).setLastDamagedAt(System.currentTimeMillis());
  }

  @EventHandler
  public void onPlayerDeath(final PlayerDeathEvent event) {
    Component deathMessage = event.deathMessage();
    if (deathMessage == null) {
      return;
    }
    String message = PlainTextComponentSerializer.plainText().serialize(deathMessage);

    message = message.replace(event.getEntity().getName(),
        PlainTextComponentSerializer.plainText().serialize(event.getEntity().displayName()));

    Player killer = event.getEntity().getKiller();
    if (killer != null) {
      message = message.replace(killer.getName(),
          PlainTextComponentSerializer.plainText().serialize(killer.displayName()));
    }

    event.deathMessage(text(message));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onAsyncChat(final AsyncChatEvent event) {
    event.renderer((source, sourceDisplayName, message, viewer) -> {
      PlayerData data = this.plugin.getDataManager().getData(source);
      TextColor messageColor = data.getMessageColor();
//    TextColor nameColor = data.getNameColor();

      String rawMessage = ((TextComponent) event.message()).content();

      // Apply the player's chat color to the message and translate color codes

      Component nameComponent = source.displayName().hoverEvent(
          net.kyori.adventure.text.event.HoverEvent.showEntity(source.getType(), source.getUniqueId(),
              source.name()));

      return ChatMessage.get(nameComponent, Emotes.parseString(rawMessage), messageColor);
    });
  }

  public static boolean isSlackJoinAllowed(PlayerData data) {
    HCCorePlugin plugin = HCCorePlugin.getPlugin(HCCorePlugin.class);

    if (!plugin.getConfig()
        .getBoolean("settings.slack-link.enabled", false)) {
      return true;
    }
    if (!plugin.getConfig()
        .getBoolean("settings.slack-link.required", false)) {
      return true;
    }

    String id = data.getSlackId();

    return !plugin.getSlackBot().isDeactivated(id);
  }


  @EventHandler(priority = EventPriority.LOWEST) // Runs foremost
  public void onPrePlayerLogin(final AsyncPlayerPreLoginEvent event) {
    UUID playerUUID = event.getUniqueId();
    PlayerData data = this.plugin.getDataManager()
        .getData(Bukkit.getServer().getOfflinePlayer(playerUUID));
    data.load();

    if (!isSlackJoinAllowed(data)) {
      // Check for slack link
      this.plugin.getLogger()
          .info("Preventing " + event.getName() + "'s join because they are not linked");
      Component kickMessage = getSlackLinkMessage(playerUUID);
      event.disallow(Result.KICK_WHITELIST, kickMessage);
    }
  }

  public static Component getSlackLinkMessage(UUID playerUUID) {
    String code = HCCorePlugin.getPlugin(HCCorePlugin.class).getSlackBot()
        .generateVerificationCode(playerUUID);
    int codeExpires = HCCorePlugin.getPlugin(HCCorePlugin.class).getConfig()
        .getInt("settings.slack-link.link-code-expiration", 60 * 10);
    String baseCommand =
        (String)HCCorePlugin.getInstance().getConfig().get("settings.slack-link.base-command", "minecraft");
    return MustLinkMessage.get(baseCommand, code, codeExpires);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(final PlayerJoinEvent event) {
    Player player = event.getPlayer();
    this.plugin.getDataManager().registerPlayer(player);
    this.plugin.getDataManager().getData(player)
        .setLastActiveAt(System.currentTimeMillis());

    // NOTE: Title isn't cleared when the player leaves the server
    player.clearTitle();
    event.joinMessage(
        JoinMessage.get(player.displayName().hoverEvent(event.getPlayer())));
    plugin.advancementTab.showTab(player);
  }

  @EventHandler
  public void onFirstJoin(final PlayerJoinEvent joinEvent) {
    Player player = joinEvent.getPlayer();
    if (player.hasPlayedBefore()) {
      return;
    }
    player.sendMessage(WelcomeMessage.get());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFailedLogin(final PlayerLoginEvent event) {
    if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
      return;
    }

    Component message;
    switch (event.getResult()) {
      case KICK_BANNED -> message = BanMessage.get(event.getPlayer().getUniqueId().toString());
      case KICK_FULL -> message = ServerFullMessage.get();
      default -> message = event.kickMessage();
    }
    event.kickMessage(message);
  }

  @EventHandler
  public void onPlayerKick(final PlayerKickEvent event) {
    Component message;
    if (event.getCause() == Cause.BANNED) {
      message = BanMessage.get(event.getPlayer().getUniqueId().toString());
    } else {
      message = event.reason();
    }

    event.reason(message);
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
    event.quitMessage(
        LeaveMessage.get(
            event.getPlayer().displayName().hoverEvent(event.getPlayer())));

    this.plugin.getDataManager().unregisterPlayer(event.getPlayer());
  }
}
