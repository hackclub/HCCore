package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

public class IronGolemAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(Material.IRON_INGOT, "Well " +
            "That's IRONic...")
            .taskFrame()
            .announceChat()
            .showToast()
            .description("Kill an Iron Golem");
    static int maxProgression = 1;

    public IronGolemAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

        registerEvent(EntityDeathEvent.class, e -> {
            if (e.getEntityType() == EntityType.IRON_GOLEM) {
                if (e.getEntity().getKiller() != null) {
                    incrementProgression(e.getEntity().getKiller());
                }
            }
        });
    }
}
