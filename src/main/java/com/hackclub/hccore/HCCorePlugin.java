package com.hackclub.hccore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.hackclub.hccore.commands.AFKCommand;
import com.hackclub.hccore.commands.LocCommand;
import com.hackclub.hccore.commands.NickCommand;
import com.hackclub.hccore.commands.ShrugCommand;
import com.hackclub.hccore.commands.SpawnCommand;
import com.hackclub.hccore.listeners.BeehiveInteractionListener;
import com.hackclub.hccore.listeners.PlayerListener;
import com.hackclub.hccore.listeners.SleepListener;
import org.bukkit.plugin.java.JavaPlugin;

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
        this.getCommand("loc").setExecutor(new LocCommand(this));
        this.getCommand("nick").setExecutor(new NickCommand(this));
        this.getCommand("shrug").setExecutor(new ShrugCommand(this));
        this.getCommand("spawn").setExecutor(new SpawnCommand(this));

        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new BeehiveInteractionListener(this),
                this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SleepListener(this), this);
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }
}
