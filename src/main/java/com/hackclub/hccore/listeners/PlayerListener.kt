package com.hackclub.hccore.listenersimport

import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

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

class PlayerListener(plugin: HCCorePlugin) : Listener {
    private val plugin: HCCorePlugin
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) {
            return
        }
        val player = event.entity as Player
        plugin.getDataManager().getData(player).setLastDamagedAt(System.currentTimeMillis())
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        var message = event.deathMessage
        message = message!!.replace(event.entity.name,
                ChatColor.stripColor(event.entity.displayName)!!)
        val killer = event.entity.killer
        if (killer != null) {
            message = message.replace(killer.name,
                    ChatColor.stripColor(killer.displayName)!!)
        }
        event.deathMessage = message
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {

        // Apply the player's chat color to the message and translate color codes
        val data: PlayerData = plugin.getDataManager().getData(event.player)
        val messageColor = data.messageColor.asBungee()
        val nameColor = data.nameColor.asBungee()
        val nameComponent = TextComponent(event.player.displayName)
        nameComponent.color = nameColor
        nameComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder(event.player.name).create())
        val arrowComponent = TextComponent(" » ")
        arrowComponent.color = ChatColor.GOLD.asBungee()
        val playerChatComponent = TextComponent(ChatColor.translateAlternateColorCodes('&', event.message))
        playerChatComponent.color = messageColor
        plugin.getServer().spigot().broadcast(nameComponent, arrowComponent,
                playerChatComponent)
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST) // Runs foremost
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.getDataManager().registerPlayer(event.player)
        // Set the initial active time
        plugin.getDataManager().getData(event.player)
                .setLastActiveAt(System.currentTimeMillis())

        // NOTE: Title isn't cleared when the player leaves the server
        event.player.resetTitle()
        event.joinMessage = ChatColor.YELLOW
                .toString() + ChatColor.stripColor(event.player.displayName) + " joined the game"
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        if (event.result == PlayerLoginEvent.Result.ALLOWED
                || event.result == PlayerLoginEvent.Result.KICK_OTHER) {
            return
        }
        var message: String? = null
        when (event.result) {
            PlayerLoginEvent.Result.KICK_BANNED -> message = """
     ${ChatColor.RED}${ChatColor.BOLD}You’ve been banned :(
     
     ${ChatColor.RESET}${ChatColor.WHITE}If you believe this was a mistake, please DM ${ChatColor.AQUA}@alx or @eli ${ChatColor.WHITE}on Slack.
     """.trimIndent()
            PlayerLoginEvent.Result.KICK_FULL -> message = """
     ${ChatColor.RED}${ChatColor.BOLD}The server is full!
     
     ${ChatColor.RESET}${ChatColor.WHITE}Sorry, it looks like there’s no more room. Please try again in ~20 minutes.
     """.trimIndent()
            PlayerLoginEvent.Result.KICK_WHITELIST -> message = """
     ${ChatColor.RED}${ChatColor.BOLD}You’re not whitelisted!
     
     ${ChatColor.RESET}${ChatColor.WHITE}Join ${ChatColor.AQUA}#minecraft ${ChatColor.WHITE}on Slack and ping ${ChatColor.AQUA}@alx or @eli ${ChatColor.WHITE}to be added.
     """.trimIndent()
            else -> {
            }
        }
        event.kickMessage = message!!
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        // Reset lastDamagedAt
        plugin.getDataManager().getData(event.player).setLastDamagedAt(0)
    }

    @EventHandler(priority = EventPriority.MONITOR) // Runs very last
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // NOTE: Title isn't cleared when the player leaves the server
        // event.getPlayer().resetTitle();
        event.quitMessage = ChatColor.YELLOW
                .toString() + ChatColor.stripColor(event.player.displayName) + " left the game"
        plugin.getDataManager().unregisterPlayer(event.player)
    }

    init {
        this.plugin = plugin
    }
}