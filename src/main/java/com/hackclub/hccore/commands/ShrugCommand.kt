package com.hackclub.hccore.commands

import com.hackclub.hccore.HCCorePlugin
import com.hackclub.hccore.commands.ShrugCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShrugCommand(private val plugin: HCCorePlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
            return true
        }
        val player = sender
        if (args.size == 0) {
            player.chat(SHRUG)
        } else {
            player.chat(java.lang.String.join(" ", *args) + " " + SHRUG)
        }
        return true
    }

    companion object {
        private const val SHRUG = "¯\\_(ツ)_/¯"
    }
}