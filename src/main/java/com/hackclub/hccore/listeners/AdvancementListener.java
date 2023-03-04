package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {

  private final HCCorePlugin plugin;

  public AdvancementListener(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onBlockBreak(final BlockBreakEvent event) {
    // Check if it's a diamond ore
    Material blockType = event.getBlock().getType();
    if (blockType != Material.DIAMOND_ORE && blockType != Material.DEEPSLATE_DIAMOND_ORE) {
      return;
    }

    // The diamond ore wasn't found underground
    final int MAX_Y_DIAMOND_ORE = 16;
    if (event.getBlock().getY() > MAX_Y_DIAMOND_ORE) {
      return;
    }

    Player player = event.getPlayer();
    NamespacedKey key = new NamespacedKey(this.plugin, "mine_diamond_ore");
    AdvancementProgress progress =
        player.getAdvancementProgress(player.getServer().getAdvancement(key));
    // Skip if player already has this advancement
    if (progress.isDone()) {
      return;
    }

    this.grantAdvancement(player, key);
  }

  @EventHandler
  public void onEntityDeath(final EntityDeathEvent event) {
    // Ignore non-player killers
    if (!(event.getEntity().getKiller() instanceof Player)) {
      return;
    }

    Player player = event.getEntity().getKiller();
    switch (event.getEntityType()) {
      case ELDER_GUARDIAN: {
        this.grantAdvancement(player,
            new NamespacedKey(this.plugin, "kill_elder_guardian"));
        break;
      }
      case ENDER_DRAGON: {
        this.incrementAdvancementProgress(player,
            new NamespacedKey(this.plugin, "kill_dragon_insane"));

        // Give "The Next Generation" since it's not easy to get it after the egg
        // is taken
        this.grantAdvancement(player, NamespacedKey.minecraft("end/dragon_egg"));
        break;
      }
      case WITHER: {
        this.incrementAdvancementProgress(player,
            new NamespacedKey(this.plugin, "kill_wither_insane"));
        break;
      }
      default:
        break;
    }
  }

  @EventHandler
  public void onEntityToggleGlide(final EntityToggleGlideEvent event) {
    // Ignore takeoff events
    if (event.isGliding()) {
      return;
    }

    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    // Check the player has flown over 1m miles (1,609,344 km)import org.bukkit.craftbukkit.v1_17_R1.advancement.CraftAdvancement;

    final int CM_PER_MILE = 160934;
    if (player.getStatistic(Statistic.AVIATE_ONE_CM) <= (1000000 * CM_PER_MILE)) {
      return;
    }

    NamespacedKey key = new NamespacedKey(this.plugin, "million_miler");
    AdvancementProgress progress =
        player.getAdvancementProgress(player.getServer().getAdvancement(key));
    // Skip if player already has this advancement
    if (progress.isDone()) {
      return;
    }

    this.grantAdvancement(player, key);
  }

  @EventHandler
  public void onPlayerAdvancementDone(final PlayerAdvancementDoneEvent event) {
    Player player = event.getPlayer();
    Advancement advancement = event.getAdvancement();

    if (advancement.getKey().equals(new NamespacedKey(this.plugin, "mine_diamond_ore"))) {
      player.sendMessage(ChatColor.GREEN
          + "Congrats, you’ve found your very first diamond! You are now eligible for the exclusive (and limited edition!) Hack Club Minecraft stickers. Head over to "
          + ChatColor.UNDERLINE + this.plugin.getConfig().getString("claim-stickers-url")
          + ChatColor.RESET + ChatColor.GREEN + " to claim them!*");
      player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString()
          + "*This offer only applies to players who have never received the stickers before. If you have, please do not fill out the form again!");
      player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString()
          + "You will only see this message once.");
    }

    AdvancementDisplay display = advancement.getDisplay();

    // Ignore hidden advancements (i.e. recipes)
    if (display == null) {
      return;
    }
    if (!display.doesAnnounceToChat()) {
      return;
    }

    Component titleComponent = Component.text("[")
        .append(display.title())
        .append(Component.text("]"));
    Component descriptionComponent = display.title()
        .append(Component.text("\n"))
        .append(display.description());
    Component frameSpecificComponent;
    TextColor frameSpecificColor;

    switch (display.frame()) {
      case TASK:
      default:
        frameSpecificComponent = Component.text(" has made the advancement ");
        frameSpecificColor = NamedTextColor.GREEN;
        break;
      case GOAL:
        frameSpecificComponent = Component.text(" has reached the goal ");
        frameSpecificColor = NamedTextColor.GREEN;
        break;
      case CHALLENGE:
        frameSpecificComponent = Component.text(" has completed the challenge ");
        frameSpecificColor = NamedTextColor.DARK_PURPLE;
        break;
    }

    player.getServer().broadcast(player.displayName()
        .color(NamedTextColor.WHITE)
        .append(frameSpecificComponent)
        .append(titleComponent
            .color(frameSpecificColor)
            .hoverEvent(descriptionComponent
                .color(frameSpecificColor))));
  }

  private void grantAdvancement(Player player, NamespacedKey key) {
    AdvancementProgress progress =
        player.getAdvancementProgress(player.getServer().getAdvancement(key));
    if (progress.isDone()) {
      return;
    }

    for (String criteria : progress.getRemainingCriteria()) {
      progress.awardCriteria(criteria);
    }
  }

  private void incrementAdvancementProgress(Player player, NamespacedKey key) {
    AdvancementProgress progress =
        player.getAdvancementProgress(player.getServer().getAdvancement(key));
    if (progress.isDone()) {
      return;
    }

    String nextCriteria = String.valueOf(progress.getAwardedCriteria().size());
    progress.awardCriteria(nextCriteria);
  }
}
