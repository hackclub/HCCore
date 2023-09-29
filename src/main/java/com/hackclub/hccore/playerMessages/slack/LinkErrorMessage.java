package com.hackclub.hccore.playerMessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class LinkErrorMessage {

  final static String minimsgSource = """
      <red>An error occurred while linking your account.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
