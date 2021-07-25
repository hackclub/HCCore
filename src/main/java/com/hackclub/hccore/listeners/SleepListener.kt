package com.hackclub.hccore.listenersimport

import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

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

class SleepListener(plugin: HCCorePlugin) : Listener {
    private val plugin: HCCorePlugin
    private var advanceTimeTaskId = 0
    @EventHandler
    fun onPlayerAFKStatusChange(event: PlayerAFKStatusChangeEvent) {
        // Wake the player up if they become AFK while sleeping
        val player = event.player
        if (event.newValue && player.isSleeping) {
            player.wakeup(true)
            player.sendMessage(ChatColor.RED.toString() + "You can’t sleep while you’re AFK.")
        }
    }

    @EventHandler
    fun onPlayerBedEnter(event: PlayerBedEnterEvent) {
        // Ignore unsuccessful attempts to sleep
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) {
            return
        }

        // Only allow active players to sleep
        val data: PlayerData = plugin.getDataManager().getData(event.player)
        if (data.isAfk) {
            event.isCancelled = true
            event.player.sendMessage(ChatColor.RED.toString() + "You can’t sleep while you’re AFK.")
            return
        }
        val currentWorld = event.player.world
        // Add 1 to account for the player that just slept
        val sleepingPlayers = getSleepingPlayers(currentWorld) + 1
        val minSleepingPlayers = getMinSleepingPlayersNeeded(currentWorld)
        broadcastMessageToWorld(ChatColor.GOLD
                .toString() + ChatColor.stripColor(event.player.displayName) + " is now sleeping ("
                + sleepingPlayers + "/" + minSleepingPlayers + " needed)", currentWorld)
        if (sleepingPlayers < minSleepingPlayers) {
            event.player.server.scheduler.cancelTask(advanceTimeTaskId)
            return
        }
        checkCanSkip(currentWorld)
    }

    @EventHandler
    fun onPlayerBedLeave(event: PlayerBedLeaveEvent) {
        val currentWorld = event.player.world
        val sleepingPlayers = getSleepingPlayers(currentWorld)
        val minSleepingPlayers = getMinSleepingPlayersNeeded(currentWorld)

        // Only show wake message if it's still within sleeping periods
        if (canSleep(currentWorld)) {
            broadcastMessageToWorld(ChatColor.GOLD
                    .toString() + ChatColor.stripColor(event.player.displayName) + " has woken up ("
                    + sleepingPlayers + "/" + minSleepingPlayers + " needed)", currentWorld)
        }
        if (sleepingPlayers < minSleepingPlayers) {
            event.player.server.scheduler.cancelTask(advanceTimeTaskId)
            return
        }
        checkCanSkip(currentWorld)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val currentWorld = event.player.world
        if (currentWorld.environment != World.Environment.NORMAL) {
            return
        }
        if (getSleepingPlayers(currentWorld) < getMinSleepingPlayersNeeded(currentWorld)) {
            event.player.server.scheduler.cancelTask(advanceTimeTaskId)
            return
        }
        checkCanSkip(event.player.world)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val currentWorld = event.player.world
        if (currentWorld.environment != World.Environment.NORMAL) {
            return
        }
        if (getSleepingPlayers(
                        currentWorld) < getMinSleepingPlayersNeeded(currentWorld) - 1) {
            event.player.server.scheduler.cancelTask(advanceTimeTaskId)
            return
        }
        checkCanSkip(event.player.world)
    }

    private fun broadcastMessageToWorld(message: String, world: World) {
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.GRAY.toString() + "Broadcasted to "
                + world.name + ": " + ChatColor.RESET + message)
        for (player in world.players) {
            player.sendMessage(message)
        }
    }

    private fun checkCanSkip(world: World) {
        advanceTimeTaskId = plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(plugin, Runnable {
                    if (world.players.size == 0 || getSleepingPlayers(world) == 0) {
                        return@Runnable
                    }

                    // Don't advance if we no longer have the minimum players needed
                    if (getSleepingPlayers(world) < getMinSleepingPlayersNeeded(world)) {
                        return@Runnable
                    }

                    // Advance to morning and clear thunderstorms
                    world.time = SleepListener.Companion.WAKE_AT_TICK.toLong()
                    if (world.isThundering) {
                        world.isThundering = false
                        world.setStorm(false)
                    }
                    broadcastMessageToWorld(ChatColor.GREEN.toString() + "Good morning! Let's get this mf bread.", world)
                }, SleepListener.Companion.SLEEP_DURATION_TICKS.toLong())
    }

    private fun canSleep(world: World): Boolean {
        return (world.isThundering
                || world.hasStorm() && world.time >= SleepListener.Companion.STORM_SLEEP_START_TICK && world.time < SleepListener.Companion.STORM_SLEEP_END_TICK
                || (world.time >= SleepListener.Companion.CLEAR_SLEEP_START_TICK
                && world.time < SleepListener.Companion.CLEAR_SLEEP_END_TICK))
    }

    private fun getSleepingPlayers(world: World): Int {
        var sleepingPlayers = 0
        for (player in world.players) {
            if (player.isSleeping) {
                sleepingPlayers++
            }
        }
        return sleepingPlayers
    }

    private fun getMinSleepingPlayersNeeded(world: World): Int {
        // Get the number of AFK players
        var afkPlayersCount = 0
        for (player in world.players) {
            val playerData: PlayerData = plugin.getDataManager().getData(player)
            if (playerData.isAfk) {
                afkPlayersCount++
            }
        }
        return Math.ceil((world.players.size - afkPlayersCount)
                * plugin.getConfig().getDouble("settings.skip-sleep-threshold")).toInt()
    }

    companion object {
        private const val SLEEP_DURATION_TICKS = 101
        private const val WAKE_AT_TICK = 0
        private const val CLEAR_SLEEP_START_TICK = 12542
        private const val CLEAR_SLEEP_END_TICK = 23460
        private const val STORM_SLEEP_START_TICK = 12010
        private const val STORM_SLEEP_END_TICK = 23992
    }

    init {
        this.plugin = plugin
    }
}