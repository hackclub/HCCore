package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SlackUsernameMessage {

  final static String minimsgSource = """
      <green>Slack username for <player>: <username></green>""";

  public static Component get(String playerName, String slackName) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("player", text(playerName)),
        Placeholder.component("username", text(slackName))));
  }
}
