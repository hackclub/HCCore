package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import org.bukkit.Material;

public class ContributeAdv extends BaseAdvancement {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.COMMAND_BLOCK,
      "Contributor")
      .taskFrame()
      .announceChat()
      .showToast()
      .description("Contribute to the server's codebase on GitHub");
  static final int maxProgression = 1;

  public ContributeAdv(Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);
  }
}
