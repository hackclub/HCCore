package com.hackclub.hccore.playermessages.stats;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class YourStatsMessage {

  final static String minimsgSource = """
      <b><green><player> stats:</green></b>""";

  public static Component get(String playerName) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("player", text(playerName))));
  }
}
