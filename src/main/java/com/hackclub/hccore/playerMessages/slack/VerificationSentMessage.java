package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class VerificationSentMessage {

  final static String minimsgSource = """
      <green>A verification message has been sent to your Slack account.</green>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
