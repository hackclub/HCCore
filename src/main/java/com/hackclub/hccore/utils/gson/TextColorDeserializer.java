package com.hackclub.hccore.utils.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;
import net.kyori.adventure.text.format.TextColor;

public class TextColorDeserializer implements JsonDeserializer<TextColor> {

  @Override
  public TextColor deserialize(JsonElement source, Type type, JsonDeserializationContext context) {
    return TextColor.fromHexString(source.getAsString());
  }
}
