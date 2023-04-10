package com.hackclub.hccore.playerMessages;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import com.hackclub.hccore.DataManager;
import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.slack.SlackBot;
import com.slack.api.model.User;
import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class LinkedSlackMessage {

  // TODO add message
  // placeholders:
  // playername
  // slackname
  // slackuid
  final static String minimsgSource = """
      """;

  public static Component get(Player player) {
    HCCorePlugin plugin = HCCorePlugin.getInstance();
    DataManager dataManager = plugin.getDataManager();
    PlayerData playerData = dataManager.getData(player);
    SlackBot slackBot = plugin.getSlackBot();
    User linkedSlackUser;
    try {
      linkedSlackUser = slackBot.getUserInfo(playerData.getSlackId());
    } catch (IOException e) {
      linkedSlackUser = null;
    }
    plugin.getLogger().warning(
        "Player %s (%s) has no slack linked in linked slack msg".formatted(player.getName(),
            player.getUniqueId().toString()));
    Component slackInvalid = text("Unlinked").color(RED).decorate(BOLD)
        .hoverEvent(text("Unlinked!").appendNewline()
            .append(text("This should not appear! Please report this.")));

    Component playerComponent = player.displayName();
    Component slackNameComponent =
        linkedSlackUser == null ? slackInvalid : text(linkedSlackUser.getName());
    Component slackUidComponent =
        linkedSlackUser == null ? slackInvalid : text(linkedSlackUser.getId());

    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("playername", playerComponent),
            Placeholder.component("slackname", slackNameComponent),
            Placeholder.component("slackuid", slackUidComponent)));
  }

}
