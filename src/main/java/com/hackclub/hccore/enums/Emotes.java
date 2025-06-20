package com.hackclub.hccore.enums;

import lombok.Getter;

public enum Emotes {
  DOWNVOTE("↓"),
  SHRUG("¯\\_(ツ)_/¯"),
  TABLEFLIP("(╯°□°）╯︵ ┻━┻"),
  UPVOTE("↑"),
  ANGRY("ಠ_ಠ")
  ;

  @Getter
  private final String emote;

  Emotes(String emote) {
    this.emote = emote;
  }

  public static String parseString(String text) {
    for (Emotes emote : Emotes.values()) {
      String pattern = "(?i):" + emote.name() + ":";
      text = text.replaceAll(pattern, emote.getEmote());
    }
    return text;
  }


}
