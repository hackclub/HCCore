package com.hackclub.hccore.playermessages.color;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class InvalidColorMessage {

  final static String minimsgSource = """
      <red>Invalid color specified.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
