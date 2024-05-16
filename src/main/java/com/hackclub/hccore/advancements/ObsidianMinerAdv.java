package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.block.BlockBreakEvent;

public class ObsidianMinerAdv extends BaseAdvancement {
  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
      new AdvancementDisplay.Builder(Material.OBSIDIAN, "Do your fingers hurt?")
          .challengeFrame()
          .announceChat()
          .showToast()
          .description("Mine 10 pieces of Obsidian by hand");
  static final int maxProgression = 10;

  public ObsidianMinerAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(BlockBreakEvent.class, e -> {
      if (e.getBlock().getType() == Material.OBSIDIAN && e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
        incrementProgression(e.getPlayer());
      }
    });
  }
}
