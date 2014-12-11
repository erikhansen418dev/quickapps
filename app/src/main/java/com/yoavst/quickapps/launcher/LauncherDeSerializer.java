package com.yoavst.quickapps.launcher;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by Yoav.
 */
public class LauncherDeSerializer implements JsonDeserializer<CLauncherActivity.ListItem>, JsonSerializer<CLauncherActivity.ListItem> {
	@Override
	public CLauncherActivity.ListItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		String name = object.get("name").getAsString();
		String activity = object.has("activity-name") ? object.get("activity-name").getAsString() : "";
		boolean enabled = !object.has("enabled") || object.get("enabled").getAsBoolean();
		return new CLauncherActivity.ListItem(name,null,activity,enabled);
	}

    @Override
    public JsonElement serialize(CLauncherActivity.ListItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("name", src.name);
        object.addProperty("enabled", src.enabled);
        object.addProperty("activity-name", src.activity);
        return object;
    }
}
