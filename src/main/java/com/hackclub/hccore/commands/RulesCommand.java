package com.hackclub.hccore.commands;

import com.hackclub.hccore.commands.general.ArgumentlessCommand;
import com.hackclub.hccore.playerMessages.RulesMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RulesCommand extends ArgumentlessCommand implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    sender.sendMessage(RulesMessage.get());
    return true;
  }
}
