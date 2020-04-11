package com.hackclub.hccore.listeners;

import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.Statistic;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.v1_15_R1.advancement.CraftAdvancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.AdvancementDisplay;
import net.minecraft.server.v1_15_R1.AdvancementFrameType;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;

public class AdvancementListener implements Listener {
    private final HCCorePlugin plugin;

    public AdvancementListener(HCCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        // Check if it's a diamond ore
        if (event.getBlock().getType() != Material.DIAMOND_ORE) {
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
    public void onPlayerStatisticIncrement(final PlayerStatisticIncrementEvent event) {
        // Check if statistic is about meters flown
        if (event.getStatistic() != Statistic.FLY_ONE_CM) {
            return;
        }

        // Check that the value is equal or above that of 1 million miles (1,609,344 km)
        final int MILLION_AMNT = 1609344000;
        if (event.getNewValue() <= MILLION_AMNT) {
            return;
        }

        Player player = event.getPlayer();
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
    public void onEntityDeath(final EntityDeathEvent event) {
        // Ignore non-player killers
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity().getKiller();
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
        
            

        try {
            // NOTE: We interface with Minecraft's internal code here. It is unlikely, but possible
            // for it to break in the case of a future upgrade.
            net.minecraft.server.v1_15_R1.Advancement nmsAdvancement =
                    ((CraftAdvancement) advancement).getHandle();
            AdvancementDisplay display = nmsAdvancement.c();

            // Ignore hidden advancements (i.e. recipes)
            if (display == null) {
                return;
            }

            boolean announceToChat = display.i();
            if (!announceToChat) {
                return;
            }

            // Get frame type-specific wording + formatting
            IChatBaseComponent titleComponent = display.a();
            IChatBaseComponent descriptionComponent = display.b();
            AdvancementFrameType frameType = display.e();
            Object[] args = null; // This is bad practice
            switch (frameType.a()) {
                case "task":
                default:
                    args = new Object[] {"made", "advancement", ChatColor.GREEN};
                    break;
                case "goal":
                    args = new Object[] {"reached", "goal", ChatColor.GREEN};
                    break;
                case "challenge":
                    args = new Object[] {"completed", "challenge", ChatColor.DARK_PURPLE};
                    break;
            }

            // Announce custom advancement message
            BaseComponent nameComponent =
                    new TextComponent(ChatColor.stripColor(player.getDisplayName()));
            nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(player.getName()).create()));
            BaseComponent advancementComponent = new TextComponent(titleComponent.getText());
            advancementComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder().color(((ChatColor) args[2]).asBungee())
                            .append(titleComponent.getText() + "\n")
                            .append(descriptionComponent.getText()).create()));

            BaseComponent[] message = new ComponentBuilder(nameComponent)
                    .append(String.format(" has %s the %s %s[", args),
                            ComponentBuilder.FormatRetention.NONE)
                    .append(advancementComponent)
                    .append("]", ComponentBuilder.FormatRetention.FORMATTING).create();
            player.getServer().spigot().broadcast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
