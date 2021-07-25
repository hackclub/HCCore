package com.hackclub.hccore.commandsimport

import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

com.hackclub.hccore.HCCorePlugin
import com.hackclub.hccore.PlayerData
import com.hackclub.hccore.tasks.CheckAdAstraTask
import com.hackclub.hccore.utils.gson.GsonUtil
import com.hackclub.hccore.utils.gson.LocationSerializer
import com.hackclub.hccore.utils.gson.LocationMapDeserializer
import com.hackclub.hccore.utils.gson.LocationMapSerializer
import com.hackclub.hccore.utils.gson.LocationDeserializer
import java.util.LinkedHashMap
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
import java.util.UUID
import java.util.HashMap
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

class SpawnCommand(plugin: HCCorePlugin) : CommandExecutor {
    private val plugin: HCCorePlugin
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
            return true
        }
        val player = sender

        // Player needs to be in the Overworld
        if (player.world.environment != World.Environment.NORMAL) {
            sender.sendMessage(ChatColor.RED.toString() + "You can only use this command in the Overworld")
            return true
        }

        // Player needs to be within the allowed radius from spawn
        val distanceFromSpawn = player.location.distance(player.world.spawnLocation).toInt()
        val allowedRadius: Int = plugin.getConfig().getInt("settings.spawn-command.allowed-radius")
        if (distanceFromSpawn > allowedRadius) {
            sender.sendMessage(ChatColor.RED.toString() + "You need to be within " + allowedRadius
                    + " blocks from spawn to use this command. Currently, you’re "
                    + (distanceFromSpawn - allowedRadius) + " blocks too far.")
            return true
        }

        // Player needs to be on the ground
        if (!player.isOnGround) {
            sender.sendMessage(ChatColor.RED.toString() + "You need to be standing on the ground to use this command")
            return true
        }

        // Player needs to have sky access (no blocks above them at all)
        val highestBlock = player.world.getHighestBlockAt(player.location)
        if (player.location.y < highestBlock.y) {
            sender.sendMessage(ChatColor.RED.toString() + "You need be directly under the sky to use this command")
            return true
        }

        // Can't be used while in damage cooldown period
        val data: PlayerData = plugin.getDataManager().getData(player)
        val currentTime = System.currentTimeMillis()
        val secondsSinceLastDamaged = (currentTime - data.lastDamagedAt) / 1000
        val damageCooldown: Int = plugin.getConfig().getInt("settings.spawn-command.damage-cooldown")
        if (secondsSinceLastDamaged < damageCooldown) {
            sender.sendMessage(
                    ChatColor.RED.toString() + "You were hurt recently—you’ll be able to use this command in "
                            + (damageCooldown - secondsSinceLastDamaged) + " seconds")
            return true
        }
        player.teleport(player.world.spawnLocation)
        sender.sendMessage(ChatColor.GREEN.toString() + "You’ve been teleported to the world spawn")
        return true
    }

    init {
        this.plugin = plugin
    }
}