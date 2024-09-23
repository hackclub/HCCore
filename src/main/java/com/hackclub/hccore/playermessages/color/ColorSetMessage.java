package com.hackclub.hccore.playermessages.color;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ColorSetMessage {

  final static String minimsgSource = """
      <green>Your <type> color has been set to <color>.</green>""";

  public static Component get(String type, TextColor color) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("type", text(type)),
        Placeholder.component("color", text("this color").color(color))));
  }
}
