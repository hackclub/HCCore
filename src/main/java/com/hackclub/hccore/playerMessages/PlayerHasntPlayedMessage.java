package com.hackclub.hccore.playerMessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class PlayerHasntPlayedMessage {

  final static String minimsgSource = """
      <red>That player has not played on this server!</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
