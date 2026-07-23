package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VanishListener implements Listener {
  private final HCCorePlugin plugin;
  public VanishListener(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onVanish(PlayerHideEvent e) {
    if (plugin.getSlackBot() == null) {
      return;
    }
    plugin.getSlackBot().sendFakeMsg("leave", e.getPlayer().displayName());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onUnvanish(PlayerShowEvent e) {
    if (plugin.getSlackBot() == null) {
      return;
    }
    plugin.getSlackBot().sendFakeMsg("join", e.getPlayer().displayName());
  }
}
