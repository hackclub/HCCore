package com.hackclub.hccore.playermessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class WelcomeMessage {

  final static String minimsgSource = """
      <gold><i>Welcome to <b>HackCraft Vanilla</b></i></gold>
        To keep everything <b>fair</b> and <b>fun</b>,
        please read the <b><red><click:run_command:'/rules'><hover:show_text:'Click to Run <red>/rules</red>'>/rules</hover></click></red></b>.
      <gray>To view this again at any time, use <click:run_command:'/welcome'><hover:show_text:'Click to Run <gray>/welcome</gray>'>/welcome</hover></click></gray>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }
}
