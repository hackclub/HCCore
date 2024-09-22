package com.hackclub.hccore.playermessages.messages;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PrivateMessage {
  public static Component get(String sender, String recipient, String message, TextColor senderColor, TextColor recipientColor) {
    if (senderColor == null) senderColor = TextColor.color(0xAAAAAA);
    if (recipientColor == null) recipientColor = TextColor.color(0xAAAAAA);
    final Component senderNameComponent = text(sender).color(senderColor);
    final Component recipientNameComponent = text(recipient).color(recipientColor);
    final Component messageComponent = text(message);

    return empty().color(TextColor.color(0xAAAAAA))
        .append(senderNameComponent)
        .append(text(" -> ").color(TextColor.color(0xAAAAAA)))
        .append(recipientNameComponent)
        .append(text(": ").color(TextColor.color(0xAAAAAA)))
        .append(messageComponent);
  }
}