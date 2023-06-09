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
  final static String minimsgSource = """
      <b><green>Your MC account is linked to Slack!</green></b>
      <hover:show_text:'<b>Your MC account</b>
      MC UUID: <mcuuid>'><mcname></hover> <b><--></b> <hover:show_text:'<b>Your Slack account</b>
      Slack Name: <slackname>
      Slack ID: <slackid>'><slackuser></hover>""";

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
      plugin.getLogger().warning(
          "Player %s (%s) has no slack linked in linked slack msg".formatted(player.getName(),
              player.getUniqueId().toString()));
    }
    Component slackInvalid = text("Unlinked").color(RED).decorate(BOLD)
        .hoverEvent(text("Unlinked!").appendNewline()
            .append(text("This should not appear! Please report this.")));

    Component mcNameComponent = player.displayName();
    Component mcUuidComponent = text(player.getUniqueId().toString());
    Component slackNameComponent =
        linkedSlackUser == null ? slackInvalid : text(linkedSlackUser.getRealName());
    Component slackUserComponent =
        linkedSlackUser == null ? slackInvalid : text(linkedSlackUser.getName());
    Component slackIdComponent =
        linkedSlackUser == null ? slackInvalid : text(linkedSlackUser.getId());

    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("mcname", mcNameComponent),
            Placeholder.component("mcuuid", mcUuidComponent),
            Placeholder.component("slackname", slackNameComponent),
            Placeholder.component("slackuser", slackUserComponent),
            Placeholder.component("slackid", slackIdComponent)));
  }

}
