package com.hackclub.hccore.playermessages.nickname;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class NicknameAlreadyUsedMessage {
    final static String minimsgSource = """
        <red>The nickname <nickname> is already in use.</red>""";

  public static Component get(String nickname, TextColor color) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("nickname", text(nickname).color(color))));
  }
}
