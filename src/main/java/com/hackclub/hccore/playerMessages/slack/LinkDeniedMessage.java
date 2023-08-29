package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class LinkDeniedMessage {

  final static String minimsgSource = """
      <red>The request to link the account was denied.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
