package com.hackclub.hccore.tasks;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.playermessages.afk.AutoAFKMessage;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoAFKTask extends BukkitRunnable {

  private final HCCorePlugin plugin;

  public AutoAFKTask(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  public void run() {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      PlayerData data = this.plugin.getDataManager().getData(player);

      // Skip players who are already AFK
      if (data.isAfk()) {
        continue;
      }

      // Skip vanished players (no one can see them anyway, not needed to notify afk)
      if (plugin.vanishPluginPresent) {
        if (VanishAPI.isInvisible(player)) {
          continue;
        }
      }

      long secondsSinceLastActive =
          (System.currentTimeMillis() - data.getLastActiveAt()) / 1000;
      if (secondsSinceLastActive > this.plugin.getConfig().getInt("settings.auto-afk-time")) {
        data.setAfk(true);
        player.sendMessage(AutoAFKMessage.get());
      }
    }
  }
}
