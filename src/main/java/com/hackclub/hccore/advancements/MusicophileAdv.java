package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class MusicophileAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder =
            new AdvancementDisplay.Builder(Material.JUKEBOX, "Musicophile")
                    .challengeFrame()
                    .announceChat()
                    .showToast()
                    .description("Collect every single music disc");
    static Material[] musicDiscs = {Material.MUSIC_DISC_13, Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_BLOCKS,
            Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI
            , Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WARD, Material.MUSIC_DISC_11,
            Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_OTHERSIDE, Material.MUSIC_DISC_5,
            Material.MUSIC_DISC_PIGSTEP};
    @SuppressWarnings("unchecked")
    public MusicophileAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, musicDiscs.length);

        for (Material disc : musicDiscs) {
            registerEvent(InventoryClickEvent.class, e -> {
                if (e.getCurrentItem().getType() == disc) {
                    HumanEntity entity = e.getWhoClicked();

                    if (!(entity instanceof Player)) return;

                    List<String> metadataDiscs;
                    if (entity.hasMetadata("hccore_discs")) {
                        metadataDiscs = (List<String>)entity.getMetadata("hccore_discs").get(0).value();
                    } else {
                        metadataDiscs = new ArrayList<>();
                    }

                    if (metadataDiscs.contains(disc.toString())) return;
                    metadataDiscs.add(disc.toString());

                    entity.setMetadata("hccore_discs",
                           new FixedMetadataValue(plugin, metadataDiscs));

                    incrementProgression(entity.getUniqueId());
                }
            });

            registerEvent(EntityPickupItemEvent.class, e -> {
                if (e.getItem().getItemStack().getType() == disc) {
                    LivingEntity entity = e.getEntity();

                    if (!(entity instanceof Player)) return;

                    List<String> metadataDiscs;
                    if (entity.hasMetadata("hccore_discs")) {
                        metadataDiscs = (List<String>)entity.getMetadata("hccore_discs").get(0).value();
                    } else {
                        metadataDiscs = new ArrayList<>();
                    }

                    if (metadataDiscs.contains(disc.toString())) return;
                    metadataDiscs.add(disc.toString());

                    entity.setMetadata("hccore_discs",
                            new FixedMetadataValue(plugin, metadataDiscs));

                    incrementProgression(e.getEntity().getUniqueId());
                }
            });
        }
    }
}
