package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;

public class AstraAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(Material.FIREWORK_ROCKET, "Ad " +
            "Astra")
            .challengeFrame()
            .announceChat()
            .showToast()
            .description("Reach outer space and touch the stars");
    static int maxProgression = 1;

    public AstraAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);
    }
}
