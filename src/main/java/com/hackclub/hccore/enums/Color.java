package com.hackclub.hccore.enums;

import net.kyori.adventure.text.format.TextColor;

public enum Color {
  /*

    DEFAULT MINECRAFT COLORS

   */

  GRAY(170, 170, 170),
  DARK_GRAY(85, 85, 85),
  BLACK(0, 0, 0),
  DARK_BLUE(0, 0, 170),
  DARK_GREEN(0, 170, 0),
  DARK_AQUA(0, 170, 170),
  DARK_RED(170, 0, 0),
  DARK_PURPLE(170, 0, 170),
  GOLD(255, 170, 0),
  BLUE(85, 85, 255),
  GREEN(85, 255, 85),
  AQUA(85, 255, 255),
  RED(255, 85, 85),
  LIGHT_PURPLE(255, 85, 255),
  YELLOW(255, 255, 85),
  WHITE(255, 255, 255),

  ;

  private int R;
  private int G;
  private int B;

  Color(int r, int g, int b) {
    R = r;
    G = g;
    B = b;
  }

  public TextColor getTextColor() {
    return TextColor.color(R, G, B);
  }

  public int getRed() {
    return R;
  }

  public int getGreen() {
    return G;
  }

  public int getBlue() {
    return B;
  }


  public static java.awt.Color fromTextColor(TextColor textColor) {
    int value = textColor.value();
    int r = (value >> 16) & 0xFF;
    int g = (value >> 8) & 0xFF;
    int b = value & 0xFF;
    return new java.awt.Color(r, g, b);
  }
}
