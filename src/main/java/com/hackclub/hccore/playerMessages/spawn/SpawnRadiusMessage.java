package com.hackclub.hccore.playerMessages.spawn;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SpawnRadiusMessage {

  final static String minimsgSource = """
      <red>You need to within <radius> blocks from spawn to use this command. Currently, you're <dist> too far.</red>""";

  public static Component get(int radius, int dist) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("radius", text(radius)), Placeholder.component("dist", text(dist))));
  }

}
