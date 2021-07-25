package com.hackclub.hccoreimport

import org.bukkit.entity.Player
import java.io.File

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

class DataManager(plugin: HCCorePlugin) {
    private val plugin: HCCorePlugin
    val dataFolder: String
    private val players: MutableMap<UUID, PlayerData> = HashMap()
    fun getData(player: Player): PlayerData? {
        return players[player.uniqueId]
    }

    fun registerPlayer(player: Player) {
        players[player.uniqueId] = PlayerData(plugin, player)

        // Register player's team
        val mainScoreboard = player.server.scoreboardManager!!.mainScoreboard
        player.scoreboard = mainScoreboard
        // Unregister existing teams in the player's name
        var playerTeam = mainScoreboard.getTeam(player.name)
        playerTeam?.unregister()
        playerTeam = mainScoreboard.registerNewTeam(player.name)
        playerTeam.addEntry(player.name)

        // Load in player data for use
        getData(player)!!.load()
    }

    fun registerAll() {
        for (player in plugin.getServer().getOnlinePlayers()) {
            registerPlayer(player)
        }
    }

    fun unregisterPlayer(player: Player) {
        getData(player)!!.save()
        getData(player)!!.team.unregister()
        players.remove(player.uniqueId)
    }

    fun unregisterAll() {
        for (player in plugin.getServer().getOnlinePlayers()) {
            unregisterPlayer(player)
        }
    }

    init {
        this.plugin = plugin
        dataFolder = plugin.getDataFolder().toString() + File.separator + "players"
        val folder = File(dataFolder)
        folder.mkdirs()
    }
}