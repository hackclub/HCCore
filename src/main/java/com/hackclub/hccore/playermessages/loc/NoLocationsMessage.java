package com.hackclub.hccore.playermessages.loc;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class NoLocationsMessage {

  final static String minimsgSource = """
      <red>You have no saved locations.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
