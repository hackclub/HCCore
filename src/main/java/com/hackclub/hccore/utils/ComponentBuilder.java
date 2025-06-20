package com.hackclub.hccore.utils;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.enums.Color;
import com.hackclub.hccore.enums.Emotes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentBuilder {
  private final List<TextComponent> componentList = new ArrayList<>();

  private ComponentBuilder() {
  }

  public static ComponentBuilder newBuilder() {
    return new ComponentBuilder();
  }


  public TextComponent build() {
    return Component.textOfChildren(componentList.toArray(new ComponentLike[0]));
  }

  public void send(Audience audience) {
    audience.sendMessage(build());
  }

  public void broadcast() {
    HCCorePlugin.getInstance().getServer().broadcast(build());
  }





  public ComponentBuilder newLine() {
    componentList.add(Component.newline());
    return this;
  }

  public ComponentBuilder newLine(int amount) {
    for (int i = 0; i < amount; i++) {
      componentList.add(Component.newline());
    }
    return this;
  }

  public ComponentBuilder component(TextComponent component) {
    componentList.add(component);
    return this;
  }

  public ComponentBuilder component(Component component) {
    componentList.add(Component.textOfChildren(component));
    return this;
  }

  public static Component fromLegacy(String legacy) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
  }




  public ComponentBuilder custom(String text, Color color) {
    componentList.add(Component
        .text(Objects.requireNonNullElse(text, ""), color.getTextColor())
        .decoration(TextDecoration.ITALIC, false)
    );
    return this;
  }

  public ComponentBuilder custom(String text, java.awt.Color color) {
    componentList.add(Component
        .text(text, TextColor.color(color.getRed(), color.getGreen(), color.getBlue()))
        .decoration(TextDecoration.ITALIC, false)
    );
    return this;
  }

  public ComponentBuilder parsedMessage(String message) {
    String parsed = Emotes.parseString(message);
    custom(parsed, Color.WHITE);

    return this;
  }

  public ComponentBuilder gray(String text) {
    custom(text, Color.GRAY);
    return this;
  }

  public ComponentBuilder darkGray(String text) {
    custom(text, Color.DARK_GRAY);
    return this;
  }

  public ComponentBuilder black(String text) {
    custom(text, Color.BLACK);
    return this;
  }

  public ComponentBuilder darkBlue(String text) {
    custom(text, Color.DARK_BLUE);
    return this;
  }

  public ComponentBuilder darkGreen(String text) {
    custom(text, Color.DARK_GREEN);
    return this;
  }

  public ComponentBuilder darkAqua(String text) {
    custom(text, Color.DARK_AQUA);
    return this;
  }

  public ComponentBuilder darkRed(String text) {
    custom(text, Color.DARK_RED);
    return this;
  }

  public ComponentBuilder darkPurple(String text) {
    custom(text, Color.DARK_PURPLE);
    return this;
  }

  public ComponentBuilder gold(String text) {
    custom(text, Color.GOLD);
    return this;
  }

  public ComponentBuilder blue(String text) {
    custom(text, Color.BLUE);
    return this;
  }

  public ComponentBuilder green(String text) {
    custom(text, Color.GREEN);
    return this;
  }

  public ComponentBuilder aqua(String text) {
    custom(text, Color.AQUA);
    return this;
  }

  public ComponentBuilder red(String text) {
    custom(text, Color.RED);
    return this;
  }

  public ComponentBuilder lightPurple(String text) {
    custom(text, Color.LIGHT_PURPLE);
    return this;
  }

  public ComponentBuilder yellow(String text) {
    custom(text, Color.YELLOW);
    return this;
  }

  public ComponentBuilder white(String text) {
    custom(text, Color.WHITE);
    return this;
  }


}
