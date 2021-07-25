package com.hackclub.hccoreimport

import com.hackclub.hccore.utils.TimeUtil
import hu.trigary.advancementcreator.Advancement
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
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

class HCCorePlugin : JavaPlugin() {
    var dataManager: DataManager? = null
        private set
    var protocolManager: ProtocolManager? = null
        private set
    private var bot: DiscordBot? = null
    override fun onEnable() {
        // Disable default advancement announcements
        for (world in server.worlds) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        }

        // Create config
        saveDefaultConfig()

        // Load managers
        dataManager = DataManager(this)
        protocolManager = ProtocolLibrary.getProtocolManager()
        if (this.config.getBoolean("settings.discord-link.enabled")) {
            bot = DiscordBot(this)
        }

        // Register commands
        getCommand("afk")!!.setExecutor(AFKCommand(this))
        getCommand("color")!!.setExecutor(ColorCommand(this))
        getCommand("downvote")!!.setExecutor(DownvoteCommand(this))
        getCommand("loc")!!.setExecutor(LocCommand(this))
        getCommand("nick")!!.setExecutor(NickCommand(this))
        getCommand("ping")!!.setExecutor(PingCommand(this))
        getCommand("shrug")!!.setExecutor(ShrugCommand(this))
        getCommand("spawn")!!.setExecutor(SpawnCommand(this))
        getCommand("stats")!!.setExecutor(StatsCommand(this))
        getCommand("tableflip")!!.setExecutor(TableflipCommand(this))
        getCommand("upvote")!!.setExecutor(UpvoteCommand(this))
        getCommand("discord")!!.setExecutor(DiscordCommand(this))

        // Register event listeners
        server.pluginManager.registerEvents(AdvancementListener(this), this)
        server.pluginManager.registerEvents(AFKListener(this), this)
        server.pluginManager.registerEvents(BeehiveInteractionListener(this),
                this)
        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(SleepListener(this), this)

        // Register packet listeners
        protocolManager.addPacketListener(NameChangeListener(this,
                ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO))

        // Register tasks
        AutoAFKTask(this).runTaskTimer(this, (
                this.config.getInt("settings.auto-afk-time") * TimeUtil.TICKS_PER_SECOND).toLong(), (
                30 * TimeUtil.TICKS_PER_SECOND).toLong())
        CheckAdAstraTask(this).runTaskTimer(this, 0, (10 * TimeUtil.TICKS_PER_SECOND).toLong())

        // Register advancements
        registerAdvancements()

        // Register all the players that were online before this plugin was enabled (example
        // scenario: plugin reload) to prevent null pointer errors.
        dataManager.registerAll()
    }

    override fun onDisable() {
        dataManager!!.unregisterAll()
        bot!!.close()
        bot = null
    }

    private fun registerAdvancements() {
        val factory = AdvancementFactory(this, false, false)

        // Create root advancement
        val hackClubBanner = ItemObject().setItem(Material.RED_BANNER).setNbt(
                "{BlockEntityTag:{Patterns:[{Color:0,Pattern:\"rs\"},{Color:14,Pattern:\"hh\"},{Color:0,Pattern:\"ls\"},{Color:0,Pattern:\"ms\"},{Color:14,Pattern:\"bo\"}]}}")
        val root = Advancement(NamespacedKey(this, "root"), hackClubBanner,
                TextComponent("Hack Club"), TextComponent("Beep boop beep beep boop"))
                .makeRoot("block/coal_block", true).setFrame(Advancement.Frame.TASK)

        // Create regular advancements
        val allMusicDiscs = factory.getAllItems("all_music_discs", root, "Musicophile",
                "Collect every single music disc", Material.JUKEBOX, Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_13, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_PIGSTEP, Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD)
                .setFrame(Advancement.Frame.CHALLENGE)
        val findBug = factory.getImpossible("find_bug", root, "Bug Squasher",
                "Find and report a bug", Material.IRON_BOOTS)
        val contribute = factory.getImpossible("contribute", findBug, "pairsOfHands++",
                "Contribute to the server’s codebase on GitHub", Material.COMMAND_BLOCK)
        val mineDiamondOre = factory.getImpossible("mine_diamond_ore", root,
                "Look Ma, Diamonds!", "Find your first diamond while mining", Material.DIAMOND_ORE)
        val connectToNetherHub = factory
                .getImpossible("connect_to_nether_hub", mineDiamondOre, "Linked Up",
                        "Connect your base to the Nether hub", Material.POWERED_RAIL)
                .setFrame(Advancement.Frame.GOAL)
        val killDragonInsane = factory
                .getCountedImpossible("kill_dragon_insane", connectToNetherHub, "Dragon Master",
                        "Kill the Ender Dragon 10 times", Material.DRAGON_HEAD, 10)
                .setFrame(Advancement.Frame.CHALLENGE).setHidden(false)
        val killWitherInsane = factory
                .getCountedImpossible("kill_wither_insane", killDragonInsane, "Are You Insane?!",
                        "Kill the Wither 20 times", Material.WITHER_SKELETON_SKULL, 20)
                .setFrame(Advancement.Frame.CHALLENGE)
        val killElderGuardian = factory.getKill("kill_elder_guardian", mineDiamondOre,
                "The Deep End", "Defeat an Elder Guardian", Material.PRISMARINE_SHARD,
                EntityType.ELDER_GUARDIAN).setFrame(Advancement.Frame.GOAL)
        val killWolf = factory.getKill("kill_wolf", mineDiamondOre, "You Monster!", "Slaughter a doggo",
                Material.BONE, EntityType.WOLF).setFrame(Advancement.Frame.TASK)
        val killIronGolem = factory
                .getKill("kill_iron_golem", killWolf, "Well That’s IRONic…",
                        "Kill an Iron Golem", Material.IRON_INGOT, EntityType.IRON_GOLEM)
                .setFrame(Advancement.Frame.TASK)
        val millionMiler = factory
                .getImpossible("million_miler", mineDiamondOre, "Million Miler",
                        "Fly one million miles (1,609,344 km) with an elytra", Material.ELYTRA)
                .setFrame(Advancement.Frame.CHALLENGE)
        val adAstra = factory
                .getImpossible("ad_astra", millionMiler, "Ad Astra",
                        "Reach outer space and touch the stars", Material.FIREWORK_ROCKET)
                .setFrame(Advancement.Frame.CHALLENGE)

        // Activate all the advancements
        val advancements: List<Advancement> = object : ArrayList<Advancement?>() {
            private static
            val serialVersionUID = 0L

            init {
                add(root)
                add(allMusicDiscs)
                add(findBug)
                add(contribute)
                add(mineDiamondOre)
                add(connectToNetherHub)
                add(killDragonInsane)
                add(killWitherInsane)
                add(killElderGuardian)
                add(killWolf)
                add(killIronGolem)
                add(millionMiler)
                add(adAstra)
            }
        }
        for (advancement in advancements) {
            if (server.getAdvancement(advancement.id) == null) {
                advancement.activate(false)
            }
        }

        // Reload the data cache after all advancements have been added
        server.reloadData()
    }
}