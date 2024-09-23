package com.hackclub.hccore.playermessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SlackChatMessage {

  final static String minimsgSource = """
      <blue>[Slack]</blue> <playercomponent><gold> » </gold><message>""";

  public static Component get(Component playerComponent, String message) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("playercomponent", playerComponent),
            Placeholder.component("message", legacyAmpersand().deserialize(message))));
  }
}
