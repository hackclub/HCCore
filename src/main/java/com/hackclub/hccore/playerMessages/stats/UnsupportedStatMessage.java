package com.hackclub.hccore.playerMessages.stats;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class UnsupportedStatMessage {

  final static String minimsgSource = """
      <red>This statistic is not currently supported.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
