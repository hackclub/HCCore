package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class MileAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
            new AdvancementDisplay.Builder(Material.DIAMOND_BOOTS, "I'm Gonna Be")
            .challengeFrame()
            .announceChat()
            .showToast()
            .description("Just to be the man who walked a thousand miles (1609.344 km) to fall down at your door");
    static int maxProgression = 1;

    public MileAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

        registerEvent(PlayerStatisticIncrementEvent.class, e -> {
            if (e.getStatistic() == Statistic.WALK_ONE_CM) {
                if (e.getNewValue() >= 1609.344 * 100 * 1000) {
                    grant(e.getPlayer());
                }
            }
        });
    }
}
