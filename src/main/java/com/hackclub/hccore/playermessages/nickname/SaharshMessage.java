package com.hackclub.hccore.playermessages.nickname;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SaharshMessage {

  final static String minimsgSource = """
      <b><red>Kicked for being Saharsh.</red></b>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
