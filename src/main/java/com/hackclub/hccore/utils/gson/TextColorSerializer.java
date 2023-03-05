package com.hackclub.hccore.utils.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.kyori.adventure.text.format.TextColor;

public class TextColorSerializer implements JsonSerializer<TextColor> {

  @Override
  public JsonElement serialize(TextColor source, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(source.asHexString());
  }
}
