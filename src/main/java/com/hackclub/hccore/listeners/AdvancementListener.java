package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {
    private final HCCorePlugin plugin;

    public AdvancementListener(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        // Check if it's a diamond ore
        if (event.getBlock().getType() != Material.DIAMOND_ORE) {
            return;
        }

        // The diamond ore wasn't found underground
        final int MAX_Y_DIAMOND_ORE = 16;
        if (event.getBlock().getY() > MAX_Y_DIAMOND_ORE) {
            return;
        }

        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(this.plugin, "mine_diamond_ore");
        AdvancementProgress progress =
                player.getAdvancementProgress(player.getServer().getAdvancement(key));
        // Skip if player already has this advancement
        if (progress.isDone()) {
            return;
        }

        this.grantAdvancement(player, key);
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        // Ignore non-player killers
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity().getKiller();
        switch (event.getEntityType()) {
            case ELDER_GUARDIAN: {
                this.grantAdvancement(player,
                        new NamespacedKey(this.plugin, "kill_elder_guardian"));
                break;
            }
            case ENDER_DRAGON: {
                this.incrementAdvancementProgress(player,
                        new NamespacedKey(this.plugin, "kill_dragon_insane"));

                // Give "The Next Generation" since it's not easy to get it after the egg
                // is taken
                this.grantAdvancement(player, NamespacedKey.minecraft("end/dragon_egg"));
                break;
            }
            case WITHER: {
                this.incrementAdvancementProgress(player,
                        new NamespacedKey(this.plugin, "kill_wither_insane"));
                break;
            }
            default:
                break;
        }
    }

    @EventHandler
    public void onPlayerAdvancementDone(final PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        if (event.getAdvancement().getKey()
                .equals(new NamespacedKey(this.plugin, "mine_diamond_ore"))) {
            player.sendMessage(ChatColor.GREEN
                    + "Congrats, you’ve found your very first diamond! You are now eligible for the exclusive (and limited edition!) Hack Club Minecraft stickers. Head over to "
                    + ChatColor.UNDERLINE + this.plugin.getConfig().getString("claim-stickers-url")
                    + ChatColor.RESET + ChatColor.GREEN + " to claim them!*");
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString()
                    + "*This offer only applies to players who have never received the stickers before. If you have, please do not fill out the form again!");
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString()
                    + "You will only see this message once.");
        }
    }

    private void grantAdvancement(Player player, NamespacedKey key) {
        AdvancementProgress progress =
                player.getAdvancementProgress(player.getServer().getAdvancement(key));
        if (progress.isDone()) {
            return;
        }

        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
    }

    private void incrementAdvancementProgress(Player player, NamespacedKey key) {
        AdvancementProgress progress =
                player.getAdvancementProgress(player.getServer().getAdvancement(key));
        if (progress.isDone()) {
            return;
        }

        String nextCriteria = String.valueOf(progress.getAwardedCriteria().size());
        progress.awardCriteria(nextCriteria);
    }
}
