package com.hackclub.hccore.playermessages.loc;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class LocationExistsMessage {

  final static String minimsgSource = """
      <red>A location with that name already exists.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
