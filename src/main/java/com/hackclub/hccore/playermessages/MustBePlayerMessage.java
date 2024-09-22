package com.hackclub.hccore.playermessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class MustBePlayerMessage {

  final static String minimsgSource = """
      <red>You must be a player to use this.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
