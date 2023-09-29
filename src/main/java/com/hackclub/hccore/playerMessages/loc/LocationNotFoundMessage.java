package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class LocationNotFoundMessage {

  final static String minimsgSource = """
      <red>No location with that name was found.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
