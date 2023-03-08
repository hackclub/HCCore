package com.hackclub.hccore.commands;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NickCommand implements CommandExecutor {

  private final HCCorePlugin plugin;

  public NickCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(text("You must be a player to use this").color(RED));
      return true;
    }

    if (args.length == 0) {
      this.plugin.getDataManager().getData(player).setNickname(null);
      sender.sendMessage(text("Your nickname has been reset!").color(GREEN));
      return true;
    }

    String newNickname = String.join(" ", args);
    if (newNickname.equalsIgnoreCase("Saharsh")) {
      this.plugin.getDataManager().getData(player).setNickname("Saharchery");
      player.kick(text("Kicked for being Saharsh."));
      return true;
    }

    if (newNickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      sender.sendMessage(text("Your nickname can’t be longer than %d characters".formatted(
          PlayerData.MAX_NICKNAME_LENGTH)).color(RED));
      return true;
    }

    this.plugin.getDataManager().getData(player).setNickname(newNickname);
    sender.sendMessage(text("Your nickname was set to").color(GREEN).appendSpace()
        .append(text(newNickname).color(plugin.getDataManager().getData(player).getNameColor())));
    return true;
  }
}
