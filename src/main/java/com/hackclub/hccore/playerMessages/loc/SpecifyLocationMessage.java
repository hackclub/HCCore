package com.hackclub.hccore.playerMessages.loc;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SpecifyLocationMessage {

  final static String minimsgSource = """
      <red>Please specify the location name.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
