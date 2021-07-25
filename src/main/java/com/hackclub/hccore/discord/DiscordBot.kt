package com.hackclub.hccore.discord

import com.hackclub.hccore.HCCorePlugin
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
import net.dv8tion.jda.api.entities.Activity
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.awt.Color
import java.lang.StringBuilder
import java.util.logging.Level

class DiscordBot(private val plugin: HCCorePlugin) : ListenerAdapter(), Listener {
    private var jda: JDA? = null
    private var channel: TextChannel? = null
    private val prefix: String
    override fun onReady(e: ReadyEvent) {
        channel = jda!!.getTextChannelById(plugin.config.getLong("settings.discord-link.channel-id"))
        val embed = EmbedBuilder()
        embed.setColor(Color.GREEN)
        embed.setAuthor("Server Started!")
        channel!!.sendMessage(embed.build()).queue()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.member == null || event.author == jda!!.selfUser) return
        if (event.channel === channel) {
            // list command
            if (event.message.contentRaw == prefix + "list") {
                val embed = EmbedBuilder()
                embed.setTitle(Bukkit.getOnlinePlayers().size.toString() + " / " + Bukkit.getMaxPlayers() + " Players Online")
                val listBuilder = StringBuilder()
                Bukkit.getOnlinePlayers().forEach { player: Player? ->
                    val data = plugin.dataManager.getData(player)
                    listBuilder.append("""
 • ${ChatColor.stripColor(data.displayedName)}""")
                }
                embed.setDescription(listBuilder.toString())
                embed.setColor(Color.CYAN)
                channel!!.sendMessage(embed.build()).queue()
                return
            }
            val prefixComponent = TextComponent("[Discord] ")
            prefixComponent.color = ChatColor.BLUE.asBungee()
            val nameComponent = TextComponent(event.member!!.effectiveName)
            nameComponent.color = if (event.member!!.color != null) net.md_5.bungee.api.ChatColor.of(event.member!!.color) else ChatColor.WHITE.asBungee()
            nameComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(event.author.asTag))
            val arrowComponent = TextComponent(" » ")
            arrowComponent.color = ChatColor.GOLD.asBungee()
            val playerChatComponent = TextComponent(ChatColor.translateAlternateColorCodes('&', event.message.contentDisplay))
            playerChatComponent.color = ChatColor.WHITE.asBungee()
            plugin.server.spigot().broadcast(prefixComponent, nameComponent, arrowComponent, playerChatComponent)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = plugin.dataManager.getData(e.player)
        channel!!.sendMessage(ChatColor.stripColor(player.displayedName) + " » " + ChatColor.stripColor(e.message)).queue()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val embed = EmbedBuilder()
        embed.setColor(Color.GREEN)
        embed.setAuthor(ChatColor.stripColor(player.displayName) + " joined the game!", null, getAvatarUrl(player))
        channel!!.sendMessage(embed.build()).queue()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        val embed = EmbedBuilder()
        embed.setColor(Color.RED)
        embed.setAuthor(ChatColor.stripColor(player.displayName) + " left the game!", null, getAvatarUrl(player))
        channel!!.sendMessage(embed.build()).queue()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeath(e: PlayerDeathEvent) {
        val embed = EmbedBuilder()
        embed.setColor(Color.BLACK)
        embed.setAuthor(e.deathMessage, null, getAvatarUrl(e.entity))
        channel!!.sendMessage(embed.build()).queue()
    }

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onAchievement(PlayerAdvancementDoneEvent e) {
        if(e.getAdvancement() == null || e.getAdvancement().getKey().getKey().contains("recipe/") || e.getPlayer() == null) return;

        CraftAdvancement craftAdvancement = (CraftAdvancement) e.getAdvancement();
        String title = craftAdvancement.getHandle().c().a().getText();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW);
        embed.setAuthor(ChatColor.stripColor(e.getPlayer().getDisplayName()) + " has made the advancement " + title, null, getAvatarUrl(e.getPlayer()));
        channel.sendMessage(embed.build()).queue();
    }*/
    private fun getAvatarUrl(player: Player): String {
        return "https://cravatar.eu/avatar/" + player.uniqueId + ".png"
    }

    fun close() {
        val embed = EmbedBuilder()
        embed.setColor(Color.RED)
        embed.setAuthor("Server Stopped!")
        channel!!.sendMessage(embed.build()).queue()
        jda!!.shutdown()
    }

    init {
        val builder = JDABuilder.createDefault(plugin.config.getString("settings.discord-link.bot-token"))
        builder.setActivity(Activity.playing("mc.hackclub.com"))
        try {
            jda = builder.build()
        } catch (e: LoginException) {
            plugin.logger.log(Level.SEVERE, "Error logging into Discord!")
            e.printStackTrace()
        }
        jda!!.addEventListener(this)
        plugin.server.pluginManager.registerEvents(this, plugin)
        prefix = plugin.config.getString("settings.discord-link.bot-prefix")!!
    }
}