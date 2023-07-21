package com.hackclub.hccore.listeners;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playerMessages.WelcomeMessage;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.BanEntry;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
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
  public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
    event.setCancelled(true);
    Player player = event.getPlayer();

    // Apply the player's chat color to the message and translate color codes

    PlayerData data = this.plugin.getDataManager().getData(player);
    TextColor messageColor = data.getMessageColor();
//    TextColor nameColor = data.getNameColor();

    Component nameComponent = player.displayName().hoverEvent(
        net.kyori.adventure.text.event.HoverEvent.showEntity(player.getType(), player.getUniqueId(),
            player.name()));

    Component arrowComponent = text(" » ").color(GOLD);

    Component chatMsgComponent = text().color(messageColor)
        .append(legacyAmpersand().deserialize(event.getMessage())).build();
    this.plugin.getServer().broadcast(
        Component.empty().append(nameComponent).append(arrowComponent).append(chatMsgComponent));

    // TODO: find out how to do chat without cancelling the event
    // it seems that the new event, AsyncChatEvent takes / receives direct components
    // only remaining thing would be to replace formatting codes
  }

  public static boolean isSlackJoinAllowed(PlayerData data) {
    if (!HCCorePlugin.getPlugin(HCCorePlugin.class).getConfig()
        .getBoolean("settings.slack-link.enabled", false)) {
      return true;
    }
    if (!HCCorePlugin.getPlugin(HCCorePlugin.class).getConfig()
        .getBoolean("settings.slack-link.required", false)) {
      return true;
    }
    return data.getSlackId() != null;
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
    return
        text("You must link your Slack account to join the server!",
            style(RED, BOLD)).appendNewline().appendNewline().append(
            text("Please run ").color(NamedTextColor.WHITE)
                .append(text("/" + HCCorePlugin.getPlugin(HCCorePlugin.class).getConfig()
                    .get("settings.slack-link.base-command", "minecraft") + " link "
                    + code, style(GOLD)))
                .append(text(
                    " in the #minecraft channel in the Slack (https://slack.hackclub.com) to link your account."))
                .color(NamedTextColor.WHITE)).appendNewline().appendNewline().append(
            text("This code will expire after ").append(text(
                    HCCorePlugin.getPlugin(HCCorePlugin.class).getConfig()
                        .getInt("settings.slack-link.link-code-expiration", 60 * 10) + " seconds"))
                .append(text(".")).color(NamedTextColor.WHITE).decorate(
                    TextDecoration.ITALIC));
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
        player.displayName().hoverEvent(event.getPlayer()).color(NamedTextColor.YELLOW)
            .appendSpace()
            .append(text("joined the game")));
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
      case KICK_BANNED -> message = this.getBanMessage(event.getPlayer().getUniqueId().toString());
      case KICK_FULL -> message = text("The server is full!").color(RED)
          .decorate(BOLD).appendNewline().appendNewline().append(
              text(
                  "Sorry, it looks like there’s no more room. Please try again in ~20 minutes.").color(
                  NamedTextColor.WHITE));
      default -> message = event.kickMessage();
    }
    event.kickMessage(message);
  }

  @EventHandler
  public void onPlayerKick(final PlayerKickEvent event) {
    Component message;
    if (event.getCause() == Cause.BANNED) {
      message = this.getBanMessage(event.getPlayer().getUniqueId().toString());
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
        event.getPlayer().displayName().hoverEvent(event.getPlayer()).color(NamedTextColor.YELLOW)
            .appendSpace()
            .append(text("left the game")));

    this.plugin.getDataManager().unregisterPlayer(event.getPlayer());
  }

  private Component getBanMessage(String uuid) {
    BanEntry banEntry = this.plugin.getServer().getBanList(Type.NAME)
        .getBanEntry(uuid);
    String reason;
    if (banEntry != null) {
      reason = banEntry.getReason();
      if (reason != null) {
        if (!reason.equals("Banned by an operator.")) {
          reason = ": " + reason;
        } else {
          reason = " no specified reason";
        }
      } else {
        reason = " no specified reason";
      }
    } else {
      reason = " no specified reason";
    }

    return text("You've been banned for" + reason + " :(")
        .color(RED).decorate(
            BOLD).appendNewline().appendNewline().append(
            text("If you would like to appeal, please DM ").color(NamedTextColor.WHITE)).append(
            text("a Minecraft server admin (minecrafters team) ")
                .color(NamedTextColor.AQUA))
        .append(text("on Slack.").color(NamedTextColor.WHITE));
  }
}
