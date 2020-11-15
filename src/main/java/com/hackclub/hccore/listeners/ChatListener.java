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

        GrpcClient client = new GrpcClient(channel);
        this.grpcClient = client;
    }

    @EventHandler
    public void onChatChange(final AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        String player = event.getPlayer().getDisplayName();

        this.grpcClient.greet(player + ": '" + msg + "'");

        this.plugin.getLogger().info(player + ": '" + msg + "'");
    }
}
