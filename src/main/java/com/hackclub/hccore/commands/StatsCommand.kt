package com.hackclub.hccore.commandsimport

import com.hackclub.hccore.utils.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Statistic
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

class StatsCommand(plugin: HCCorePlugin) : TabExecutor {
    private val plugin: HCCorePlugin
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        var extended = false

        // /stats
        if (args.size == 0) {
            if (sender is Player) {
                sender.sendMessage("Your stats:")
                sendStatistics(sender, sender, extended)
            } else {
                sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this")
            }
            return true
        }
        if (args.size > 1) {
            extended = when (args[1].toLowerCase()) {
                "extended" -> true
                "only" -> {
                    if (args.size < 3) {
                        sender.sendMessage(ChatColor.RED
                                .toString() + "You must include both a player and statistic name")
                        return true
                    }
                    if (!StatsCommand.Companion.STATISTIC_NAMES.contains(args[2].toLowerCase())) {
                        sender.sendMessage(ChatColor.RED.toString() + "Not a valid statistic")
                        return true
                    }
                    val specificStat = Statistic.valueOf(args[2].toUpperCase())
                    if (specificStat.isSubstatistic) {
                        sender.sendMessage(ChatColor.RED.toString() + "This statistic is not currently supported")
                        return true
                    }
                    val player = sender.server.getPlayerExact(args[0])
                    if (player != null) {
                        val data: PlayerData = plugin.getDataManager().getData(player)
                        sender.sendMessage(data.usableName + "’s " + args[2].toLowerCase()
                                + " statistic: " + player.getStatistic(specificStat))
                    } else {
                        sender.sendMessage(ChatColor.RED.toString() + "No online player with that name was found")
                    }
                    return true
                }
                else -> return false
            }
        }

        // /stats <player>
        val targetPlayer = sender.server.getPlayerExact(args[0])
        if (targetPlayer != null) {
            val data: PlayerData = plugin.getDataManager().getData(targetPlayer)
            sender.sendMessage(data.usableName + "’s stats:")
            sendStatistics(sender, targetPlayer, extended)
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "No online player with that name was found")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String,
                               args: Array<String>): List<String>? {
        val completions: MutableList<String> = ArrayList()
        when (args.size) {
            1 -> for (player in sender.server.onlinePlayers) {
                if (StringUtil.startsWithIgnoreCase(player.name, args[0])) {
                    completions.add(player.name)
                }
            }
            2 -> {
                val subcommands = Arrays.asList("extended", "only")
                StringUtil.copyPartialMatches<List<String>>(args[1], subcommands, completions)
            }
            3 -> {
                // Only send statistic name suggestions in /stats <player> only
                if (!args[1].equals("only", ignoreCase = true)) {
                    break
                }
                for (statistic in Statistic.values()) {
                    if (StringUtil.startsWithIgnoreCase(statistic.name, args[2])) {
                        completions.add(statistic.name.toLowerCase())
                    }
                }
            }
        }
        Collections.sort(completions)
        return completions
    }

    private fun sendStatistics(sender: CommandSender, player: Player, extended: Boolean) {
        sender.sendMessage("- Deaths: " + player.getStatistic(Statistic.DEATHS))
        sender.sendMessage("- Mob kills: " + player.getStatistic(Statistic.MOB_KILLS))
        sender.sendMessage("- Player kills: " + player.getStatistic(Statistic.PLAYER_KILLS))
        sender.sendMessage("- Time played: "
                + TimeUtil.toPrettyTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE)))
        sender.sendMessage("- Time since last death: "
                + TimeUtil.toPrettyTime(player.getStatistic(Statistic.TIME_SINCE_DEATH)))
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sender.sendMessage(
                "- Registered since: " + dateFormat.format(Date(player.firstPlayed)))
        if (extended) {
            sender.sendMessage("- Distance by elytra: "
                    + StatsCommand.Companion.toSIPrefix(player.getStatistic(Statistic.AVIATE_ONE_CM).toDouble()) + "m")
            sender.sendMessage("- Distance by minecart: "
                    + StatsCommand.Companion.toSIPrefix(player.getStatistic(Statistic.MINECART_ONE_CM).toDouble()) + "m")
            sender.sendMessage("- Distance by horse: "
                    + StatsCommand.Companion.toSIPrefix(player.getStatistic(Statistic.HORSE_ONE_CM).toDouble()) + "m")
            sender.sendMessage("- Distance walked: "
                    + StatsCommand.Companion.toSIPrefix(player.getStatistic(Statistic.WALK_ONE_CM).toDouble()) + "m")
            sender.sendMessage("- Damage taken: " + player.getStatistic(Statistic.DAMAGE_TAKEN))
            sender.sendMessage("- Damage dealt: " + player.getStatistic(Statistic.DAMAGE_DEALT))
            sender.sendMessage("- Times jumped: " + player.getStatistic(Statistic.JUMP))
            sender.sendMessage("- Raids won: " + player.getStatistic(Statistic.RAID_WIN))
            sender.sendMessage("- Diamonds picked up: "
                    + player.getStatistic(Statistic.PICKUP, Material.DIAMOND))
        }
    }

    companion object {
        private val STATISTIC_NAMES = Arrays.asList(*Statistic.values()).stream()
                .map { statistic: Statistic -> statistic.name.toLowerCase() }.collect(Collectors.toList())

        // converts numbers to their SI prefix laden counterparts
        private fun toSIPrefix(number: Double): String? {
            var number = number
            if (number < 100) {
                return "$number c"
            } else if (number < 100000) {
                number = Math.round(number / 100).toDouble()
                return number.toString()
            } else if (number >= 100000) {
                // Divides by 1000 to allow for two significant digits
                number = Math.round(number / 1000).toDouble()
                // Divides by 100 to finally get to km
                number /= 100.0
                return String.format("%.2f k", number)
            }
            return null
        }
    }

    init {
        this.plugin = plugin
    }
}