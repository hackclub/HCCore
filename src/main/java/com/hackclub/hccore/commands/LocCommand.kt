package com.hackclub.hccore.commands

import com.hackclub.hccore.HCCorePlugin
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
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*

class LocCommand(private val plugin: HCCorePlugin) : TabExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
            return true
        }
        if (args.size == 0) {
            return false
        }
        val player = sender
        val data = plugin.dataManager.getData(player)
        var locationName = java.lang.String.join("_", *Arrays.copyOfRange(args, 1, args.size))
        when (args[0].toLowerCase()) {
            "del" -> {
                if (args.size < 2) {
                    sender.sendMessage(ChatColor.RED.toString() + "Please specify the location name")
                    break
                }
                if (!data.savedLocations.containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "No location with that name was found")
                    break
                }
                data.savedLocations.remove(locationName)
                sender.sendMessage(
                        ChatColor.GREEN.toString() + "Removed " + locationName + " from saved locations")
            }
            "get" -> {
                if (args.size < 2) {
                    sender.sendMessage(ChatColor.RED.toString() + "Please specify the location name")
                    break
                }
                if (!data.savedLocations.containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "No location with that name was found")
                    break
                }
                val savedLocation = data.savedLocations[locationName]
                sender.sendMessage(locationName + ": " + savedLocation!!.world!!.name + " @ "
                        + savedLocation.blockX + ", " + savedLocation.blockY + ", "
                        + savedLocation.blockZ)
            }
            "list" -> {
                val savedLocations = data.savedLocations
                if (savedLocations.isEmpty()) {
                    sender.sendMessage("You have no saved locations")
                    break
                }
                sender.sendMessage(
                        ChatColor.AQUA.toString() + "Your saved locations (" + savedLocations.size + "):")
                for ((key, savedLocation) in savedLocations) {
                    sender.sendMessage("- " + key + ": "
                            + savedLocation.world!!.name + " @ " + savedLocation.blockX
                            + ", " + savedLocation.blockY + ", " + savedLocation.blockZ)
                }
            }
            "rename" -> {
                if (args.size < 3) {
                    sender.sendMessage("/loc rename <old name> <new name>")
                    break
                }
                val oldName = args[1]
                val newName = java.lang.String.join("_", *Arrays.copyOfRange(args, 2, args.size))
                val targetLoc = data.savedLocations[oldName]
                if (!data.savedLocations.containsKey(oldName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "No location with that name was found")
                    break
                }
                if (data.savedLocations.containsKey(newName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "A location with that name already exists")
                    break
                }
                data.savedLocations[newName] = targetLoc
                data.savedLocations.remove(oldName)
                sender.sendMessage(ChatColor.GREEN.toString() + "Renamed from " + oldName + "to " + newName)
            }
            "save" -> {
                if (args.size < 2) {
                    sender.sendMessage(ChatColor.RED.toString() + "Please specify the location name")
                    break
                }
                if (data.savedLocations.containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "A location with that name already exists")
                    break
                }
                val currentLocation = player.location
                data.savedLocations[locationName] = currentLocation
                sender.sendMessage(ChatColor.GREEN.toString() + "Added " + locationName + " ("
                        + currentLocation.world!!.name + " @ " + currentLocation.blockX
                        + ", " + currentLocation.blockY + ", " + currentLocation.blockZ
                        + ") to saved locations")
            }
            "share" -> {
                if (args.size < 3) {
                    sender.sendMessage(ChatColor.RED
                            .toString() + "Please specify the location name and the player you want to share it with")
                    break
                }
                locationName = args[1]
                val recipientName = args[2]
                if (!data.savedLocations.containsKey(locationName)) {
                    sender.sendMessage(ChatColor.RED.toString() + "No location with that name was found")
                    break
                }
                val sendLocation = data.savedLocations[locationName]
                // Get the player we're sending to
                val recipient = sender.getServer().getPlayer(recipientName)
                if (recipient == null) {
                    sender.sendMessage(ChatColor.RED.toString() + "No online player with that name was found")
                    break
                }
                if (recipientName == player.name) {
                    sender.sendMessage(ChatColor.RED.toString() + "You can’t share a location with yourself!")
                    break
                }
                val recipData = plugin.dataManager.getData(recipient)
                val shareLocName = player.name + " " + locationName
                if (recipData.savedLocations
                                .containsKey(player.name + ":" + shareLocName)) {
                    sender.sendMessage(ChatColor.RED.toString() + recipientName
                            + " already has a location called " + shareLocName)
                    break
                }
                val locationString = ("(" + sendLocation!!.world!!.name + " @ "
                        + sendLocation.blockX + ", " + sendLocation.blockY + ", "
                        + sendLocation.blockZ + ")")
                player.sendMessage(ChatColor.GREEN
                        .toString() + String.format("Shared %s with %s", locationName, recipientName))
                recipient.sendMessage(ChatColor.GREEN.toString() + String.format("%s has shared a location: %s (%s)",
                        player.name, locationName, locationString))
                recipData.savedLocations[player.name + ":" + locationName] = sendLocation
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
        val completions: MutableList<String> = ArrayList()
        when (args.size) {
            1 -> {
                val subcommands = Arrays.asList("del", "get", "list", "rename", "save", "share")
                StringUtil.copyPartialMatches<List<String>>(args[0], subcommands, completions)
            }
            2 -> {
                if (args[0].equals("list", ignoreCase = true) || args[0].equals("save", ignoreCase = true)) {
                    break
                }
                val data = plugin.dataManager.getData(sender)
                for ((key) in data.savedLocations) {
                    if (StringUtil.startsWithIgnoreCase(key, args[1])) {
                        completions.add(key)
                    }
                }
            }
            3 -> {
                if (!args[0].equals("share", ignoreCase = true)) {
                    break
                }
                for (player in sender.getServer().onlinePlayers) {
                    if (StringUtil.startsWithIgnoreCase(player.name, args[2])) {
                        completions.add(player.name)
                    }
                }
            }
        }
        Collections.sort(completions)
        return completions
    }
}