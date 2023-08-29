package com.hackclub.hccore.playerMessages.stats;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class IncludePlayerStatMessage {

  final static String minimsgSource = """
      <red>You must include both a player and statistic name.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
