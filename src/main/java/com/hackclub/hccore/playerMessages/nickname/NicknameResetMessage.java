package com.hackclub.hccore.playerMessages.nickname;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class NicknameResetMessage {

  final static String minimsgSource = """
      <green>Your nickname has been reset!</green>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
