package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class HasLocationMessage {

  final static String minimsgSource = """
      <red><player> already has a location called <name></red>""";

  public static Component get(String playerName, String name) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("player", text(playerName)),
        Placeholder.component("name", text(name))));
  }

}
