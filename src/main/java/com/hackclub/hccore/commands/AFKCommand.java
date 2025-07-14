package com.hackclub.hccore.commands;

import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

@RegisteredCommand
public class AFKCommand extends AbstractCommand {

  @Command("afk|a|away")
  public void execute(
      final @NotNull Player sender
  ) {
    PlayerData data = this.plugin.getDataManager().getData(sender);
    data.setAfk(!data.isAfk());
  }
}
