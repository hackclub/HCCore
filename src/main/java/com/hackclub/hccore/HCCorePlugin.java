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
import com.fren_gor.ultimateAdvancementAPI.events.advancement.ProgressionUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.advancements.AstraAdv;
import com.hackclub.hccore.advancements.BugAdv;
import com.hackclub.hccore.advancements.ContributeAdv;
import com.hackclub.hccore.advancements.DiamondsAdv;
import com.hackclub.hccore.advancements.DragonAdv;
import com.hackclub.hccore.advancements.ElderAdv;
import com.hackclub.hccore.advancements.HubAdv;
import com.hackclub.hccore.advancements.IronGolemAdv;
import com.hackclub.hccore.advancements.MileAdv;
import com.hackclub.hccore.advancements.MusicophileAdv;
import com.hackclub.hccore.advancements.WitherAdv;
import com.hackclub.hccore.advancements.WolfAdv;
import com.hackclub.hccore.commands.AFKCommand;
import com.hackclub.hccore.commands.ColorCommand;
import com.hackclub.hccore.commands.LocCommand;
import com.hackclub.hccore.commands.NickCommand;
import com.hackclub.hccore.commands.PingCommand;
import com.hackclub.hccore.commands.RulesCommand;
import com.hackclub.hccore.commands.SlackCommand;
import com.hackclub.hccore.commands.SpawnCommand;
import com.hackclub.hccore.commands.StatsCommand;
import com.hackclub.hccore.commands.WelcomeCommand;
import com.hackclub.hccore.commands.messaging.MessageCommand;
import com.hackclub.hccore.commands.messaging.ReplyCommand;
import com.hackclub.hccore.listeners.AFKListener;
import com.hackclub.hccore.listeners.BeehiveInteractionListener;
import com.hackclub.hccore.listeners.NameChangeListener;
import com.hackclub.hccore.listeners.PlayerListener;
import com.hackclub.hccore.listeners.ServerListPingListener;
import com.hackclub.hccore.slack.SlackBot;
import com.hackclub.hccore.tasks.AutoAFKTask;
import com.hackclub.hccore.tasks.IconTask;
import com.hackclub.hccore.utils.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

public class HCCorePlugin extends JavaPlugin {

  private DataManager dataManager;
  private ProtocolManager protocolManager;
  private SlackBot bot;

  // Server icon from Shrimp Shuffler
  public volatile CachedServerIcon serverIcon = null;

  @Override
  public void onEnable() {
    // enable default advancement announcements, should probably leave default, but removes need to re-enable on each server
    for (World world : this.getServer().getWorlds()) {
      world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);
    }

    // Create config
    this.saveDefaultConfig();

    // Load managers
    this.dataManager = new DataManager(this);
    this.protocolManager = ProtocolLibrary.getProtocolManager();

