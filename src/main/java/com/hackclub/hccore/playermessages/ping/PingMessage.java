package com.hackclub.hccore.playermessages.ping;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PingMessage {

  final static String minimsgSource = """
      <green><user> ping is <ping> ms.</green>""";

  public static Component get(String user, int ping) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("user", text(user)), Placeholder.component("ping", text(ping))));
  }

}
