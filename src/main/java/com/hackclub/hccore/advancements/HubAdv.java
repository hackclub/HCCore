package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;

public class HubAdv extends BaseAdvancement implements HiddenVisibility {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(
      Material.POWERED_RAIL, "Linked Up")
      .goalFrame()
      .announceChat()
      .showToast()
      .description("Connect your base to the Nether Hub");
  static final int maxProgression = 1;

  public HubAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);
  }
}
