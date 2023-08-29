package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class LocationRemovedMessage {

  final static String minimsgSource = """
      <green>Removed <location> from saved locations.</green>""";

  public static Component get(String location) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("location", text(location))));
  }

}
