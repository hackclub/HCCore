package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class RecipSharedMessage {

  final static String minimsgSource = """
      <green><sender> has shared a location: <name> (<world> @ <x>, <y>, <z>).</green>""";

  public static Component get(String senderName, String name, String world, int x, int y, int z) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("sender", text(senderName)),
        Placeholder.component("name", text(name)), Placeholder.component("world", text(world)),
        Placeholder.component("x", text(x)), Placeholder.component("y", text(y)),
        Placeholder.component("z", text(z))));
  }

}
