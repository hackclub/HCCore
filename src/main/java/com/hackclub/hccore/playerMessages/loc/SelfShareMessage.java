package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SelfShareMessage {

  final static String minimsgSource = """
      <red>You can't share a location with yourself!</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
