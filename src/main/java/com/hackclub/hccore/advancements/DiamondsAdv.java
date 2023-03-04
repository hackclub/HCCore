package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

public class DiamondsAdv extends BaseAdvancement {

  static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.DIAMOND_ORE, "Look Ma, " +
      "Diamonds!")
      .taskFrame()
      .announceChat()
      .showToast()
      .description("Find your first diamond while mining");
  static int maxProgression = 1;

  public DiamondsAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(BlockBreakEvent.class, e -> {
      if (e.getBlock().getType() == Material.DIAMOND_ORE
          || e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
        incrementProgression(e.getPlayer());
      }
    });
  }
}
