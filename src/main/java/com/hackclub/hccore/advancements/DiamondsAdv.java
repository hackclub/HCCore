package com.hackclub.hccore.advancements;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.HCCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class DiamondsAdv extends BaseAdvancement {
    static AdvancementDisplayBuilder<AdvancementDisplay.Builder, AdvancementDisplay> displayBuilder = new AdvancementDisplay.Builder(Material.DIAMOND_ORE, "Look Ma, " +
            "Diamonds!")
            .taskFrame()
            .announceChat()
            .showToast()
            .description("Find your first diamond while mining");
    static int maxProgression = 1;

    HCCorePlugin plugin;

    public DiamondsAdv(HCCorePlugin plugin, Advancement root, AdvancementKey key, CoordAdapter adapter) {
        super(key.getKey(), displayBuilder.coords(adapter, key).build(), root, maxProgression);
        this.plugin = plugin;

        registerEvent(BlockBreakEvent.class, e -> {
           if (e.getBlock().getType() == Material.DIAMOND_ORE || e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
               incrementProgression(e.getPlayer());
           }
        });
    }

    @Override
    public void giveReward(@NotNull Player player) {
        Component congratsComponent = Component.text("Congrats, you’ve found your very first diamond! You are now eligible for the exclusive (and limited edition!) Hack Club Minecraft stickers. Head over to ")
                .color(NamedTextColor.GREEN);
        Component linkComponent = Component.text(this.plugin.getConfig().getString("claim-stickers-url"))
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.UNDERLINED)
                .hoverEvent(Component.text("Click here to claim your stickers!"))
                .clickEvent(ClickEvent.openUrl(this.plugin.getConfig().getString("claim-stickers-url")));
        Component claimComponent = Component.text(" to claim them!")
                .color(NamedTextColor.GREEN);
        Component italicComponent = Component.text("""

                        This offer only applies to players who have never received the stickers before. If you have, please do not fill out the form again!
                        You will only see this message once.""")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC);
        player.sendMessage(congratsComponent.append(linkComponent).append(claimComponent).append(italicComponent));
    }
}
