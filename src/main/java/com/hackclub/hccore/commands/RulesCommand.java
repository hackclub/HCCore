package com.hackclub.hccore.commands;

import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.RulesMessage;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

public class RulesCommand extends AbstractCommand {

  @Command("rules")
  public void execute(
      final @NotNull CommandSender sender
  ) {
    sender.sendMessage(RulesMessage.get());
  }
}
