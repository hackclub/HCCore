package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
      sender.sendMessage(
          Component.text("You must be a player to use this").color(NamedTextColor.RED));
      return true;
    }

    if (args.length == 0) {
      this.plugin.getDataManager().getData(player).setNickname(null);
      sender.sendMessage(
          Component.text("Your nickname has been reset!").color(NamedTextColor.GREEN));
      return true;
    }

    String newNickname = String.join(" ", args);
    if (newNickname.equalsIgnoreCase("Saharsh")) {
      this.plugin.getDataManager().getData(player).setNickname("Saharchery");
      player.kick(Component.text("Kicked for being Saharsh."));
      return true;
    }

    if (newNickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      sender.sendMessage(Component.text("Your nickname can’t be longer than "
          + PlayerData.MAX_NICKNAME_LENGTH + " characters").color(NamedTextColor.RED));
      return true;
    }

    this.plugin.getDataManager().getData(player).setNickname(newNickname);
    sender.sendMessage(Component.text("Your nickname was set to ").append(
        Component.text(newNickname)
            .color(this.plugin.getDataManager().getData(player).getNameColor())));

    return true;
  }
}