    if (this.getConfig().getBoolean("settings.slack-link.enabled", false)) {
      try {
        this.bot = new SlackBot(this);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // Register commands

    registerCommand("afk", new AFKCommand(this));
    registerCommand("color", new ColorCommand(this));
    registerCommand("loc", new LocCommand(this));
    registerCommand("nick", new NickCommand(this));
    registerCommand("ping", new PingCommand(this));
    registerCommand("slack", new SlackCommand(this));
    registerCommand("spawn", new SpawnCommand(this));
    registerCommand("stats", new StatsCommand(this));
    registerCommand("rules", new RulesCommand());
    registerCommand("welcome", new WelcomeCommand());
    registerCommand("msg", new MessageCommand(this));
    registerCommand("reply", new ReplyCommand(this));

    // prepare for new emotes commands:
    // downvote       "↓"
    // shrug          "¯\_(ツ)_/¯"
    // tableflip      "(╯°□°）╯︵ ┻━┻"
    // upvote         "↑"
    // angry          "ಠ_ಠ"

    // Register advancements
    this.registerAdvancements();

    // Register event listeners
    if (this.bot != null) {
      this.getServer().getPluginManager().registerEvents(this.bot, this);
      this.advancementTab.getEventManager().register(this.bot, ProgressionUpdateEvent.class,
          this.bot::onCustomAdvancementProgressed);
    }

    this.getServer().getPluginManager().registerEvents(new AFKListener(this), this);
    this.getServer().getPluginManager().registerEvents(new BeehiveInteractionListener(), this);
    this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    this.getServer().getPluginManager().registerEvents(new ServerListPingListener(this), this);

    // Register packet listeners
    this.getProtocolManager().addPacketListener(
        new NameChangeListener(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO));

    // Register tasks
    new AutoAFKTask(this).runTaskTimer(this,
        (long) this.getConfig().getInt("settings.auto-afk-time") * TimeUtil.TICKS_PER_SECOND,
        30 * TimeUtil.TICKS_PER_SECOND);
    getServer().getScheduler().runTaskTimerAsynchronously(this, new IconTask(this), 20, (long) this.getConfig().getInt("settings.icon-change-time") * TimeUtil.TICKS_PER_SECOND);

    // Register all the players that were online before this plugin was enabled (example
    // scenario: plugin reload) to prevent null pointer errors.
    this.getDataManager().registerAll();

    // Add the server icon to the variable
    serverIcon = getServer().getServerIcon();
  }

  @Override
  public void onDisable() {
    this.getDataManager().unregisterAll();

    if (this.bot != null) {
      try {
        this.bot.disconnect();
      } catch (Exception e) {
        e.printStackTrace();
      }

      this.bot = null;
    }
  }

  public DataManager getDataManager() {
    return this.dataManager;
  }

  public ProtocolManager getProtocolManager() {
    return this.protocolManager;
  }

  public SlackBot getSlackBot() {
    return this.bot;
  }

  public AdvancementTab advancementTab;
  public RootAdvancement root;

  private void registerAdvancements() {
    // Initialize advancement api
    UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(this);
    advancementTab = api.createAdvancementTab("hack_club");

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
        AdvancementFrameType.TASK, false, false, 0, 3, "Beep boop beep beep boop");
    root = new RootAdvancement(advancementTab, "root", rootDisplay,
        "textures/block/coal_block.png");

    AdvancementKey astraKey = new AdvancementKey(this, "astra");
    AdvancementKey bugKey = new AdvancementKey(this, "bug");
    AdvancementKey contributeKey = new AdvancementKey(this, "contribute");
    AdvancementKey diamondsKey = new AdvancementKey(this, "diamonds");
    AdvancementKey dragonKey = new AdvancementKey(this, "dragon");
    AdvancementKey elderKey = new AdvancementKey(this, "elder");
    AdvancementKey hubKey = new AdvancementKey(this, "hub");
    AdvancementKey ironGolemKey = new AdvancementKey(this, "iron_golem");
    AdvancementKey mileKey = new AdvancementKey(this, "mile");
    AdvancementKey musicophileKey = new AdvancementKey(this, "musicophile");
    AdvancementKey witherKey = new AdvancementKey(this, "wither");
    AdvancementKey wolfKey = new AdvancementKey(this, "wolf");

    CoordAdapter adapter = CoordAdapter.builder().add(astraKey, 5, 3).add(bugKey, 1, 2)
        .add(contributeKey, 2, 2).add(diamondsKey, 1, 4).add(dragonKey, 3, 4).add(elderKey, 4, 4)
        .add(hubKey, 6, 4).add(ironGolemKey, 2, 5).add(mileKey, 5, 4).add(musicophileKey, 1, 3)
        .add(witherKey, 3, 3).add(wolfKey, 2, 4).build();

    MusicophileAdv musicophile = new MusicophileAdv(this, root, musicophileKey, adapter);
    BugAdv bug = new BugAdv(root, bugKey, adapter);
    ContributeAdv contribute = new ContributeAdv(bug, contributeKey, adapter);
    DiamondsAdv diamonds = new DiamondsAdv(this, root, diamondsKey, adapter);
    HubAdv hub = new HubAdv(diamonds, hubKey, adapter);
    DragonAdv dragon = new DragonAdv(diamonds, dragonKey, adapter);
    WitherAdv wither = new WitherAdv(dragon, witherKey, adapter);
    ElderAdv elder = new ElderAdv(diamonds, elderKey, adapter);
    WolfAdv wolf = new WolfAdv(diamonds, wolfKey, adapter);
    IronGolemAdv ironGolem = new IronGolemAdv(wolf, ironGolemKey, adapter);
    MileAdv mile = new MileAdv(diamonds, mileKey, adapter);
    AstraAdv astra = new AstraAdv(mile, astraKey, adapter);

    // Register all advancements
    advancementTab.registerAdvancements(root, musicophile, bug, contribute, diamonds, hub, dragon,
        wither, elder, wolf, ironGolem, mile, astra);
  }

  private void registerCommand(String name, CommandExecutor commandExecutor) {
    PluginCommand command = this.getCommand(name);
    if (command == null) {
      this.getLogger().severe("Command %s not found in plugin.yml".formatted(name));
      return;
    }
    command.setExecutor(commandExecutor);
  }

  public static HCCorePlugin getInstance() {
    return HCCorePlugin.getPlugin(HCCorePlugin.class);
  }

}
