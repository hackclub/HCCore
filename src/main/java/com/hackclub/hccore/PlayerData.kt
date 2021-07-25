package com.hackclub.hccoreimport

import com.google.gson.annotations.Expose
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.scoreboard.Team
import java.io.File
import java.util.logging.Level

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

class PlayerData(plugin: HCCorePlugin, player: Player) {
    private val plugin: HCCorePlugin
    private val player: Player
    private val dataFile: File

    // Session data (will be cleared when player quits)
    var isAfk = false
        set(afk) {
            field = afk
            updateDisplayedName()
            val newColor = if (afk) ChatColor.GRAY else nameColor
            val newSuffix = if (afk) " (AFK)" else ""
            team!!.color = newColor
            team!!.suffix = newSuffix
            val event: Event = PlayerAFKStatusChangeEvent(player, afk)
            player.server.pluginManager.callEvent(event)
        }
    var lastDamagedAt: Long = 0
    var lastActiveAt: Long = 0
    // Validate length if a nickname was specified

    // Team#addEntry takes in a string, which in our case, will be a player name. We have to
    // remove the old name and add the new one so the game has a reference to the player.
    // Persistent data
    @Expose
    var nickname: String? = null
        set(nickname) {
            // Validate length if a nickname was specified
            if (nickname != null && nickname.length > PlayerData.Companion.MAX_NICKNAME_LENGTH) {
                return
            }
            val oldName = usableName
            field = nickname
            updateDisplayedName()

            // Team#addEntry takes in a string, which in our case, will be a player name. We have to
            // remove the old name and add the new one so the game has a reference to the player.
            team!!.removeEntry(oldName!!)
            team!!.addEntry(usableName!!)
        }

    @Expose
    var slackId: String? = null

    @Expose
    var nameColor = ChatColor.WHITE
        set(color) {
            field = if (color != null && color.isColor) color else ChatColor.WHITE
            updateDisplayedName()
            team!!.color = nameColor
        }

    @Expose
    var messageColor = ChatColor.GRAY
        set(color) {
            field = if (color != null && color.isColor) color else ChatColor.GRAY
        }

    @Expose
    var savedLocations: Map<String, Location> = LinkedHashMap()
        private set
    val team: Team?
        get() = player.server.scoreboardManager!!.mainScoreboard
                .getTeam(player.name)

    fun load() {
        try {
            dataFile.parentFile.mkdirs()
            if (!dataFile.exists()) {
                plugin.getLogger().log(Level.INFO,
                        "No data file found for " + player.name + ", creating…")
                dataFile.createNewFile()
                save()
            }

            // Populate this instance
            val data = GsonUtil.getInstance().fromJson(FileReader(dataFile),
                    PlayerData::class.java)
            nickname = data.nickname
            slackId = data.slackId
            nameColor = data.nameColor
            messageColor = data.messageColor
            savedLocations = data.savedLocations
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            dataFile.parentFile.mkdirs()
            val writer = FileWriter(dataFile)
            GsonUtil.getInstance().toJson(this, writer)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val usableName: String?
        get() = if (nickname != null) nickname else player.name
    val displayedName: String
        get() {
            var format = "%s"
            format = if (isAfk) {
                ChatColor.GRAY.toString() + format + " (AFK)"
            } else {
                nameColor.toString() + format
            }
            return String.format(format, usableName)
        }

    private fun updateDisplayedName() {
        val builtDisplayName = displayedName
        player.setDisplayName(builtDisplayName)
        player.setPlayerListName(builtDisplayName)
        refreshNameTag()
    }

    private fun refreshNameTag() {
        for (onlinePlayer in player.server.onlinePlayers) {
            if (onlinePlayer == player || !onlinePlayer.canSee(player)) {
                continue
            }
            // Showing the player again after they've been hidden causes a Player Info packet to be
            // sent, which is then intercepted by NameChangeListener. The actual modification of the
            // name tag takes place there.
            onlinePlayer.hidePlayer(plugin, player)
            onlinePlayer.showPlayer(plugin, player)
        }
    }

    companion object {
        const val MAX_NICKNAME_LENGTH = 16
    }

    init {
        this.plugin = plugin
        this.player = player
        dataFile = File(plugin.getDataManager().getDataFolder(), player.uniqueId.toString() + ".json")
    }
}