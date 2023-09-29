package com.hackclub.hccore.playerMessages.player;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MustLinkMessage {

  final static String minimsgSource = """
      <b><red>You must link your Slack account to join the server!</red></b>
      Please run <gold>/<basecommand> link <code></gold> in the #minecraft channel in the Slack (https://slack.hackclub.com) to link your account.
      
      <i>This code expires in <seconds> seconds.</i>""";

  public static Component get(String baseCommand, String code, int seconds) {
    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("basecommand", text(baseCommand)),
            Placeholder.component("code", text(code)),
            Placeholder.component("seconds", text(seconds))));
  }
}
