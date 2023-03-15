package com.hackclub.hccore.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BeehiveInteractionListener implements Listener {

  public BeehiveInteractionListener() {
  }

  @EventHandler
  public void onPlayerInteract(final PlayerInteractEvent event) {
    // Check if this was a right hand interaction with a block while not holding
    // anything
    if (!(event.getHand() == EquipmentSlot.HAND && event.getAction() == Action.RIGHT_CLICK_BLOCK
        && event.getPlayer().getInventory().getItemInMainHand()
        .getType() == Material.AIR)) {
      return;
    }

    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) {
      return;
    }
    if (clickedBlock.getType() != Material.BEEHIVE) {
      return;
    }

    int beeCount = ((org.bukkit.block.Beehive) clickedBlock.getState()).getEntityCount();
    int honeyLevel = ((Beehive) clickedBlock.getBlockData()).getHoneyLevel();

    Component message = Component.empty().decorate(TextDecoration.ITALIC).color(NamedTextColor.GOLD)
        .append(Component.text("There").appendSpace()
            .append(Component.text((beeCount == 1) ? "is" : "are")).appendSpace()
            .append(
                Component.text(beeCount).color(NamedTextColor.WHITE))
            .appendSpace()
            .append(Component.text((beeCount == 1) ? "bee" : "bees")).appendSpace()
            .append(Component.text("hiding inside,")).appendNewline()
            .append(Component.text("guarding")).appendSpace().append(
                Component.text(honeyLevel).color(NamedTextColor.WHITE))
            .appendSpace().append(Component.text((honeyLevel == 1) ? "level" : "levels"))
            .appendSpace()
            .append(Component.text("of honey.")));
    /*
    there are/is 0/1/2/3 bee[s] hiding inside
    guarding 0/1/2/3/4/5 level[s] of honey
     */
    event.getPlayer().sendMessage(message);
  }
}
