package com.hackclub.hccore.listenersimport

import com.comphenix.protocol.wrappers.PlayerInfoData
import java.util.ArrayList

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

// Modified from https://gist.github.com/Techcable/d574a6e78ada3b8511bd
class NameChangeListener(plugin: HCCorePlugin?, listenerPriority: ListenerPriority?,
                         vararg types: PacketType?) : PacketAdapter(params(plugin, *types).listenerPriority(listenerPriority!!)) {
    private val plugin: HCCorePlugin
    override fun onPacketSending(event: PacketEvent) {
        // Only intercept packets that add players to the list
        if (event.packet.playerInfoAction
                        .read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
            return
        }
        val playerInfoDataList = event.packet.playerInfoDataLists.read(0)
        val newPlayerInfoDataList: MutableList<PlayerInfoData?> = ArrayList()
        for (playerInfoData in playerInfoDataList) {
            val player = event.player.server.getPlayer(playerInfoData!!.profile.uuid)
            // If any of this doesn't exist but it's in the list, just add it to the new one
            // and forget about it.
            if (playerInfoData == null || playerInfoData.profile == null || player == null) {
                newPlayerInfoDataList.add(playerInfoData)
                continue
            }

            // Create a profile with a custom name from current one
            val newName: String = this.plugin.getDataManager().getData(player).getUsableName()
            val newProfile = playerInfoData.profile.withName(newName)
            // Copy properties (currently just skin texture) to new profile
            newProfile.properties.putAll(playerInfoData.profile.properties)

            // Put all of the info from the old profile into the new one
            val newPlayerInfoData = PlayerInfoData(newProfile, playerInfoData.latency,
                    playerInfoData.gameMode, playerInfoData.displayName)
            newPlayerInfoDataList.add(newPlayerInfoData)
        }

        // Send the modified list
        event.packet.playerInfoDataLists.write(0, newPlayerInfoDataList)
    }

    init {
        this.plugin = plugin
    }
}