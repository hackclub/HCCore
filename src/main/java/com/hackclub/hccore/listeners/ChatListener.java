package com.hackclub.hccore.listeners;

import java.util.logging.Logger;
import com.hackclub.hccore.HCCorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.hackclub.hccore.GrpcClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ChatListener implements Listener {
    private final HCCorePlugin plugin;
    private final GrpcClient grpcClient;

    public ChatListener(HCCorePlugin plugin) {
        this.plugin = plugin;

        String target = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // disable TLS to avoid needing certificates.
            .usePlaintext()
            .build();

        this.grpcClient = new GrpcClient(channel);
    }

    @EventHandler
    public void onChatChange(final AsyncPlayerChatEvent event) {
        String player = event.getPlayer().getDisplayName();
        String text = event.getMessage();

        // this.grpcClient.greet(player + ": '" + msg + "'");

        this.plugin.getLogger().info(player + ": '" + text + "'");

        this.grpcClient.sendToSlackWrapper(player, text);
    }
}
