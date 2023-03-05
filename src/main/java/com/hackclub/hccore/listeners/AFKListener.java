package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.events.player.PlayerAFKStatusChangeEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AFKListener implements Listener {

  private final Title afkTitle;

  private final HCCorePlugin plugin;

  public AFKListener(HCCorePlugin plugin) {
    this.plugin = plugin;

    this.afkTitle = Title.title(Component.text("You are AFK").color(
            NamedTextColor.RED).decorate(TextDecoration.BOLD),
        Component.text("Run /afk to mark yourself as active"), Title.Times.times(
            Duration.of(500, ChronoUnit.MILLIS), Duration.of(12, ChronoUnit.HOURS),
            Duration.of(1, ChronoUnit.SECONDS)));
  }

  @EventHandler
  public void onPlayerAFKStatusChange(final PlayerAFKStatusChangeEvent event) {
    Player player = event.getPlayer();
    PlayerData data = this.plugin.getDataManager().getData(player);

    if (event.getNewValue()) {
      player.showTitle(afkTitle);
      player.getServer().broadcast(Component.text(data.getUsableName() + " is now AFK"));
    } else {
      player.clearTitle();
      player.getServer().broadcast(Component.text(data.getUsableName() + " is now Active"));
    }
  }

  @EventHandler
  public void onAsyncPlayerChat(final AsyncChatEvent event) {
    this.plugin.getDataManager().getData(event.getPlayer())
        .setLastActiveAt(System.currentTimeMillis());
  }

  @EventHandler
  public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
    this.plugin.getDataManager().getData(event.getPlayer())
        .setLastActiveAt(System.currentTimeMillis());
  }

  @EventHandler
  public void onPlayerInteract(final PlayerInteractEvent event) {
    this.plugin.getDataManager().getData(event.getPlayer())
        .setLastActiveAt(System.currentTimeMillis());
  }

  @EventHandler
  public void onPlayerMove(final PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();

    // Make sure the player didn't just turn their head
    if (from.toVector().equals(to.toVector())) { // NOTE: Might be inefficient?
      return;
    }

    this.plugin.getDataManager().getData(event.getPlayer())
        .setLastActiveAt(System.currentTimeMillis());
  }
}
