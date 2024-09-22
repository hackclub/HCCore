package com.hackclub.hccore.commands;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playermessages.MustBePlayerMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameLengthMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameResetMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameSetMessage;
import com.hackclub.hccore.playermessages.nickname.SaharshMessage;
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

    if (args.length == 0) {
      this.plugin.getDataManager().getData(player).setNickname(null);
      sender.sendMessage(NicknameResetMessage.get());
      return true;
    }

    String newNickname = String.join(" ", args);
    if (newNickname.equalsIgnoreCase("Saharsh")) {
      this.plugin.getDataManager().getData(player).setNickname("Saharchery");
      player.kick(SaharshMessage.get());
      return true;
    }

    if (newNickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      sender.sendMessage(NicknameLengthMessage.get(
          PlayerData.MAX_NICKNAME_LENGTH));
      return true;
    }

    this.plugin.getDataManager().getData(player).setNickname(newNickname);
    sender.sendMessage(NicknameSetMessage.get(newNickname,
        plugin.getDataManager().getData(player).getNameColor()));
    return true;
  }
}
