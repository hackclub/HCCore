package com.hackclub.hccore.commands;

import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.WelcomeMessage;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

public class WelcomeCommand extends AbstractCommand {

  @Command("welcome")
  public void execute(
      final @NotNull CommandSender sender
  ) {
    sender.sendMessage(WelcomeMessage.get());
  }
}
