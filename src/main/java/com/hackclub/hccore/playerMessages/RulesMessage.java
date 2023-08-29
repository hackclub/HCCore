package com.hackclub.hccore.playerMessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class RulesMessage {

  final static String minimsgSource = """
      <b><red>HackCraft Vanilla Server Rules</red></b>
      <b>1.</b> No destruction of other players' property, stealing other player's items, trapping players in a structure, or harmful wars/conflict.
            
      <b>2.</b> No cheats - We allow certain (client) mods to be used on the server. Quality of life mods are allowed, such as minimaps and other useful client utilities. However, mods that are designed to give you an unfair advantage over other players, such as baritone or any hack client, are not allowed. <i>This includes x-ray hacks.</i> If you are uncertain about if a mod is permitted or not, ask a server admin.
            
      <b>3.</b> If you see that another player is not following the server rules, <i>simply report it to a server admin</i>. <red>Do not retaliate.</red>
            
      <b>4.</b> Any violation of the above rules will result in the following consequences:
      <red>First offense</red>: temp ban (duration determined by admins)
      <red>Second offense</red>: indefinite ban
            
      <b>5.</b> If not otherwise mentioned in these rules, follow the <u><click:open_url:'https://hackclub.com/conduct/'><hover:show_text:'Open in Browser'>Hack Club Code of Conduct</hover></click></u>.""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
