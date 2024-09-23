package com.hackclub.hccore.playermessages.spawn;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SpawnGroundMessage {

  final static String minimsgSource = """
      <red>You need to be standing on the ground to use this command.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
