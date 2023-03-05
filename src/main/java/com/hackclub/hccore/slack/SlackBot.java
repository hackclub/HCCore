package com.hackclub.hccore.slack;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.profile.UsersProfileGetResponse;
import com.slack.api.model.event.MessageBotEvent;
import com.slack.api.model.event.MessageEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SlackBot implements Listener {

  private final HCCorePlugin plugin;
  private final SocketModeApp socket;

  public SlackBot(HCCorePlugin plugin) throws Exception {
    this.plugin = plugin;
    App app = new App(AppConfig.builder().singleTeamBotToken(getBotToken()).build());

    Pattern sdk = Pattern.compile(".*");
    app.message(sdk, (payload, ctx) -> {
      MessageEvent event = payload.getEvent();
      String text = StringEscapeUtils.unescapeHtml4(event.getText());
      String channelId = event.getChannel();
      String mainChannel = getSlackChannel();
      if (!channelId.equals(mainChannel)) {
        return ctx.ack();
      }
      String userId = event.getUser();
      UsersProfileGetResponse result = ctx.client().usersProfileGet(r ->
          r
              .token(ctx.getBotToken())
              .user(userId)
      );
      String displayName = result.getProfile().getDisplayName();

      TextComponent prefixComponent = Component.text("[Slack] ")
          .color(NamedTextColor.BLUE);

      TextComponent nameComponent = Component.text(displayName)
          .color(NamedTextColor.WHITE)
          .hoverEvent(Component.text(result.getProfile().getRealName()));

      TextComponent arrowComponent = Component.text(" » ")
          .color(NamedTextColor.GOLD);

      TextComponent playerChatComponent = Component.text(
          ChatColor.translateAlternateColorCodes('&', text)).color(NamedTextColor.GRAY);

      plugin.getServer().broadcast(prefixComponent
          .append(nameComponent)
          .append(arrowComponent)
          .append(playerChatComponent)
      );

      return ctx.ack();
    });

    app.event(MessageBotEvent.class, (payload, ctx) -> ctx.ack());

    SocketModeApp socket = new SocketModeApp(getAppToken(), app);
    this.socket = socket;
    socket.startAsync();
    this.plugin.getLogger().info("HackCraft Slack started!");
    sendMessage("*Server Started*", getServerAvatarLink(), "Console");
  }

  public static String getPlayerAvatarLink(String uuid) {
    return "https://cravatar.eu/avatar/" + uuid;
  }

  public static String getServerAvatarLink() {
    return "https://assets.hackclub.com/icon-progress-square.png";
  }

  public void disconnect() throws Exception {
    sendMessage("*Server Stopped*", getServerAvatarLink(), "Console");
    this.socket.stop();
  }

  @EventHandler(ignoreCancelled = true)
  public void onChat(AsyncChatEvent e) throws IOException {
    PlayerData player = plugin.getDataManager().getData(e.getPlayer());
    sendMessage(
        PlainTextComponentSerializer.plainText().serialize(e.message()),
        getPlayerAvatarLink(player.player.getUniqueId().toString())
        ,PlainTextComponentSerializer.plainText().serialize(player.getDisplayedName()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" +
            PlainTextComponentSerializer.plainText().serialize(player.displayName())
            + "* joined the game!",
        getServerAvatarLink()
        , "Console");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" +
            PlainTextComponentSerializer.plainText().serialize(player.displayName())
            + "* left the game!",
        getServerAvatarLink(),
        "Console");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDeath(PlayerDeathEvent e) throws IOException {
    Component deathMessage = e.deathMessage();
    if (deathMessage == null) {
      return;
    }
    sendMessage(PlainTextComponentSerializer.plainText().serialize(deathMessage),
        "https://cloud-4zgvoofbx-hack-club-bot.vercel.app/0image.png",
        "R.I.P.");
  }

  private String getSlackChannel() {
    String id = this.plugin.getConfig().getString("settings.slack-link.channel-id");

    if (id == null) {
      throw new IllegalStateException("Slack channel ID is not set!");
    }

    return id;
  }

  private String getBotToken() {
    String botToken = this.plugin.getConfig().getString("settings.slack-link.bot-token");

    if (botToken == null) {
      throw new IllegalStateException("Slack bot token is not set!");
    }

    return botToken;
  }

  private String getAppToken() {
    String appToken =  this.plugin.getConfig().getString("settings.slack-link.app-token");

    if (appToken == null) {
      throw new IllegalStateException("Slack app token is not set!");
    }

    return appToken;
  }

  void sendMessage(String msg, String iconURL, String username) throws IOException {
    MethodsClient client = Slack.getInstance().methods();

    try {
      var res = client.chatPostMessage(r -> r
          .token(getBotToken())
          .channel(this.getSlackChannel())
          .text(msg)
          .iconUrl(iconURL)
          .username(username)
      );

      if (!res.isOk()) {
        this.plugin.getLogger().log(Level.WARNING, "SlackBot failed to send message: " + res);
      }
    } catch (SlackApiException e) {
      e.printStackTrace();
    }
  }
}
