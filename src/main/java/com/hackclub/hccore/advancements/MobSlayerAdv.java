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

public class MasterMobSlayerAdv extends BaseAdvancement {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.NETHERITE_AXE, "Master Mob Slayer")
      .challengeFrame()
      .announceChat()
      .showToast()
      .description("Kill EVERY hostile Mob 10 times");
  static final int maxProgression = 640; // 10 times for each of the 64 hostile mobs

  public MasterMobSlayerAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(EntityDeathEvent.class, e -> {
      if (isHostile(e.getEntityType())) {
        if (e.getEntity().getKiller() != null) {
          incrementProgression(e.getEntity().getKiller());
        }
      }
    });
  }

  private boolean isHostile(EntityType entityType) {
    switch (entityType) {
      case BLAZE:
      case CREEPER:
      case DROWNED:
      case ELDER_GUARDIAN:
      case ENDERMAN:
      case ENDERMITE:
      case EVOKER:
      case GHAST:
      case GUARDIAN:
      case HOGLIN:
      case HUSK:
      case MAGMA_CUBE:
      case PHANTOM:
      case PIGLIN:
      case PIGLIN_BRUTE:
      case PILLAGER:
      case RAVAGER:
      case SHULKER:
      case SILVERFISH:
      case SKELETON:
      case SLIME:
      case SPIDER:
      case STRAY:
      case VEX:
      case VINDICATOR:
      case WITCH:
      case WITHER_SKELETON:
      case ZOGLIN:
      case ZOMBIE:
      case ZOMBIE_VILLAGER:
      case ZOMBIFIED_PIGLIN:
        return true;
      default:
        return false;
    }
  }
}
