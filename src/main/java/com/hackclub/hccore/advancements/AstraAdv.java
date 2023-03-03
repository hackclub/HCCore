package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;

public class AstraAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(Material.FIREWORK_ROCKET, "Ad " +
            "Astra")
            .challengeFrame()
            .announceChat()
            .showToast()
            .description("Reach outer space and touch the stars");
    static int maxProgression = 1;
    static int minY = 10000;

    public AstraAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

        registerEvent(PlayerMoveEvent.class, e -> {
            if (e.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
                if (e.getPlayer().getLocation().getY() > minY) {
                    incrementProgression(e.getPlayer());
                }
            }
        });
    }
}
