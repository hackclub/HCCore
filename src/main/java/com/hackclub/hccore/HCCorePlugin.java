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
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.advancements.*;
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
    public AdvancementTab tab;
    public RootAdvancement root;

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

        // Register advancements
        this.registerAdvancements();

        // Register commands
        this.getCommand("afk").setExecutor(new AFKCommand(this));
        this.getCommand("color").setExecutor(new ColorCommand(this));
        this.getCommand("hcadvancement").setExecutor(new HCAdvancementCommand(this));
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

    private void registerAdvancements() {
        // Initialize advancement api
        UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(this);
        tab = api.createAdvancementTab("hccore");

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

        AdvancementKey astraKey = new AdvancementKey(this, "astra");
        AdvancementKey bugKey = new AdvancementKey(this, "bug");
        AdvancementKey contributeKey = new AdvancementKey(this, "contribute");
        AdvancementKey diamondsKey = new AdvancementKey(this, "diamonds");
        AdvancementKey dragonKey = new AdvancementKey(this, "dragon");
        AdvancementKey elderKey = new AdvancementKey(this, "elder");
        AdvancementKey hubKey = new AdvancementKey(this, "hub");
        AdvancementKey ironGolemKey = new AdvancementKey(this, "iron_golem");
        AdvancementKey millionKey = new AdvancementKey(this, "million");
        AdvancementKey musicophileKey = new AdvancementKey(this, "musicophile");
        AdvancementKey witherKey = new AdvancementKey(this, "wither");
        AdvancementKey wolfKey = new AdvancementKey(this, "wolf");

        CoordAdapter adapter = CoordAdapter.builder()
                .add(astraKey, 3, -4)
                .add(bugKey, 1, 1)
                .add(contributeKey, 2, 1)
                .add(diamondsKey, 1, -1)
                .add(dragonKey, 3, -1)
                .add(elderKey, 2, -2)
                .add(hubKey, 2, -1)
                .add(ironGolemKey, 3, -3)
                .add(millionKey, 2, -4)
                .add(musicophileKey, 1, 0)
                .add(witherKey, 4, -1)
                .add(wolfKey, 2, -3)
                .build();

        MusicophileAdv musicophile = new MusicophileAdv(this, root, musicophileKey, adapter);
        BugAdv bug = new BugAdv(this, root, bugKey, adapter);
        ContributeAdv contribute = new ContributeAdv(this, bug, contributeKey, adapter);
        DiamondsAdv diamonds = new DiamondsAdv(this, root, diamondsKey, adapter);
        HubAdv hub = new HubAdv(this, diamonds, hubKey, adapter);
        DragonAdv dragon = new DragonAdv(this, hub, dragonKey, adapter);
        WitherAdv wither = new WitherAdv(this, dragon, witherKey, adapter);
        ElderAdv elder = new ElderAdv(this, diamonds, elderKey, adapter);
        WolfAdv wolf = new WolfAdv(this, diamonds, wolfKey, adapter);
        IronGolemAdv ironGolem = new IronGolemAdv(this, wolf, ironGolemKey, adapter);
        MillionAdv million = new MillionAdv(this, diamonds, millionKey, adapter);
        AstraAdv astra = new AstraAdv(this, million, astraKey, adapter);

        // Register all advancements
        tab.registerAdvancements(root, musicophile, bug, contribute, diamonds, hub, dragon, wither, elder, wolf,
                ironGolem, million, astra);
    }
}
