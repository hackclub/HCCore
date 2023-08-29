package com.hackclub.hccore.playerMessages.color;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ColorResetMessage {

  final static String minimsgSource = """
      <green>Your <type> color has been reset.</green>""";

  public static Component get(String type) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("type", text(type))));
  }

}
