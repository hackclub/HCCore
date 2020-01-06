package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepListener implements Listener {
    private final HCCorePlugin plugin;
    private int advanceTimeTaskId;

    public SleepListener(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        // Ignore unsuccessful attempts to sleep
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        World currentWorld = event.getPlayer().getWorld();
        // Add 1 to account for the player that just slept
        int sleepingPlayers = this.getSleepingPlayers(currentWorld) + 1;
        int minSleepingPlayers = this.getMinSleepingPlayersNeeded(currentWorld);
        this.plugin.getServer().broadcastMessage(ChatColor.GOLD + event.getPlayer().getDisplayName()
                + " is now sleeping (" + sleepingPlayers + "/" + minSleepingPlayers + " needed)");

        if (sleepingPlayers < minSleepingPlayers) {
            return;
        }
        // There's enough players in bed now, so advance to the next day
        final int SLEEP_DURATION_TICKS = 101;
        final int WAKE_AT_TICK = 0;
        this.advanceTimeTaskId = this.plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Advance to morning and clear thunderstorms
                        currentWorld.setTime(WAKE_AT_TICK);
                        if (currentWorld.isThundering()) {
                            currentWorld.setThundering(false);
                            currentWorld.setStorm(false);
                        }
                        plugin.getServer().broadcastMessage(
                                ChatColor.GREEN + "Good morning! Let's get this mf bread.");
                    }
                }, SLEEP_DURATION_TICKS);
    }

    @EventHandler
    public void onPlayerBedLeave(final PlayerBedLeaveEvent event) {
        World currentWorld = event.getPlayer().getWorld();
        int sleepingPlayers = this.getSleepingPlayers(currentWorld);
        int minSleepingPlayers = this.getMinSleepingPlayersNeeded(currentWorld);

        // Only show wake message if it's still within sleeping periods
        final int CLEAR_SLEEP_START_TICK = 12542;
        final int CLEAR_SLEEP_END_TICK = 23460;
        final int STORM_SLEEP_START_TICK = 12010;
        final int STORM_SLEEP_END_TICK = 23992;
        if (currentWorld.isThundering()
                || (currentWorld.hasStorm() && currentWorld.getTime() >= STORM_SLEEP_START_TICK
                        && currentWorld.getTime() <= STORM_SLEEP_END_TICK)
                || (currentWorld.getTime() >= CLEAR_SLEEP_START_TICK
                        && currentWorld.getTime() <= CLEAR_SLEEP_END_TICK)) {
            this.plugin.getServer().broadcastMessage(
                    ChatColor.GOLD + event.getPlayer().getDisplayName() + " has woken up ("
                            + sleepingPlayers + "/" + minSleepingPlayers + " needed)");
        }

        // Don't advance if we no longer have the minimum players needed
        if (sleepingPlayers < minSleepingPlayers) {
            this.plugin.getServer().getScheduler().cancelTask(this.advanceTimeTaskId);
        }
    }

    private int getSleepingPlayers(World world) {
        int sleepingPlayers = 0;
        for (Player player : world.getPlayers()) {
            if (player.isSleeping()) {
                sleepingPlayers++;
            }
        }
        return sleepingPlayers;
    }

    private int getMinSleepingPlayersNeeded(World world) {
        final double SLEEPING_MIN_PERCENTAGE = 0.5;
        int playersNeeded = (int) Math.ceil(world.getPlayers().size() * SLEEPING_MIN_PERCENTAGE);
        return playersNeeded;
    }
}
