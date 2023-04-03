package com.hackclub.hccore.commands;

import com.hackclub.hccore.commands.general.ArgumentlessCommand;
import com.hackclub.hccore.playerMessages.WelcomeMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class WelcomeCommand extends ArgumentlessCommand implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    WelcomeMessage.send(sender);
    return true;
  }
}
