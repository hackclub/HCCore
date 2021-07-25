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

class PingCommand(plugin: HCCorePlugin) : TabExecutor {
    private val plugin: HCCorePlugin
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        // /ping
        if (args.size == 0) {
            if (sender !is Player) {
                sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
                return true
            }
            val ping = getPing(sender)
            // Failed for some reason
            if (ping == -1) {
                sender.sendMessage(ChatColor.RED.toString() + "Failed to get your ping")
                return true
            }
            sender.sendMessage("Your ping is " + ping + "ms")
            return true
        }

        // /ping [player]
        val targetPlayer = sender.server.getPlayerExact(args[0])
        if (targetPlayer != null) {
            val data: PlayerData = plugin.getDataManager().getData(targetPlayer)
            val ping = getPing(targetPlayer)
            // Failed for some reason
            if (ping == -1) {
                sender.sendMessage(
                        ChatColor.RED.toString() + "Failed to get " + data.usableName + "’s ping")
                return true
            }
            sender.sendMessage(data.usableName + "’s ping is " + ping + "ms")
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "No online player with that name was found")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String,
                               args: Array<String>): List<String>? {
        val completions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            for (player in sender.server.onlinePlayers) {
                if (StringUtil.startsWithIgnoreCase(player.name, args[0])) {
                    completions.add(player.name)
                }
            }
        }
        Collections.sort(completions)
        return completions
    }

    private fun getPing(player: Player): Int {
        var ping = -1
        try {
            // Use reflection because there's no Player#getPing method
            val entityPlayer = player.javaClass.getMethod("getHandle").invoke(player)
            ping = entityPlayer.javaClass.getField("ping")[entityPlayer] as Int
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ping
    }

    init {
        this.plugin = plugin
    }
}