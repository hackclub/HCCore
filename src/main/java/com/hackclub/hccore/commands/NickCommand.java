package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameLengthMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameResetMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameSetMessage;
import com.hackclub.hccore.playermessages.nickname.SaharshMessage;
import com.hackclub.hccore.slack.SlackBot;
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
      sender.sendMessage(MustBePlayerMessage.get());
      return true;
    }
    SlackBot bot = plugin.getSlackBot();
    if (args.length == 0) {
      PlayerData data = this.plugin.getDataManager().getData(player);
      if (bot != null) {
        bot.preNicknameChange(data.getUsableName(), player.getName());
      }
      data.setNickname(null);
      sender.sendMessage(NicknameResetMessage.get());
      return true;
    }

    String newNickname = String.join(" ", args);
    if (newNickname.equalsIgnoreCase("Saharsh")) {
      PlayerData data = this.plugin.getDataManager().getData(player);
      if (bot != null) {
        bot.preNicknameChange(data.getUsableName(), "Saharchery");
      }
      data.setNickname("Saharchery");
      player.kick(SaharshMessage.get());
      return true;
    }

    if (newNickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      sender.sendMessage(NicknameLengthMessage.get(
          PlayerData.MAX_NICKNAME_LENGTH));
      return true;
    }
    PlayerData data = this.plugin.getDataManager().getData(player);
    if (bot != null) {
      bot.preNicknameChange(data.getUsableName(), newNickname);
    }
    data.setNickname(newNickname);
    sender.sendMessage(NicknameSetMessage.get(newNickname,
        plugin.getDataManager().getData(player).getNameColor()));
    return true;
  }
}
