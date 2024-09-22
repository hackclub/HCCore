package com.hackclub.hccore.playermessages.loc;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class LocationRenamedMessage {

  final static String minimsgSource = """
      <green>Renamed from <oldname> to <newname>.</green>""";

  public static Component get(String oldname, String newname) {
    return miniMessage().deserialize(minimsgSource, TagResolver.resolver(
        Placeholder.component("oldname", text(oldname)),
        Placeholder.component("newname", text(newname))));
  }

}
