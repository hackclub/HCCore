package com.hackclub.hccore.playermessages.spawn;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class SpawnTeleportMessage {

  final static String minimsgSource = """
      <green>You’ve been teleported to the world spawn.</green>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
