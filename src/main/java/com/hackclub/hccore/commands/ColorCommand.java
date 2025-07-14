package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.color.ColorResetMessage;
import com.hackclub.hccore.playermessages.color.ColorSetMessage;
import com.hackclub.hccore.playermessages.color.InvalidColorMessage;
import java.util.Set;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisteredCommand
public class ColorCommand extends AbstractCommand {

  @Command("color|colour <type> [color]")
  public void execute(
      final @NotNull Player sender,
      final @NotNull @Argument("type") ColorType type,
      final @Nullable @Argument(value = "color", suggestions = "colors") String color
  ) {
    PlayerData data = this.plugin.getDataManager().getData(sender);

    // Validate selected color
    TextColor newColor = null;
    if (color != null) {
      newColor = NamedTextColor.NAMES.keyToValue().get(color);
      if (newColor == null) {
        newColor = TextColor.fromCSSHexString(color);
      }
      if (newColor == null) {
        newColor = TextColor.fromHexString(color);
      }

      if (newColor == null) {
        sender.sendMessage(InvalidColorMessage.get());
      }
    }

    switch (type) {
      // /color chat [color]
      case CHAT -> {
        if (color == null) {
          data.setMessageColor(null);
          sender.sendMessage(ColorResetMessage.get("chat"));
          break;
        }
        data.setMessageColor(newColor);
        sender.sendMessage(ColorSetMessage.get("chat", newColor));
      }
      // /color name [color]
      case NAME -> {
        if (color == null) {
          data.setNameColor(null);
          sender.sendMessage(ColorResetMessage.get("chat"));
          break;
        }
        data.setNameColor(newColor);
        sender.sendMessage(ColorSetMessage.get("name", newColor));
      }
      default -> {
        return;
      }
    }
  }

  public enum ColorType {
    CHAT,NAME
  }

  @Suggestions("colors")
  public Set<String> colors() {
    return NamedTextColor.NAMES.keys();
  }
}
