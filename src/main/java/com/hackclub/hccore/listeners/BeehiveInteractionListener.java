package com.hackclub.hccore.listeners;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

import net.kyori.adventure.text.Component;
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
        && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)) {
      return;
    }

    // Check if the block is a beehive (or bee nest, which implements beehive)
    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) {
      return;
    }
    if (!(clickedBlock.getState() instanceof org.bukkit.block.Beehive beehiveState)) {
      return;
    }
    Beehive beehiveData = (Beehive) clickedBlock.getBlockData();

    int beeCount = beehiveState.getEntityCount();
    int honeyLevel = beehiveData.getHoneyLevel();

    Component msg = text("Bees:").color(AQUA);

    for (int i = 0; i < beehiveState.getMaxEntities(); i++) {
      Component bee = space().append(text("☻"));
      Component beeHoverText = text("Bee").color(WHITE).appendNewline();
      if (i >= beeCount) {
        bee = bee.color(GRAY);
        beeHoverText = beeHoverText.append(text("Not Home :(").color(RED));
      } else {
        bee = bee.color(YELLOW);
        beeHoverText = beeHoverText.append(text("Buzzing Around").color(GREEN));
      }
      bee = bee.hoverEvent(beeHoverText);
      msg = msg.append(bee);
    }
    msg = msg.appendNewline().append(text("Honey:").color(AQUA)).appendSpace();

    Component honeyBlock = empty();
    for (int i = 0; i < beehiveData.getMaximumHoneyLevel(); i++) {
      Component honey = text("█");
      if (i >= honeyLevel) {
        honey = honey.color(GRAY);
      } else {
        honey = honey.color(GOLD);
      }
      honeyBlock = honeyBlock.append(honey);
    }
    Component honeyHoverText = text(
        "Honey %d/%d".formatted(honeyLevel, beehiveData.getMaximumHoneyLevel())).color(WHITE)
        .appendNewline();
    if (honeyLevel == beehiveData.getMaximumHoneyLevel()) {
      honeyHoverText = honeyHoverText.append(text("Ready to Harvest!").color(GREEN));
    } else {
      honeyHoverText = honeyHoverText.append(text("Not Ready Yet...").color(RED));
    }
    honeyBlock = honeyBlock.hoverEvent(honeyHoverText);
    msg = msg.append(honeyBlock);

    event.getPlayer().sendMessage(msg);
  }
}
