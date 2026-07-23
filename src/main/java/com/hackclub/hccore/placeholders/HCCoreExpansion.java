package com.hackclub.hccore.placeholders;

import com.hackclub.hccore.HCCorePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class HCCoreExpansion extends PlaceholderExpansion {
  private HCCorePlugin plugin;
  public HCCoreExpansion(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "hccore";
  }

  @Override
  public @NotNull String getAuthor() {
    return "Hack Club";
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getPluginMeta().getVersion();
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    if (params.equalsIgnoreCase("displayname")) {
      LegacyComponentSerializer hexSerializer = LegacyComponentSerializer.builder()
          .character(LegacyComponentSerializer.SECTION_CHAR)
          .hexColors()
          .build();

      return hexSerializer.serialize(plugin.getDataManager().getData(player).getDisplayedName());
    }
    return null;
  }
}
