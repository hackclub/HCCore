package com.hackclub.hccore.playermessages.spawn;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SpawnSkyMessage {

  final static String minimsgSource = """
      <red>You need be directly under the sky to use this command.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
