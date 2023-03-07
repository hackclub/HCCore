package com.hackclub.hccore.slack;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.events.player.PlayerAFKStatusChangeEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.profile.UsersProfileGetResponse;
import com.slack.api.model.event.MessageBotEvent;
import com.slack.api.model.event.MessageChannelJoinEvent;
import com.slack.api.model.event.MessageDeletedEvent;
import com.slack.api.model.event.MessageEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SlackBot implements Listener {

  private final HCCorePlugin plugin;
  private final SocketModeApp socket;
  private final String commandBase;

  public static final String playerDeathMessageAvatarUrl = "https://cloud-4zgvoofbx-hack-club-bot.vercel.app/0image.png";
  public static final String playerAfkEnterAvatarUrl = "https://cloud-pt6yc0dyx-hack-club-bot.vercel.app/0hc-afk-icon.png";
  public static final String playerAfkLeaveAvatarUrl = "https://cloud-pt6yc0dyx-hack-club-bot.vercel.app/0hc-afk-icon.png";
  public static final String serverConsoleAvatarUrl = "https://cloud-6lujjsrt6-hack-club-bot.vercel.app/0console_edited.png";
  public static final String serverAvatarLink = "https://assets.hackclub.com/icon-progress-square.png";
  public static final String playerServerLeaveAvatarUrl = "https://cloud-if9tepzbn-hack-club-bot.vercel.app/0hccoreleave.png";
  public static final String playerServerJoinAvatarUrl = "https://cloud-if9tepzbn-hack-club-bot.vercel.app/1hccorejoin.png";

  public SlackBot(HCCorePlugin plugin) throws Exception {
    this.plugin = plugin;
    App app = new App(AppConfig.builder().singleTeamBotToken(getBotToken()).build());
    commandBase = plugin.getConfig().getString("settings.slack-link.base-command", "minecraft");

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
      UsersProfileGetResponse result = ctx.client()
          .usersProfileGet(r -> r.token(ctx.getBotToken()).user(userId));
      String displayName = result.getProfile().getDisplayName();

      TextComponent prefixComponent = Component.text("[Slack] ").color(NamedTextColor.BLUE);

      TextComponent nameComponent = Component.text(displayName).color(NamedTextColor.WHITE)
          .hoverEvent(Component.text(result.getProfile().getRealName()));

      TextComponent arrowComponent = Component.text(" » ").color(NamedTextColor.GOLD);

      TextComponent playerChatComponent = Component.text(
          ChatColor.translateAlternateColorCodes('&', text)).color(NamedTextColor.GRAY);

      plugin.getServer().broadcast(
          prefixComponent.append(nameComponent).append(arrowComponent).append(playerChatComponent));

      return ctx.ack();
    });

    app.event(MessageBotEvent.class, (payload, ctx) -> ctx.ack());

    app.event(MessageDeletedEvent.class, (payload, ctx) -> ctx.ack());

    app.event(MessageChannelJoinEvent.class, (payload, ctx) -> ctx.ack());

    CommandDispatcher<SlashCommandRequest> dispatcher = new CommandDispatcher<>();
    // TODO change to get base command from config.yml
    dispatcher.register(
        LiteralArgumentBuilder.<SlashCommandRequest>literal("/%s".formatted(commandBase))
            .then(
                LiteralArgumentBuilder.<SlashCommandRequest>literal("players").executes(context -> {
                  StringBuilder message = new StringBuilder(
                      "Players online (%d/%d)\n\n".formatted(
                          plugin.getServer().getOnlinePlayers().size(),
                          plugin.getServer().getMaxPlayers()));
                  for (Player player : plugin.getServer().getOnlinePlayers()) {
                    String displayName = plainText().serialize(player.displayName());
                    String name = player.getName();
                    String line = "%s%s\n".formatted(displayName,
                        (name.equals(displayName)) ? "" : (", AKA " + name));
                    message.append(line);
                  }

                  try {
                    ChatPostMessageResponse response = context.getSource().getContext()
                        .say(message.toString());
                    if (!response.isOk()) {
                      context.getSource().getContext().respond(message.toString());
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                  } catch (SlackApiException e) {
                    throw new RuntimeException(e);
                  }
                  return 1;
                })).executes(context -> {
              try {
                context.getSource().getContext()
                    .respond("no arguments given\ntry /%s players".formatted(commandBase));
              } catch (IOException e) {
                e.printStackTrace();
              }
              return 1;
            }));

    app.command("/%s".formatted(commandBase), ((slashCommandRequest, ctx) -> {
      String command = slashCommandRequest.getPayload().getCommand() + (
          (slashCommandRequest.getPayload().getText().isEmpty()) ? ""
              : (" " + slashCommandRequest.getPayload().getText()));
      plugin.getLogger().info(
          "received slack command from %s: \"%s\"".formatted(ctx.getRequestUserId(), command));
      try {
        dispatcher.execute(command, slashCommandRequest);
      } catch (CommandSyntaxException e) {
        ctx.respond("parsing error: %s\ntry /%s players".formatted(e.getMessage(), commandBase));
        e.printStackTrace();
      }
      return ctx.ack();
    }));

    SocketModeApp socket = new SocketModeApp(getAppToken(), app);
    this.socket = socket;
    socket.startAsync();
    this.plugin.getLogger().info("HackCraft Slack started!");
    sendMessage(":large_green_circle: *Server Started*", serverConsoleAvatarUrl, "Console");
  }

  @Contract(pure = true)
  public static @NotNull String getPlayerAvatarLink(String uuid) {
    return "https://cravatar.eu/avatar/" + uuid + "/512";
  }

  public void disconnect() throws Exception {
    sendMessage(":tw_octagonal_sign: *Server Stopped*", serverConsoleAvatarUrl, "Console");
    this.socket.stop();
  }


  @EventHandler
  public void onChat(AsyncChatEvent e) throws IOException {
    PlayerData player = plugin.getDataManager().getData(e.getPlayer());
    sendMessage(plainText().serialize(e.message()),
        getPlayerAvatarLink(player.player.getUniqueId().toString()),
        plainText().serialize(player.getDisplayedName()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" + plainText().serialize(player.displayName()) + "* joined the game!",
        playerServerJoinAvatarUrl, "Join");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent e) throws IOException {
    Player player = e.getPlayer();
    sendMessage("*" + plainText().serialize(player.displayName()) + "* left the game!",
        playerServerLeaveAvatarUrl, "Leave");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDeath(PlayerDeathEvent e) throws IOException {
    Component deathMessage = e.deathMessage();
    if (deathMessage == null) {
      return;
    }
    sendMessage(plainText().serialize(deathMessage), playerDeathMessageAvatarUrl, "R.I.P.");
  }

  @EventHandler
  public void onAfkChange(PlayerAFKStatusChangeEvent e) throws IOException {
    boolean nowAfk = e.getNewValue();
    PlayerData data = this.plugin.getDataManager().getData(e.getPlayer());
    sendMessage("%s is ".formatted(data.getUsableName()) + (nowAfk ? "now" : "no longer") + " AFK",
        nowAfk ? playerAfkEnterAvatarUrl : playerAfkLeaveAvatarUrl, "AFK");
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
    String appToken = this.plugin.getConfig().getString("settings.slack-link.app-token");

    if (appToken == null) {
      throw new IllegalStateException("Slack app token is not set!");
    }

    return appToken;
  }

  void sendMessage(String msg, String iconURL, String username) throws IOException {
    MethodsClient client = Slack.getInstance().methods();

    try {
      var res = client.chatPostMessage(
          r -> r.token(getBotToken()).channel(this.getSlackChannel()).text(msg).iconUrl(iconURL)
              .username(username));

      if (!res.isOk()) {
        this.plugin.getLogger().log(Level.WARNING, "SlackBot failed to send message: " + res);
      }
    } catch (SlackApiException e) {
      e.printStackTrace();
    }
  }
}
