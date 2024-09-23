package com.hackclub.hccore.playermessages.afk;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class NotAFKMessage {

  final static String minimsgSource = """
      <i><player> is no longer AFK.</i>""";

  public static Component get(String playerName, TextColor color) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("player", text(playerName).color(color))));
  }
}
