package com.hackclub.hccore.playermessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class InvalidSlackMessage {

  final static String minimsgSource = """
      <red>That Slack ID is invalid!</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}