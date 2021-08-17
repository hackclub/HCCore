package com.hackclub.hccore.slack;

import java.io.*;
import java.lang.Exception;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
// import java.util.stream.Collectors;

// Internal Imports
import com.hackclub.hccore.HCCorePlugin;

// Slack Imports
import com.hackclub.hccore.PlayerData;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.response.users.profile.UsersProfileGetResponse;
import com.slack.api.model.event.MessageEvent;

// Bukkit/Bungee imports
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class SlackBot implements Listener {
    private HCCorePlugin plugin;
    private SocketModeApp socket;

    public SlackBot(HCCorePlugin plugin) throws  Exception {
        this.plugin = plugin;
        App app = new App();

        Pattern sdk = Pattern.compile(".*");
        app.message(sdk, (payload, ctx) -> {
            MessageEvent event = payload.getEvent();
            String text = event.getText();
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

            // System.out.println(text);
            // System.out.println(displayName);

            TextComponent prefixComponent = new TextComponent("[Slack] ");
            prefixComponent.setColor(ChatColor.BLUE.asBungee());

            TextComponent nameComponent = new TextComponent(displayName);
            nameComponent.setColor(ChatColor.WHITE.asBungee());
            // nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(result.getProfile().getRealName())));

            TextComponent arrowComponent = new TextComponent(" » ");
            arrowComponent.setColor(ChatColor.GOLD.asBungee());

            TextComponent playerChatComponent =
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
            playerChatComponent.setColor(ChatColor.WHITE.asBungee());
            plugin.getServer().spigot().broadcast(prefixComponent, nameComponent, arrowComponent, playerChatComponent);
            return ctx.ack();
        });

        SocketModeApp socket = new SocketModeApp(app);
        this.socket = socket;
        socket.start();
        sendMessage("*Server Started*", getServerAvatarLink(), "Console");
    }

    public void disconnect() throws Exception {
       sendMessage("*Server Stopped*", getServerAvatarLink(), "Console");
       this.socket.stop();
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) throws IOException {
        PlayerData player = plugin.getDataManager().getData(e.getPlayer());
        // channel.sendMessage(ChatColor.stripColor(player.getDisplayedName()) + " » " + ChatColor.stripColor(e.getMessage())).queue();
        sendMessage(ChatColor.stripColor(e.getMessage()), getPlayerAvatarLink(player.player.getUniqueId().toString()), ChatColor.stripColor(player.getDisplayedName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) throws IOException {
        Player player = e.getPlayer();
        /*
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setAuthor(ChatColor.stripColor(player.getDisplayName()) + " joined the game!", null, getAvatarUrl(player));
         channel.sendMessage(embed.build()).queue();
         */
        sendMessage("*" + ChatColor.stripColor(player.getDisplayName()) + "* joined the game!", getServerAvatarLink(), "Console");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) throws IOException {
        Player player = e.getPlayer();
        /*
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setAuthor(ChatColor.stripColor(player.getDisplayName()) + " left the game!", null, getAvatarUrl(player));
        channel.sendMessage(embed.build()).queue();
         */
        sendMessage("*" + ChatColor.stripColor(player.getDisplayName()) + "* left the game!", getServerAvatarLink(), "Console");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) throws IOException {
        /*
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLACK);
        embed.setAuthor(e.getDeathMessage(), null, getAvatarUrl(e.getEntity()));
        channel.sendMessage(embed.build()).queue();
         */
        sendMessage(e.getDeathMessage(), "https://files.slack.com/files-tmb/T0266FRGM-F02C5RT9CAC-0034a7ef06/image_720.png", "R.I.P.");
    }

    private long getSlackChannel() {
        return this.plugin.getConfig().getLong("settings.discord-link.channel-id");
    }

    private String getBotToken() {
        return this.plugin.getConfig().getString("settings.discord-link.bot-token");
    }

    void sendMessage(String msg, String iconURL, String username) throws IOException {
        String channel = String.valueOf(this.getSlackChannel());
        URL url = new URL("https://slack.com/api/chat.postMessage");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        String body = "{" +
                "\"channel\": \"" + channel + "\"," +
                "\"text\": \"" + msg + "\"," +
                "\"icon_url\": \"" + iconURL + "\"," +
                "\"username\": \"" + username + "\"" +
                "}";
        byte[] out = body.getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.setRequestProperty("Authorization", "Bearer " + getBotToken());
        http.connect();
        /*
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        InputStream inputStream = http.getInputStream();
        String text = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        System.out.println(text);
         */
    }

    public static String getPlayerAvatarLink(String uuid) {
        return "https://cravatar.eu/avatar/" + uuid;
    }

    public static String getServerAvatarLink() {
        return "https://files.slack.com/files-tmb/T0266FRGM-F029M783RFF-77e6a14fee/image-removebg-preview__1__480.png";
    }
}
