package com.hackclub.hccore.playerMessages.stats;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class InvalidStatMessage {

  final static String minimsgSource = """
      <red>Not a valid statistic.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
