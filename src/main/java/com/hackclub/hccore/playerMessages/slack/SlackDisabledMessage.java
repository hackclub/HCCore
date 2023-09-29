package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SlackDisabledMessage {

  final static String minimsgSource = """
      <red>Slack integration is not enabled.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
