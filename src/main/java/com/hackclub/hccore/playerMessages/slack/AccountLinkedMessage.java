package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class AccountLinkedMessage {

  final static String minimsgSource = """
      <green>Your accounts have been linked!</green>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
