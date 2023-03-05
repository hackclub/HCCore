package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

public class DragonAdv extends BaseAdvancement {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.DRAGON_HEAD, "Dragon " +
      "Master")
      .challengeFrame()
      .announceChat()
      .showToast()
      .description("Kill the Ender Dragon 10 times");
  static final int maxProgression = 10;

  public DragonAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(EntityDeathEvent.class, e -> {
      if (e.getEntityType() == EntityType.ENDER_DRAGON) {
        if (e.getEntity().getKiller() != null) {
          incrementProgression(e.getEntity().getKiller());
        }
      }
    });
  }
}
