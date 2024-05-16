package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.player.PlayerMoveEvent;

public class MileAdv extends BaseAdvancement {
  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
      new AdvancementDisplay.Builder(Material.OBSIDIAN_BLOCK, "Do your fingers hurt?")
          .challengeFrame()
          .announceChat()
          .showToast()
          .description(
              "Mine one full stack of Obsidian by hand");
  static final int maxProgression = 1;

  public MileAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(PlayerMoveEvent.class, e -> {
      if (e.getPlayer().getStatistic(Statistic.WALK_ONE_CM) >= 1609.344 * 100 * 1000) {
        grant(e.getPlayer());
      }
    });
  }
}
