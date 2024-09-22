package com.hackclub.hccore.playermessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;

public class LocationListMessage {

  final static String minimsgSource = """
      <b><green>Your saved locations (<size>):</green></b>
      <locations>""";

  public static Component get(Map<String, Location> savedLocations) {
    Component locations = text("");
    for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
      Location savedLocation = entry.getValue();
      locations = locations.appendNewline().append(text(
          "- " + entry.getKey() + ": " + savedLocation.getWorld().getName() + " @ "
              + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
              + savedLocation.getBlockZ()));
    }

    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("size", text(savedLocations.size())),
        Placeholder.component("locations", locations)));
  }

}
