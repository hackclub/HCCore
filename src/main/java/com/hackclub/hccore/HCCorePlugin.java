package com.hackclub.hccore;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.hackclub.hccore.advancements.MusicophileAdv;
import com.hackclub.hccore.commands.*;
import com.hackclub.hccore.listeners.*;
import com.hackclub.hccore.tasks.AutoAFKTask;
import com.hackclub.hccore.tasks.CheckAdAstraTask;
import com.hackclub.hccore.utils.TimeUtil;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class HCCorePlugin extends JavaPlugin {
    private DataManager dataManager;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Disable default advancement announcements
        for (World world : this.getServer().getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        // Create config
        this.saveDefaultConfig();

        // Load managers
        this.dataManager = new DataManager(this);
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        // Register commands
        this.getCommand("afk").setExecutor(new AFKCommand(this));
        this.getCommand("color").setExecutor(new ColorCommand(this));
        this.getCommand("loc").setExecutor(new LocCommand(this));
        this.getCommand("nick").setExecutor(new NickCommand(this));
        this.getCommand("ping").setExecutor(new PingCommand(this));
        this.getCommand("spawn").setExecutor(new SpawnCommand(this));
        this.getCommand("stats").setExecutor(new StatsCommand(this));
        // disable emote commands due to Player#chat not working with colours on (recent) paper
        // current behavior is being kicked, which while funny the first time, gets old fast
        //        this.getCommand("downvote").setExecutor(new DownvoteCommand(this));
        //        this.getCommand("shrug").setExecutor(new ShrugCommand(this));
        //        this.getCommand("tableflip").setExecutor(new TableflipCommand(this));
        //        this.getCommand("upvote").setExecutor(new UpvoteCommand(this));
        //        this.getCommand("angry").setExecutor(new AngryCommand(this));
        //        this.getCommand("flippedbytable").setExecutor(new FlippedByTableCommand(this));

        // Register advancements
        this.registerAdvancements();

        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new AdvancementListener(this), this);
        this.getServer().getPluginManager().registerEvents(new AFKListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BeehiveInteractionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SleepListener(this), this);

        // Register packet listeners
        this.getProtocolManager().addPacketListener(new NameChangeListener(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.PLAYER_INFO));

        // Register tasks
        new AutoAFKTask(this).runTaskTimer(this,
                (long) this.getConfig().getInt("settings.auto-afk-time") * TimeUtil.TICKS_PER_SECOND,
                30 * TimeUtil.TICKS_PER_SECOND);
        new CheckAdAstraTask(this).runTaskTimer(this, 0, 10 * TimeUtil.TICKS_PER_SECOND);

        // Register all the players that were online before this plugin was enabled (example
        // scenario: plugin reload) to prevent null pointer errors.
        this.getDataManager().registerAll();
    }

    @Override
    public void onDisable() {
        this.getDataManager().unregisterAll();
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }

    public AdvancementTab tab;
    public RootAdvancement root;

    private void registerAdvancements() {
        //        // Create regular advancements
        //        Advancement allMusicDiscs = factory.getAllItems("all_music_discs", root, "Musicophile",
        //                        "Collect every single music disc", Material.JUKEBOX, Material.MUSIC_DISC_11,
        //                        Material.MUSIC_DISC_13, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CAT,
        //                        Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL,
        //                        Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_PIGSTEP, Material.MUSIC_DISC_STAL,
        //                        Material.MUSIC_DISC_STRAD,
        //                        Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD)
        //                .setFrame(Advancement.Frame.CHALLENGE);
        //
        //        Advancement findBug = factory.getImpossible("find_bug", root, "Bug Squasher",
        //                "Find and report a bug", Material.IRON_BOOTS);
        //        Advancement contribute = factory.getImpossible("contribute", findBug, "pairsOfHands++",
        //                "Contribute to the server’s codebase on GitHub", Material.COMMAND_BLOCK);
        //
        //        Advancement mineDiamondOre = factory.getImpossible("mine_diamond_ore", root,
        //                "Look Ma, Diamonds!", "Find your first diamond while mining", Material.DIAMOND_ORE);
        //        Advancement connectToNetherHub = factory
        //                .getImpossible("connect_to_nether_hub", mineDiamondOre, "Linked Up",
        //                        "Connect your base to the Nether hub", Material.POWERED_RAIL)
        //                .setFrame(Advancement.Frame.GOAL);
        //        Advancement killDragonInsane = factory
        //                .getCountedImpossible("kill_dragon_insane", connectToNetherHub, "Dragon Master",
        //                        "Kill the Ender Dragon 10 times", Material.DRAGON_HEAD, 10)
        //                .setFrame(Advancement.Frame.CHALLENGE).setHidden(false);
        //        Advancement killWitherInsane = factory
        //                .getCountedImpossible("kill_wither_insane", killDragonInsane, "Are You Insane?!",
        //                        "Kill the Wither 20 times", Material.WITHER_SKELETON_SKULL, 20)
        //                .setFrame(Advancement.Frame.CHALLENGE);
        //        Advancement killElderGuardian = factory.getKill("kill_elder_guardian", mineDiamondOre,
        //                "The Deep End", "Defeat an Elder Guardian", Material.PRISMARINE_SHARD,
        //                EntityType.ELDER_GUARDIAN).setFrame(Advancement.Frame.GOAL);
        //        Advancement killWolf =
        //                factory.getKill("kill_wolf", mineDiamondOre, "You Monster!", "Slaughter a doggo",
        //                        Material.BONE, EntityType.WOLF).setFrame(Advancement.Frame.TASK);
        //        Advancement killIronGolem = factory
        //                .getKill("kill_iron_golem", killWolf, "Well That’s IRONic…",
        //                        "Kill an Iron Golem", Material.IRON_INGOT, EntityType.IRON_GOLEM)
        //                .setFrame(Advancement.Frame.TASK);
        //        Advancement millionMiler = factory
        //                .getImpossible("million_miler", mineDiamondOre, "Million Miler",
        //                        "Fly one million miles (1,609,344 km) with an elytra", Material.ELYTRA)
        //                .setFrame(Advancement.Frame.CHALLENGE);
        //        Advancement adAstra = factory
        //                .getImpossible("ad_astra", millionMiler, "Ad Astra",
        //                        "Reach outer space and touch the stars", Material.FIREWORK_ROCKET)
        //                .setFrame(Advancement.Frame.CHALLENGE);
        //
        //        // Activate all the advancements
        //        List<Advancement> advancements = new ArrayList<Advancement>() {
        //            private static final long serialVersionUID = 0L;
        //
        //            {
        //                add(root);
        //                add(allMusicDiscs);
        //                add(findBug);
        //                add(contribute);
        //                add(mineDiamondOre);
        //                add(connectToNetherHub);
        //                add(killDragonInsane);
        //                add(killWitherInsane);
        //                add(killElderGuardian);
        //                add(killWolf);
        //                add(killIronGolem);
        //                add(millionMiler);
        //                add(adAstra);
        //            }
        //        };
        //        for (Advancement advancement : advancements) {
        //            if (this.getServer().getAdvancement(advancement.getId()) == null) {
        //                advancement.activate(false);
        //            }
        //        }
        //
        //        // Reload the data cache after all advancements have been added
        //        this.getServer().reloadData();

        // Initialize advancement api
        UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(this);
        tab = api.createAdvancementTab("hack_club");

        // Create root display banner
        ItemStack bannerStack = new ItemStack(Material.RED_BANNER);
        BannerMeta bannerMeta = (BannerMeta) bannerStack.getItemMeta();
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_RIGHT));
        patterns.add(new Pattern(DyeColor.RED, PatternType.HALF_HORIZONTAL));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE));
        patterns.add(new Pattern(DyeColor.RED, PatternType.BORDER));
        bannerMeta.setPatterns(patterns);
        bannerStack.setItemMeta(bannerMeta);

        // Create root display
        AdvancementDisplay rootDisplay = new AdvancementDisplay(bannerStack, "Hack Club",
                AdvancementFrameType.TASK, false, false, 0, 0, "Beep boop beep beep boop");
        root = new RootAdvancement(tab, "root", rootDisplay, "textures/block/coal_block.png");

        MusicophileAdv musicophile = new MusicophileAdv(this);

        // Register all advancements
        tab.registerAdvancements(root, musicophile);
    }
}
