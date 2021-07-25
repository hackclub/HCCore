package com.hackclub.hccore.commandsimport

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*

com.hackclub.hccore.HCCorePlugin
import com.hackclub.hccore.PlayerData
import com.hackclub.hccore.tasks.CheckAdAstraTask
import com.hackclub.hccore.utils.gson.GsonUtil
import com.hackclub.hccore.utils.gson.LocationSerializer
import com.hackclub.hccore.utils.gson.LocationMapDeserializer
import com.hackclub.hccore.utils.gson.LocationMapSerializer
import com.hackclub.hccore.utils.gson.LocationDeserializer
import com.hackclub.hccore.events.player.PlayerAFKStatusChangeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.JDABuilder
import javax.security.auth.login.LoginException
import java.lang.Exception
import com.hackclub.hccore.commands.ColorCommand
import java.util.stream.Collectors
import com.hackclub.hccore.commands.StatsCommand
import java.text.SimpleDateFormat
import com.hackclub.hccore.commands.UpvoteCommand
import com.hackclub.hccore.commands.DownvoteCommand
import com.hackclub.hccore.commands.TableflipCommand
import java.lang.Runnable
import com.hackclub.hccore.listeners.SleepListener
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.craftbukkit.v1_17_R1.advancement.CraftAdvancement
import net.minecraft.advancements.AdvancementDisplay
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.advancements.AdvancementFrameType
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import java.io.FileReader
import java.io.IOException
import java.io.FileWriter
import com.hackclub.hccore.DataManager
import com.comphenix.protocol.ProtocolManager
import com.hackclub.hccore.discord.DiscordBot
import com.comphenix.protocol.ProtocolLibrary
import com.hackclub.hccore.commands.AFKCommand
import com.hackclub.hccore.commands.LocCommand
import com.hackclub.hccore.commands.NickCommand
import com.hackclub.hccore.commands.PingCommand
import com.hackclub.hccore.commands.ShrugCommand
import com.hackclub.hccore.commands.SpawnCommand
import com.hackclub.hccore.commands.DiscordCommand
import com.hackclub.hccore.listeners.AdvancementListener
import com.hackclub.hccore.listeners.AFKListener
import com.hackclub.hccore.listeners.BeehiveInteractionListener
import com.hackclub.hccore.listeners.PlayerListener
import com.hackclub.hccore.listeners.NameChangeListener
import com.hackclub.hccore.tasks.AutoAFKTask
import hu.trigary.advancementcreator.AdvancementFactory
import hu.trigary.advancementcreator.shared.ItemObject

class ColorCommand(plugin: HCCorePlugin) : TabExecutor {
    private val plugin: HCCorePlugin
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
            return true
        }
        if (args.size == 0) {
            return false
        }
        val data: PlayerData = plugin.getDataManager().getData(sender)

        // Validate selected color
        var newColor: ChatColor? = null
        if (args.size > 1) {
            // Not in ChatColor at all
            if (!ColorCommand.Companion.COLOR_NAMES.contains(args[1].toLowerCase())) {
                sender.sendMessage(ChatColor.RED.toString() + "Invalid color specified")
                return true
            }

            // Is in ChatColor, but not a color
            newColor = ChatColor.valueOf(args[1].toUpperCase())
            if (!newColor.isColor) {
                sender.sendMessage(ChatColor.RED.toString() + "Invalid color specified")
                return true
            }
        }
        when (args[0].toLowerCase()) {
            "chat" -> {
                if (args.size == 1) {
                    data.messageColor = null
                    sender.sendMessage("Your chat color has been reset")
                    break
                }
                data.messageColor = newColor
                sender.sendMessage("Your chat color has been set to " + newColor + "this color")
            }
            "name" -> {
                if (args.size == 1) {
                    data.nameColor = null
                    sender.sendMessage("Your name color has been reset")
                    break
                }
                data.nameColor = newColor
                sender.sendMessage("Your name color has been set to " + newColor + "this color")
            }
            else -> return false
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String,
                               args: Array<String>): List<String>? {
        if (sender !is Player) {
            return null
        }
        val completions: List<String> = ArrayList()
        when (args.size) {
            1 -> {
                val subcommands = Arrays.asList("chat", "name")
                StringUtil.copyPartialMatches(args[0], subcommands, completions)
            }
            2 -> {
                if (!(args[0].equals("chat", ignoreCase = true) || args[0].equals("name", ignoreCase = true))) {
                    break
                }
                StringUtil.copyPartialMatches(args[1], ColorCommand.Companion.COLOR_NAMES, completions)
            }
        }
        Collections.sort(completions)
        return completions
    }

    companion object {
        private val COLOR_NAMES = Arrays.asList(*ChatColor.values()).stream().filter { value: ChatColor -> value.isColor }
                .map { color: ChatColor -> color.name.toLowerCase() }.collect(Collectors.toList())
    }

    init {
        this.plugin = plugin
    }
}