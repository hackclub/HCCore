package com.hackclub.hccore.tasks;

import com.hackclub.hccore.HCCorePlugin;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;

public class IconTask implements Runnable {
  private final HCCorePlugin plugin;
  public IconTask(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    Logger logger = plugin.getLogger();

    HttpClient client = HttpClient.newHttpClient();
    // get the icon url from Shrimp Shuffler
    HttpRequest ssRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://shrimp-shuffler.a.hackclub.dev/api/current"))
        .GET()
        .timeout(Duration.ofSeconds(20))
        .build();
    HttpResponse<String> ssResponse;
    try {
      ssResponse = client.send(ssRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.severe("Failed to get the current icon from Shrimp Shuffler!");
      logger.severe(e.getMessage());
      return;
    } catch (IOException e) {
      logger.severe("Failed to get the current icon from Shrimp Shuffler!");
      logger.severe(e.getMessage());
      return;
    }
    if (ssResponse == null || ssResponse.body() == null) {
      logger.severe("Failed to get the current icon from Shrimp Shuffler!");
      return;
    }
    // load that icon and save to a temp file
    HttpRequest cdnRequest = HttpRequest.newBuilder()
        .uri(URI.create(ssResponse.body()))
        .GET()
        .timeout(Duration.ofSeconds(20))
        .build();
    HttpResponse<byte[]> cdnResponse;
    try {
      cdnResponse = client.send(cdnRequest, HttpResponse.BodyHandlers.ofByteArray());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.severe("Failed to get the current icon from the Hack Club CDN!");
      logger.severe(e.getMessage());
      return;
    } catch (IOException e) {
      logger.severe("Failed to get the current icon from the Hack Club CDN!");
      logger.severe(e.getMessage());
      return;
    }
    // load as a buffered image and cache in plugin
    BufferedImage image;
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(cdnResponse.body());
      image = ImageIO.read(in);
    } catch (IOException e) {
      logger.severe("Failed to load the current icon from the file!");
      logger.severe(e.getMessage());
      return;
    }
    if (image == null) {
      logger.severe("Failed to load the current icon from the file!");
      return;
    }
    BufferedImage serverIcon = resizeToIcon(image);
    Bukkit.getScheduler().runTask(plugin, () -> {
      try {
        plugin.serverIcon = Bukkit.loadServerIcon(serverIcon);
      } catch (Exception e) {
        logger.severe("Failed to load the current icon from the file!");
        logger.severe(e.getMessage());
      }
    });
  }
  // little resizing method to make it work with minecraft's server list
  private static BufferedImage resizeToIcon(BufferedImage original) {
    BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = resizedImage.createGraphics();
    try {
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.drawImage(original, 0, 0, 64, 64, null);
    } finally {
      graphics.dispose();
    }
    return resizedImage;
  }
}