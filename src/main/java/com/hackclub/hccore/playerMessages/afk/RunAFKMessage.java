package com.hackclub.hccore.playerMessages.afk;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class RunAFKMessage {

  final static String minimsgSource = """
      Run /notafk to mark yourself as active""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
