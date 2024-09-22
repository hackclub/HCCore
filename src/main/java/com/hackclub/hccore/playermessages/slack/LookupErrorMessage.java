package com.hackclub.hccore.playermessages.slack;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class LookupErrorMessage {

  final static String minimsgSource = """
      <red>There was an error while looking up that player.</red>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
