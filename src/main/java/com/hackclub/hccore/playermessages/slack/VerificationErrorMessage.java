package com.hackclub.hccore.playermessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class VerificationErrorMessage {

  final static String minimsgSource = """
      <red>An error occurred while sending the verification message.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
