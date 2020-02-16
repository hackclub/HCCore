package com.hackclub.hccore;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.hackclub.hccore.commands.AFKCommand;
import com.hackclub.hccore.commands.ColorCommand;
import com.hackclub.hccore.commands.LocCommand;
import com.hackclub.hccore.commands.NickCommand;
import com.hackclub.hccore.commands.ShrugCommand;
import com.hackclub.hccore.commands.SpawnCommand;
import com.hackclub.hccore.commands.StatsCommand;
import com.hackclub.hccore.listeners.BeehiveInteractionListener;
import com.hackclub.hccore.listeners.NameChangeListener;
import com.hackclub.hccore.listeners.PlayerListener;
import com.hackclub.hccore.listeners.SleepListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import hu.trigary.advancementcreator.Advancement;
import hu.trigary.advancementcreator.Advancement.Frame;
import hu.trigary.advancementcreator.AdvancementFactory;
import hu.trigary.advancementcreator.shared.ItemObject;
import net.md_5.bungee.api.chat.TextComponent;

public class HCCorePlugin extends JavaPlugin {
    private DataManager dataManager;
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Load managers
        this.dataManager = new DataManager(this);
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        // Register commands
        this.getCommand("afk").setExecutor(new AFKCommand(this));
        this.getCommand("color").setExecutor(new ColorCommand(this));
        this.getCommand("loc").setExecutor(new LocCommand(this));
        this.getCommand("nick").setExecutor(new NickCommand(this));
        this.getCommand("shrug").setExecutor(new ShrugCommand(this));
        this.getCommand("spawn").setExecutor(new SpawnCommand(this));
        this.getCommand("stats").setExecutor(new StatsCommand(this));

        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new BeehiveInteractionListener(this),
                this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SleepListener(this), this);

        // Register packet listeners
        this.getProtocolManager().addPacketListener(new NameChangeListener(this,
                ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO));

        // Register advancements
        this.registerAdvancements();

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
        AdvancementFactory factory = new AdvancementFactory(this, true, true);

        ItemObject hackClubBanner = new ItemObject().setItem(Material.RED_BANNER).setNbt(
                "{BlockEntityTag:{Patterns:[{Color:0,Pattern:\"rs\"},{Color:14,Pattern:\"hh\"},{Color:0,Pattern:\"ls\"},{Color:0,Pattern:\"ms\"},{Color:14,Pattern:\"bo\"}]}}");
        Advancement root = new Advancement(new NamespacedKey(this, "root"), hackClubBanner,
                new TextComponent("Hack Club"), new TextComponent("Beep boop beep beep boop"))
                        .makeRoot("block/coal_block", true).setFrame(Frame.TASK);
        root.activate(true);

        Advancement connectToNetherHub = factory.getImpossible("connect_to_nether_hub", root,
                "Linked Up", "Connect your base to the Nether hub", Material.POWERED_RAIL);
        Advancement contribute = factory.getImpossible("contribute", root, "pairsOfHands++",
                "Contribute to the server’s codebase on GitHub", Material.COMMAND_BLOCK);
        Advancement killDragonInsane = factory
                .getCountedImpossible("kill_dragon_insane", root, "Are You Insane?",
                        "Kill the Ender Dragon 10 times", Material.DRAGON_HEAD, 10)
                .setFrame(Advancement.Frame.CHALLENGE);
        Advancement killWitherInsane = factory
                .getCountedImpossible("kill_wither_insane", root, "Totally Dominated",
                        "Kill the Wither 20 times", Material.WITHER_SKELETON_SKULL, 20)
                .setFrame(Advancement.Frame.CHALLENGE);
    }
}
