package com.hackclub.hccore.playerMessages.spawn;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class SpawnHurtMessage {

  final static String minimsgSource = """
      <red>You were hurt recently - you'll be able to use this command in <seconds> seconds.</red>""";

  public static Component get(long seconds) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("seconds", text(seconds))));
  }

}
