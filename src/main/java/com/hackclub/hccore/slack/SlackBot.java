package com.hackclub.hccore.slack;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.events.player.PlayerAFKStatusChangeEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import com.slack.api.model.User;
import com.slack.api.model.event.MessageBotEvent;
import com.slack.api.model.event.MessageChannelJoinEvent;
import com.slack.api.model.event.MessageDeletedEvent;
import com.slack.api.model.event.MessageEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SlackBot implements Listener {

  public static final String playerDeathMessageAvatarUrl = "https://cloud-4zgvoofbx-hack-club-bot.vercel.app/0image.png";
  public static final String playerAfkEnterAvatarUrl = "https://cloud-pt6yc0dyx-hack-club-bot.vercel.app/0hc-afk-icon.png";
  public static final String playerAfkLeaveAvatarUrl = "https://cloud-pt6yc0dyx-hack-club-bot.vercel.app/0hc-afk-icon.png";
  public static final String serverConsoleAvatarUrl = "https://cloud-6lujjsrt6-hack-club-bot.vercel.app/0console_edited.png";
  public static final String serverAvatarLink = "https://assets.hackclub.com/icon-progress-square.png";
  public static final String playerServerLeaveAvatarUrl = "https://cloud-if9tepzbn-hack-club-bot.vercel.app/0hccoreleave.png";
  public static final String playerServerJoinAvatarUrl = "https://cloud-if9tepzbn-hack-club-bot.vercel.app/1hccorejoin.png";
  public static final String playerAdvancementAvatarUrl = "https://cloud-obk2f29h4-hack-club-bot.vercel.app/0achievement.png";
  private final HCCorePlugin plugin;
  private final SocketModeApp socket;
  private final String commandBase;
  private final HashMap<UUID, String> mcUuidVerificationCodes = new HashMap<>();

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

    app.blockAction("verify-link", (req, ctx) -> {
      String mcUuid = req.getPayload().getActions().get(0).getValue();
      OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(mcUuid));
      String channelId = req.getPayload().getChannel().getId();
      String messageTs = req.getPayload().getMessage().getTs();
      MethodsClient client = ctx.client();

      if (!player.hasPlayedBefore()) {
        client.chatPostMessage(
            r -> r.token(ctx.getBotToken()).channel(channelId).threadTs(messageTs).text(
                "Hmm, it seems that the player you're trying to link doesn't exist? Please report this to a minecraft server admin"));
        this.plugin.getLogger()
            .warning("Player " + mcUuid + " doesn't exist, but tried to link their account");
        return ctx.ack();
      }

      this.plugin.getDataManager().getData(player).setSlackId(req.getPayload().getUser().getId());

      try {
        client.chatPostMessage(
            r -> r.token(ctx.getBotToken()).channel(channelId).threadTs(messageTs)
                .text("Your accounts have been linked!"));
        if (player.isOnline()) {
          player.getPlayer().sendMessage(
              Component.text("Your accounts have been linked!").color(NamedTextColor.GREEN));
        }
      } catch (IOException | SlackApiException e) {
        e.printStackTrace();
      }

      return ctx.ack();
    });

    app.blockAction("deny-link", (req, ctx) -> {
      String mcUuid = req.getPayload().getActions().get(0).getValue();
      OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(mcUuid));
      String channelId = req.getPayload().getChannel().getId();
      String messageTs = req.getPayload().getMessage().getTs();
      MethodsClient client = ctx.client();

      if (!player.hasPlayedBefore()) {
        client.chatPostMessage(
            r -> r.token(ctx.getBotToken()).channel(channelId).threadTs(messageTs).text(
                "Hmm, it seems that the player you're trying to deny linking doesn't exist? Please report this to a minecraft server admin"));
        this.plugin.getLogger().warning(
            "Player " + mcUuid + " doesn't exist, but tried to deny linking their account");
        return ctx.ack();
      }

      try {
        client.chatPostMessage(
            r -> r.token(ctx.getBotToken()).channel(channelId).threadTs(messageTs)
                .text("Denied link request"));

        if (player.isOnline()) {
          player.getPlayer().sendMessage(
              Component.text("The request to link the account was denied")
                  .color(NamedTextColor.RED));
        }
      } catch (IOException | SlackApiException e) {
        e.printStackTrace();
      }

      return ctx.ack();
    });

    app.event(MessageBotEvent.class, (payload, ctx) -> ctx.ack());

    app.event(MessageDeletedEvent.class, (payload, ctx) -> ctx.ack());

    app.event(MessageChannelJoinEvent.class, (payload, ctx) -> ctx.ack());

    CommandDispatcher<SlashCommandRequest> dispatcher = new CommandDispatcher<>();
    dispatcher.register(
        LiteralArgumentBuilder.<SlashCommandRequest>literal("/%s".formatted(commandBase)).then(
                LiteralArgumentBuilder.<SlashCommandRequest>literal("players").executes(context -> {
                  Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
                  StringBuilder message = new StringBuilder();
                  if (onlinePlayers.size() == 0) {
                    message.append("There are currently no players online");
                  } else {
                    message.append("*Players online* (%d/%d)\n\n".formatted(onlinePlayers.size(),
                        plugin.getServer().getMaxPlayers()));
                    for (Player player : onlinePlayers) {
                      String displayName = plainText().serialize(player.displayName());
                      String name = player.getName();
                      String line = "%s%s\n".formatted(displayName,
                          (name.equals(displayName)) ? "" : (", AKA " + name));
                      message.append(line);
                    }
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
                })).then(LiteralArgumentBuilder.<SlashCommandRequest>literal("lookup")
                .then(RequiredArgumentBuilder.<SlashCommandRequest, String>argument("mention",
                    StringArgumentType.greedyString()).executes(context -> {
                  String mention = StringArgumentType.getString(context, "mention");
                  String id;

                  // User mention
                  if (mention.startsWith("<@") && mention.endsWith(">")) {
                    int pipeIdx = mention.indexOf('|');
                    if (pipeIdx == -1) {
                      id = mention.substring(2, mention.length() - 1);
                    } else {
                      id = mention.substring(2, pipeIdx);
                    }
                  } else {
                    // Try user id
                    id = mention;
                  }

                  try {
                    PlayerData data = this.plugin.getDataManager()
                        .findData(pData -> pData.getSlackId().equals(id));

                    if (data == null) {
                      context.getSource().getContext().respond("No linked user was found");
                      return 1;
                    }

                    context.getSource().getContext().respond(
                        "The linked user is %s".formatted(data.getUsableName()));
                  } catch (IOException e) {
                    e.printStackTrace();
                  }

                  return 1;
                })).executes(context -> {
                  try {
                    context.getSource().getContext()
                        .respond("Missing argument: slack user mention or id");
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  return 1;
                }))
            .then(LiteralArgumentBuilder.<SlashCommandRequest>literal("link").then(RequiredArgumentBuilder.<SlashCommandRequest, String>argument("code",
                StringArgumentType.greedyString()).executes(context -> {
              String code = StringArgumentType.getString(context, "code");
              UUID mcUuid = null;

              for (Entry<UUID, String> entry : mcUuidVerificationCodes.entrySet()) {
                if (Objects.equals(code, entry.getValue())) {
                  mcUuid = entry.getKey();
                  break;
                }
              }

              try {
                if (mcUuid == null) {
                  context.getSource().getContext().respond("Invalid code");
                } else {
                  OfflinePlayer player = Bukkit.getOfflinePlayer(mcUuid);
                  PlayerData data = this.plugin.getDataManager().getData(player);

                  if (data == null) {
                    context.getSource().getContext().respond(
                        "Error: this player has not yet logged in");
                    return 1;
                  }

                  if (data.getSlackId() != null) {
                    context.getSource().getContext().respond(
                        "This minecraft account is already linked to %s".formatted(
                            data.getUsableName()));
                    return 1;
                  }

                  data.setSlackId(context.getSource().getContext().getRequestUserId());
                  data.save();
                  mcUuidVerificationCodes.remove(mcUuid);

                  // TODO: Make this message better to include rules and such
                  context.getSource().getContext().respond(
                      "Successfully linked your minecraft account to your slack account! You may now join the server.");

                }
                return 1;
              } catch (IOException e) {
                e.printStackTrace();
              }

              return 1;
            })).executes(context -> {
              try {
                List<String> usage = Arrays.asList(dispatcher.getAllUsage(dispatcher.getRoot(), null,
                    false));
                context.getSource().getContext()
                    .respond("No arguments given\nPossible commands:\n%s".formatted(
                        usage.stream().reduce((s, s2) -> s + "\n" + s2).orElse("")));
              } catch (IOException e) {
                e.printStackTrace();
              }
              return 1;
            })));

    app.command("/%s".formatted(commandBase), ((slashCommandRequest, ctx) -> {
      String command = slashCommandRequest.getPayload().getCommand() + (
          (slashCommandRequest.getPayload().getText().isEmpty()) ? ""
              : (" " + slashCommandRequest.getPayload().getText()));
      plugin.getLogger().info(
          "Received slack command from %s: \"%s\"".formatted(ctx.getRequestUserId(), command));
      try {
        dispatcher.execute(command, slashCommandRequest);
      } catch (CommandSyntaxException e) {
        List<String> usage = Arrays.asList(dispatcher.getAllUsage(dispatcher.getRoot(), null,
            false));
        ctx.respond("Parsing error: %s\nPossible commands:\n%s".formatted(e.getMessage(),
            usage.stream().reduce((s, s2) -> s + "\n" + s2).orElse("")));
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

  @EventHandler
  public void onAdvancementDone(PlayerAdvancementDoneEvent e) throws IOException {
    PlayerData data = this.plugin.getDataManager().getData(e.getPlayer());
    Advancement advancement = e.getAdvancement();

    // Filter out hidden advancements and recipes
    if (advancement.getDisplay() == null) {
      return;
    }
    if (!advancement.getDisplay().doesAnnounceToChat()) {
      return;
    }

    String advancementName = PlainTextComponentSerializer.plainText()
        .serialize(advancement.getDisplay().title());

    String advancementType;
    switch (advancement.getDisplay().frame()) {
      case GOAL -> advancementType = "goal";
      case CHALLENGE -> advancementType = "challenge";
      default -> advancementType = "advancement";
    }

    sendMessage("%s has completed the %s *%s*".formatted(data.getUsableName(), advancementType,
        advancementName), playerAdvancementAvatarUrl, "Advancement");
  }

  public void onCustomAdvancementProgressed(ProgressionUpdateEvent e) {
    UUID uuid = e.getTeamProgression().getAMember();
    if (uuid == null) {
      return;
    }
    Player player = Bukkit.getPlayer(e.getTeamProgression().getAMember());
    if (player == null) {
      return;
    }

    PlayerData data = this.plugin.getDataManager().getData(player);
    AdvancementKey key = e.getAdvancementKey();
    com.fren_gor.ultimateAdvancementAPI.advancement.Advancement advancement =
        this.plugin.advancementTab.getAdvancement(
            key);
    if (advancement == null) {
      return;
    }
    if (e.getNewProgression() < advancement.getMaxProgression()) {
      return;
    }
    String advancementName = advancement.getDisplay().getTitle();

    String advancementType;
    switch (advancement.getDisplay().getFrame()) {
      case GOAL -> advancementType = "goal";
      case CHALLENGE -> advancementType = "challenge";
      default -> advancementType = "advancement";
    }

    try {
      sendMessage("%s has completed the %s *%s*".formatted(data.getUsableName(), advancementType,
          advancementName), playerAdvancementAvatarUrl, "Advancement");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
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

  public User getUserInfo(String id) throws IOException {
    MethodsClient client = this.socket.getApp().getClient();
    try {
      var res = client.usersInfo(r -> r.token(getBotToken()).user(id));

      return res.getUser();
    } catch (SlackApiException e) {
      return null;
    }
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

  public boolean sendVerificationMessage(String id, String mcName, String uuid) throws IOException {
    MethodsClient client = this.socket.getApp().getClient();

    try {
      var res = client.chatPostMessage(r -> r.token(getBotToken()).channel(id).text(
              "A player on the Hack Club Minecraft server with username " + mcName
                  + " (UUID " + uuid + ") is trying to link to your Slack account")
          .blocks(asBlocks(section(s -> s.text(
              markdownText("A player on the Hack Club Minecraft server with username *" + mcName
                  + "*" + " (UUID `" + uuid + "`) is trying to link to " + "your "
                  + "Slack account. If you are this player, click the \"Verify\" button. If you "
                  + "are " + "not this player, click the \"Deny\" button."))), actions(
              actions -> actions.elements(asElements(button(b -> b.actionId("verify-link").text(
                  com.slack.api.model.block.composition.BlockCompositions.plainText(
                      pt -> pt.text("Verify"))).value(uuid).style("primary")), button(
                  b -> b.actionId("deny-link").text(
                      com.slack.api.model.block.composition.BlockCompositions.plainText(
                          pt -> pt.text("Deny"))).value(uuid).style("danger"))))))));

      if (!res.isOk()) {
        this.plugin.getLogger()
            .log(Level.WARNING, "SlackBot failed to send verification message: " + res);
        return false;
      }

      return true;
    } catch (SlackApiException e) {
      e.printStackTrace();
      return false;
    }
  }

  public String generateVerificationCode(UUID mcUuid) {
    if (mcUuidVerificationCodes.containsKey(mcUuid)) {
      return mcUuidVerificationCodes.get(mcUuid);
    } else {
      String code = UUID.randomUUID().toString().substring(0, 6);
      mcUuidVerificationCodes.put(mcUuid, code);
      return code;
    }
  }
}
