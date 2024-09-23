package com.hackclub.hccore.playermessages.afk;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class YouAFKMessage {

  final static String minimsgSource = """
      <b><red>You are AFK</red></b>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
