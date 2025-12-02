package com.hackclub.hccore.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerListPingListener implements Listener {
  private final HCCorePlugin plugin;
  public ServerListPingListener(HCCorePlugin plugin) {
    this.plugin = plugin;
  }
  @EventHandler
  public void serverListPing(PaperServerListPingEvent e) {
    if (plugin.serverIcon != null) {
      e.setServerIcon(plugin.serverIcon);
    }
  }
}
