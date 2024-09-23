package com.hackclub.hccore.playermessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;

public class LocationAddMessage {

  final static String minimsgSource = """
      <green>Added <name> (<world> @ <x>, <y>, <z>) to saved locations.</green>""";

  public static Component get(String name, String world, Location location) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("name", text(name)), Placeholder.component("world", text(world)),
        Placeholder.component("x", text(String.valueOf(location.getBlockX()))),
        Placeholder.component("y", text(String.valueOf(location.getBlockY()))),
        Placeholder.component("z", text(String.valueOf(location.getBlockZ())))));
  }

}
