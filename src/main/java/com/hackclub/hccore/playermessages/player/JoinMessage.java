package com.hackclub.hccore.playermessages.player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class JoinMessage {

  final static String minimsgSource = """
      <player> <yellow>joined the game!</yellow>""";

  public static Component get(Component playerComponent) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("player", playerComponent)));
  }
}
