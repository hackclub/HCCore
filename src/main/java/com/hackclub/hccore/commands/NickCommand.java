package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.nickname.NicknameLengthMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameResetMessage;
import com.hackclub.hccore.playermessages.nickname.NicknameSetMessage;
import com.hackclub.hccore.playermessages.nickname.SaharshMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NickCommand extends AbstractCommand {

  @Command("nick|nickname [nickname]")
  public void execute(
      final @NotNull Player sender,
      final @Nullable @Argument("nickname") @Greedy String nickname
  ) {
    if (nickname == null) {
      this.plugin.getDataManager().getData(sender).setNickname(null);
      sender.sendMessage(NicknameResetMessage.get());
      return;
    }
    if (nickname.equalsIgnoreCase("Saharsh")) {
      this.plugin.getDataManager().getData(sender).setNickname("Saharchery");
      sender.kick(SaharshMessage.get());
      return;
    }

    if (nickname.length() > PlayerData.MAX_NICKNAME_LENGTH) {
      sender.sendMessage(NicknameLengthMessage.get(
          PlayerData.MAX_NICKNAME_LENGTH));
      return;
    }

    this.plugin.getDataManager().getData(sender).setNickname(nickname);
    sender.sendMessage(NicknameSetMessage.get(nickname,
        plugin.getDataManager().getData(sender).getNameColor()));
  }
}
