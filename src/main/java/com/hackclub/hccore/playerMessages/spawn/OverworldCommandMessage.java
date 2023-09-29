package com.hackclub.hccore.playerMessages.spawn;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class OverworldCommandMessage {

  final static String minimsgSource = """
      <red>You can only use this command in the Overworld.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
