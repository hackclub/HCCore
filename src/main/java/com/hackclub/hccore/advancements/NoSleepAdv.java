package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class NoSleepAdv extends BaseAdvancement {
  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
      new AdvancementDisplay.Builder(Material.RED_BED, "Please Sleep")
          .challengeFrame()
          .announceChat()
          .showToast()
          .description("Go to bed after staying up for a full week");
  static final int maxProgression = 1;

  public NoSleepAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);

    registerEvent(PlayerBedEnterEvent.class, e -> {
      if (e.getPlayer().getStatistic(Statistic.TIME_SINCE_REST) >= 20 * 60 * 60 * 24 * 7) {
        grant(e.getPlayer());
      }
    });
  }
}
