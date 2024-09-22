package com.hackclub.hccore.playermessages.player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ChatMessage {

  final static String minimsgSource = """
      <playercomponent><gold> » </gold><message>""";

  public static Component get(Component playerComponent, String message, TextColor messageColor) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("playercomponent", playerComponent),
            Placeholder.component("message", legacyAmpersand().deserialize(message).color(messageColor))));
  }
}
