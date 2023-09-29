package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SendSharedMessage {

  final static String minimsgSource = """
      <green>Shared <name> with <player>.</green>""";

  public static Component get(String name, String playerName) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("name", text(name)),
        Placeholder.component("player", text(playerName))));
  }

}
