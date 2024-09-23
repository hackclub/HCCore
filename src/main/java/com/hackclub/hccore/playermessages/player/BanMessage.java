package com.hackclub.hccore.playermessages.player;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import com.hackclub.hccore.HCCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.BanEntry;
import org.bukkit.BanList.Type;

public class BanMessage {

  final static String minimsgSource = """
      <b><red>You've been banned for<reason> :(</red></b>
            
      If you would like to appeal, please DM a <aqua>Minecraft server admin (@minecraft-admins user group)</aqua> on Slack.""";

  public static Component get(String uuid) {
    HCCorePlugin plugin = HCCorePlugin.getInstance();
    BanEntry banEntry = plugin.getServer().getBanList(Type.NAME)
        .getBanEntry(uuid);

    String reason;
    if (banEntry != null) {
      reason = banEntry.getReason();
      if (reason != null) {
        if (!reason.equals("Banned by an operator.")) {
          reason = ": " + reason;
        } else {
          reason = " no specified reason";
        }
      } else {
        reason = " no specified reason";
      }
    } else {
      reason = " no specified reason";
    }

    return miniMessage().deserialize(minimsgSource,
        TagResolver.resolver(Placeholder.component("reason", text(reason))));
  }
}
