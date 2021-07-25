package com.hackclub.hccore.utils

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
import java.util.concurrent.TimeUnit

object TimeUtil {
    const val TICKS_PER_SECOND = 20
    const val SECONDS_PER_MINUTE = 60
    fun toPrettyTime(ticks: Int): String {
        return toPrettyTime(ticks, false)
    }

    fun toPrettyTime(ticks: Int, full: Boolean): String {
        val totalSeconds = ticks / TICKS_PER_SECOND
        val days = TimeUnit.SECONDS.toDays(totalSeconds.toLong()).toInt()
        val hours = (TimeUnit.SECONDS.toHours(totalSeconds.toLong()) - TimeUnit.DAYS.toHours(days.toLong())).toInt()
        val minutes = (TimeUnit.SECONDS.toMinutes(totalSeconds.toLong())
                - TimeUnit.DAYS.toMinutes(days.toLong()) - TimeUnit.HOURS.toMinutes(hours.toLong())).toInt()
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        var format = ""
        if (full) {
            format = "%1\$dd %2\$dh %3\$dm %4\$ds"
        } else {
            // Hide all zero values
            if (days != 0) {
                format += "%1\$dd "
            }
            if (hours != 0) {
                format += "%2\$dh "
            }
            if (minutes != 0) {
                format += "%3\$dm "
            }
            // Only show seconds if it's less than a minute
            if (seconds != 0 && totalSeconds < SECONDS_PER_MINUTE) {
                format += "%4\$ds"
            }
            format = format.trim { it <= ' ' }
        }
        return String.format(format, days, hours, minutes, seconds)
    }
}