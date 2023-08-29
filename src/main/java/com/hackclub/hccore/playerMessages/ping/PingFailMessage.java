package com.hackclub.hccore.playerMessages.ping;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PingFailMessage {

  final static String minimsgSource = """
      <red>Failed to get <user> ping.</red>""";

  public static Component get(String user) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("user", text(user))));
  }

}
