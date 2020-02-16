package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class AdvancementListener implements Listener {
    private final HCCorePlugin plugin;

    public AdvancementListener(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        // Ignore non-player killers
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity().getKiller();
        switch (event.getEntityType()) {
            case ENDER_DRAGON: {
                this.incrementAdvancementProgress(player,
                        new NamespacedKey(this.plugin, "kill_dragon_insane"));
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
