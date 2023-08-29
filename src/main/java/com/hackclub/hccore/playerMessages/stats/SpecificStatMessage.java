package com.hackclub.hccore.playerMessages.stats;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SpecificStatMessage {

  final static String minimsgSource = """
      <player>'s <stat> statistic: <value>""";

  public static Component get(String playerName, String statName, String statValue) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("player", text(playerName)),
            Placeholder.component("stat", text(statName)),
            Placeholder.component("value", text(statValue))));
  }
}
