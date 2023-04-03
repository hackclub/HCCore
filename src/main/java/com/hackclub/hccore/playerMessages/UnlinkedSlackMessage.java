package com.hackclub.hccore.playerMessages;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;

public class UnlinkedSlackMessage {

  final static String minimsgSource = """
      <b><red>Link your MC account with Hack Club Slack</red></b>
      <gold><b>1.</b></gold> Not on the Slack? Don't worry!
         Join at <click:open_url:'https://hackclub.com/slack'><hover:show_text:'Open in Browser'>hackclub.com/slack</hover></click>
      <gold><b>2.</b></gold> Find your <i>Slack User ID</i>  <b><hover:show_text:'Use #what-is-my-slack-id if you dont know it!
      Click to open channel in Slack'><click:open_url:'https://hackclub.slack.com/archives/C0159TSJVH8'>(?)</click></hover></b>
      <gold><b>3.</b></gold> Run <click:suggest_command:'/slack link '><hover:show_text:'Click to Autofill'><green>/slack link <your slack id></green></hover></click>""";

  public static Component get() {
    return miniMessage().deserialize(minimsgSource);
  }

}
