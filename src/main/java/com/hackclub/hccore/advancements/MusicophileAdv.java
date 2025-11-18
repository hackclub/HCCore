package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class MusicophileAdv extends BaseAdvancement {

  static final AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
      new AdvancementDisplay.Builder(Material.JUKEBOX, "Musicophile")
          .challengeFrame()
          .announceChat()
          .showToast()
          .description("Collect every single music disc");
  static final Material[] musicDiscs = {Material.MUSIC_DISC_13, Material.MUSIC_DISC_CAT,
      Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR,
      Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_STAL,
      Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WARD, Material.MUSIC_DISC_11,
      Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_OTHERSIDE, Material.MUSIC_DISC_5,
      Material.MUSIC_DISC_PIGSTEP, Material.MUSIC_DISC_RELIC, Material.MUSIC_DISC_CREATOR,
      Material.MUSIC_DISC_CREATOR_MUSIC_BOX, Material.MUSIC_DISC_PRECIPICE};

  private final HCCorePlugin plugin;

  @SuppressWarnings("unchecked")
  public MusicophileAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key,
      CoordAdapter adapter) {
    super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, musicDiscs.length);
    this.plugin = plugin;


+    registerEvent(InventoryClickEvent.class, this::handleInventoryClick);
+    registerEvent(EntityPickupItemEvent.class, this::handleItemPickup);

    private void handleInventoryClick(InventoryClickEvent event) {
      ItemStack currentItem = event.getCurrentItem();
      if (currentItem == null) {
        return;
      }

      if (!(event.getWhoClicked() instanceof Player player)) {
        return;
      }

      if (!musicDiscs.contains(currentItem.getType())) {
        return;
      }

      handleDiscCollection(player, currentItem.getType());
    }

    private void handleItemPickup(EntityPickupItemEvent event) {
      if (!(event.getEntity() instanceof Player player)) {
        return;
      }

      Material itemType = event.getItem().getItemStack().getType();

      if (!musicDiscs.contains(itemType)) {
        return;
      }

      handleDiscCollection(player, itemType);
    }

    private void handleDiscCollection(Player player, Material disc) {
      String discName = disc.toString();

      PlayerData data = plugin.getDataManager().getData(player);
      if (data == null) {
        return;
      }

      if (data.getCollectedMusicDiscs().contains(discName)) {
        return;
      }

      if (data.addCollectedMusicDisc(discName)) {
        data.save();

        incrementProgression(player.getUniqueId());
      }
    }
  }
}
