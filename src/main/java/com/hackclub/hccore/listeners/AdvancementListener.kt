package com.hackclub.hccore.listenersimport

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent

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

class AdvancementListener(plugin: HCCorePlugin) : Listener {
    private val plugin: HCCorePlugin
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        // Check if it's a diamond ore
        if (event.block.type != Material.DIAMOND_ORE) {
            return
        }

        // The diamond ore wasn't found underground
        val MAX_Y_DIAMOND_ORE = 16
        if (event.block.y > MAX_Y_DIAMOND_ORE) {
            return
        }
        val player = event.player
        val key: NamespacedKey = NamespacedKey(plugin, "mine_diamond_ore")
        val progress = player.getAdvancementProgress(player.server.getAdvancement(key)!!)
        // Skip if player already has this advancement
        if (progress.isDone) {
            return
        }
        grantAdvancement(player, key)
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        // Ignore non-player killers
        if (event.entity.killer !is Player) {
            return
        }
        val player = event.entity.killer
        when (event.entityType) {
            EntityType.ELDER_GUARDIAN -> {
                grantAdvancement(player,
                        NamespacedKey(plugin, "kill_elder_guardian"))
            }
            EntityType.ENDER_DRAGON -> {
                incrementAdvancementProgress(player,
                        NamespacedKey(plugin, "kill_dragon_insane"))

                // Give "The Next Generation" since it's not easy to get it after the egg
                // is taken
                grantAdvancement(player, NamespacedKey.minecraft("end/dragon_egg"))
            }
            EntityType.WITHER -> {
                incrementAdvancementProgress(player,
                        NamespacedKey(plugin, "kill_wither_insane"))
            }
            else -> {
            }
        }
    }

    @EventHandler
    fun onEntityToggleGlide(event: EntityToggleGlideEvent) {
        // Ignore takeoff events
        if (event.isGliding) {
            return
        }
        if (event.entity !is Player) {
            return
        }
        val player = event.entity as Player

        // Check the player has flown over 1m miles (1,609,344 km)import org.bukkit.craftbukkit.v1_17_R1.advancement.CraftAdvancement;
        val CM_PER_MILE = 160934
        if (player.getStatistic(Statistic.AVIATE_ONE_CM) <= 1000000 * CM_PER_MILE) {
            return
        }
        val key: NamespacedKey = NamespacedKey(plugin, "million_miler")
        val progress = player.getAdvancementProgress(player.server.getAdvancement(key)!!)
        // Skip if player already has this advancement
        if (progress.isDone) {
            return
        }
        grantAdvancement(player, key)
    }

    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        if (advancement.key == NamespacedKey(plugin, "mine_diamond_ore")) {
            player.sendMessage(org.bukkit.ChatColor.GREEN
                    .toString() + "Congrats, you’ve found your very first diamond! You are now eligible for the exclusive (and limited edition!) Hack Club Minecraft stickers. Head over to "
                    + org.bukkit.ChatColor.UNDERLINE + plugin.getConfig().getString("claim-stickers-url")
                    + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.GREEN + " to claim them!*")
            player.sendMessage(org.bukkit.ChatColor.GRAY.toString() + org.bukkit.ChatColor.ITALIC.toString()
                    + "*This offer only applies to players who have never received the stickers before. If you have, please do not fill out the form again!")
            player.sendMessage(org.bukkit.ChatColor.GRAY.toString() + org.bukkit.ChatColor.ITALIC.toString()
                    + "You will only see this message once.")
        }
        try {
            // NOTE: We interface with Minecraft's internal code here. It is unlikely, but possible
            // for it to break in the case of a future upgrade.
            val nmsAdvancement = (advancement as CraftAdvancement).handle
            val display = nmsAdvancement.c() ?: return

            // Ignore hidden advancements (i.e. recipes)
            val announceToChat = display.i()
            if (!announceToChat) {
                return
            }

            // Get frame type-specific wording + formatting
            val titleComponent = display.a()
            val descriptionComponent = display.b()
            val frameType = display.e()
            var args: Array<Any>? = null // This is bad practice
            args = when (frameType.a()) {
                "task" -> arrayOf("made", "advancement", org.bukkit.ChatColor.GREEN.asBungee())
                "goal" -> arrayOf("reached", "goal", org.bukkit.ChatColor.GREEN.asBungee())
                "challenge" -> arrayOf("completed", "challenge",
                        org.bukkit.ChatColor.DARK_PURPLE.asBungee())
                else -> arrayOf("made", "advancement", org.bukkit.ChatColor.GREEN.asBungee())
            }

            // Announce custom advancement message
            val nameComponent = TextComponent.fromLegacyText(org.bukkit.ChatColor.stripColor(player.displayName))[0]
            nameComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(player.name))
            val advancementComponent = TextComponent.fromLegacyText(titleComponent.text)[0]
            advancementComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    ComponentBuilder().color(args[2] as ChatColor)
                            .append("""
    ${titleComponent.text}
    
    """.trimIndent())
                            .append(descriptionComponent.text).create())
            val message = ComponentBuilder(nameComponent)
                    .append(String.format(" has %s the %s %s[", *args),
                            ComponentBuilder.FormatRetention.NONE)
                    .append(advancementComponent).color(args[2] as ChatColor)
                    .append("]", ComponentBuilder.FormatRetention.FORMATTING).create()
            player.server.spigot().broadcast(*message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun grantAdvancement(player: Player?, key: NamespacedKey) {
        val progress = player!!.getAdvancementProgress(player.server.getAdvancement(key)!!)
        if (progress.isDone) {
            return
        }
        for (criteria in progress.remainingCriteria) {
            progress.awardCriteria(criteria)
        }
    }

    private fun incrementAdvancementProgress(player: Player?, key: NamespacedKey) {
        val progress = player!!.getAdvancementProgress(player.server.getAdvancement(key)!!)
        if (progress.isDone) {
            return
        }
        val nextCriteria = progress.awardedCriteria.size.toString()
        progress.awardCriteria(nextCriteria)
    }

    init {
        this.plugin = plugin
    }
}