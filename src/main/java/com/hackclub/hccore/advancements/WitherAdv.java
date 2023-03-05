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

public class WitherAdv extends BaseAdvancement {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.WITHER_SKELETON_SKULL,
      "Are You" +
          " Insane?!")
      .challengeFrame()
      .announceChat()
      .showToast()
      .description("Kill the Wither 20 times");
  static final int maxProgression = 20;

  public WitherAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(EntityDeathEvent.class, e -> {
      if (e.getEntityType() == EntityType.WITHER) {
        if (e.getEntity().getKiller() != null) {
          incrementProgression(e.getEntity().getKiller());
        }
      }
    });
  }
}
