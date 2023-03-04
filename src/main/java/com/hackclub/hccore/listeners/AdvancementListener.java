package com.hackclub.hccore.listeners;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class AdvancementListener implements Listener {

    public AdvancementListener() {
    }

    @EventHandler
    public void onPlayerAdvancementDone(final PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        AdvancementDisplay display = advancement.getDisplay();

        // Ignore hidden advancements (i.e. recipes)
        if (display == null) return;
        if (!display.doesAnnounceToChat()) return;

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
}
