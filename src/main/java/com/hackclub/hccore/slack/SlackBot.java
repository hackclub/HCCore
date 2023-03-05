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
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
      String mainChannel = String.valueOf(getSlackChannel());
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

      TextComponent prefixComponent = new TextComponent("[Slack] ");
      prefixComponent.setColor(ChatColor.BLUE.asBungee());

      TextComponent nameComponent = new TextComponent(displayName);
      nameComponent.setColor(ChatColor.WHITE.asBungee());

      nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
          new Text(result.getProfile().getRealName())));

      TextComponent arrowComponent = new TextComponent(" » ");
      arrowComponent.setColor(ChatColor.GOLD.asBungee());

      TextComponent playerChatComponent =
          new TextComponent(ChatColor.translateAlternateColorCodes('&', text));

      plugin.getServer().spigot()
          .broadcast(prefixComponent, nameComponent, arrowComponent, playerChatComponent);
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
  public void onChat(AsyncPlayerChatEvent e) throws IOException {
    PlayerData player = plugin.getDataManager().getData(e.getPlayer());
    sendMessage(ChatColor.stripColor(e.getMessage()),
        getPlayerAvatarLink(player.player.getUniqueId().toString())
        , ChatColor.stripColor(player.getDisplayedName()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" + ChatColor.stripColor(player.getDisplayName()) + "* joined the game!",
        getServerAvatarLink()
        , "Console");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" + ChatColor.stripColor(player.getDisplayName()) + "* left the game!",
        getServerAvatarLink(),
        "Console");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDeath(PlayerDeathEvent e) throws IOException {
    sendMessage(e.getDeathMessage(), "https://cloud-4zgvoofbx-hack-club-bot.vercel.app/0image.png",
        "R.I.P.");
  }

  private String getSlackChannel() {
    return this.plugin.getConfig().getString("settings.slack-link.channel-id");
  }

  private String getBotToken() {
    return this.plugin.getConfig().getString("settings.slack-link.bot-token");
  }

  private String getAppToken() {
    return this.plugin.getConfig().getString("settings.slack-link.app-token");
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
