package com.hackclub.hccore.tasks;

import com.hackclub.hccore.HCCorePlugin;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class IconTask implements Runnable {
  private final HCCorePlugin plugin;
  public IconTask(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    HttpClient client = HttpClient.newHttpClient();
    // get the icon url from Shrimp Shuffler
    HttpRequest ssRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://shrimp-shuffler.a.hackclub.dev/api/current"))
        .GET()
        .build();
    HttpResponse<String> ssResponse;
    try {
      ssResponse = client.send(ssRequest, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      plugin.getLogger().severe("Failed to get the current icon from Shrimp Shuffler!");
      plugin.getLogger().severe(e.getMessage());
      return;
    }
    if (ssResponse == null) {
      plugin.getLogger().severe("Failed to get the current icon from Shrimp Shuffler!");
      return;
    }
    // load that icon and save to a temp file
    HttpRequest cdnRequest = HttpRequest.newBuilder()
        .uri(URI.create(ssResponse.body()))
        .GET()
        .build();
    HttpResponse<Path> cdnResponse;
    try {
      cdnResponse = client.send(cdnRequest,
          HttpResponse.BodyHandlers.ofFile(Paths.get(plugin.getDataPath().toString(), "temp.png")));
    } catch (IOException | InterruptedException | SecurityException e) {
      plugin.getLogger().severe("Failed to get the current icon from the Hack Club CDN!");
      plugin.getLogger().severe(e.getMessage());
      return;
    }
    // load as a buffered image and cache in plugin
    BufferedImage image;
    try {
      File imageFile = new File(cdnResponse.body().toUri());
      image = ImageIO.read(imageFile);
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to load the current icon from the file!");
      plugin.getLogger().severe(e.getMessage());
      return;
    }
    if (image == null) {
      plugin.getLogger().severe("Failed to load the current icon from the file!");
      return;
    }
    try {
      plugin.serverIcon = plugin.getServer().loadServerIcon(resizeToIcon(image));
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to load the current icon from the file!");
      plugin.getLogger().severe(e.getMessage());
    }
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