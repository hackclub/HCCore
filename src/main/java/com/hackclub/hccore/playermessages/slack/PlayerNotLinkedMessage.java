package com.hackclub.hccore.playermessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class PlayerNotLinkedMessage {

  final static String minimsgSource = """
      <red>That player has not linked their Slack account.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
