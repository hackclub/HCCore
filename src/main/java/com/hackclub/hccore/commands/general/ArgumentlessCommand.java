package com.hackclub.hccore.commands.general;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentlessCommand implements TabCompleter {

  final List<String> emptyArgs = List.of();

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
      @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return emptyArgs; // no player name completion
  }
}
