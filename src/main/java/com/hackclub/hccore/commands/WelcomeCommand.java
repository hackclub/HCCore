package com.hackclub.hccore.commands;

import com.hackclub.hccore.annotations.annotations.RegisteredCommand;
import com.hackclub.hccore.commands.general.AbstractCommand;
import com.hackclub.hccore.playermessages.WelcomeMessage;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

@RegisteredCommand
public class WelcomeCommand extends AbstractCommand {

  @Command("welcome")
  public void execute(
      final @NotNull CommandSender sender
  ) {
    sender.sendMessage(WelcomeMessage.get());
  }
}
