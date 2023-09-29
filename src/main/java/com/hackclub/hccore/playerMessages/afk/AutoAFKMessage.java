package com.hackclub.hccore.playerMessages.afk;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class AutoAFKMessage {

  final static String minimsgSource = """
      <gray><i>You’ve been automatically set to AFK due to inactivity.</i></gray>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
