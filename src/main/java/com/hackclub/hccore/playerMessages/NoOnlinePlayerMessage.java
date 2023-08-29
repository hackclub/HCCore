package com.hackclub.hccore.playerMessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class NoOnlinePlayerMessage {

  final static String minimsgSource = """
      <red>No online player with that name was found.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
