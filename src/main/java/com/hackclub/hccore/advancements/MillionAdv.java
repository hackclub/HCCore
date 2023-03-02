package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;

public class MillionAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(Material.ELYTRA, "Million Miler")
            .challengeFrame()
            .announceChat()
            .showToast()
            .description("Fly one million miles (1,609,344 km) with an elytra");
    static int maxProgression = 1;

    public MillionAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);
    }
}
