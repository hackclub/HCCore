package com.hackclub.hccore.playermessages.stats;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class StatMessage {

  final static String minimsgSource = """
      - <stat>: <value>""";

  public static Component get(String statName, String statValue) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("stat", text(statName)),
        Placeholder.component("value", text(statValue))));
  }
}
