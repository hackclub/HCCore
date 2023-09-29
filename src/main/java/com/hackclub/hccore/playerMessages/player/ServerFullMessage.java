package com.hackclub.hccore.playerMessages.player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class ServerFullMessage {

  final static String minimsgSource = """
      <b><red>The server is full!</red></b>
      Sorry, it looks like there’s no more room. Please try again in ~20 minutes.""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
