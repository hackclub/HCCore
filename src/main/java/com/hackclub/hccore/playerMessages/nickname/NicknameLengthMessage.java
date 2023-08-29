package com.hackclub.hccore.playerMessages.nickname;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class NicknameLengthMessage {

  final static String minimsgSource = """
      <red>Your nickname can't be longer than <length> characters.</red>""";

  public static Component get(int length) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("length", text(length))));
  }

}
