package com.hackclub.hccore.playerMessages.tags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class ConditionalResolver {

  public static @NotNull TagResolver conditionalTag(String tagname, Boolean enabled) {
    return TagResolver.resolver(tagname, (argumentQueue, context) -> {
      if (enabled) { // should return original (inner) tags
        return Tag.inserting(Component.empty()); // includes children tags, essentially no-op
      } else {
        return emptyTag();
      }
    });
  }

  public static Modifying emptyTag() {
    return (current, depth) -> Component.empty();
  }
}
