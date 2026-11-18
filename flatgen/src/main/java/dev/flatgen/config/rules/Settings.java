package dev.flatgen.config.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The player's settings for Flatgen, as config/flatgen/flatgen.json holds them.
 *
 * @param enabled whether Customize opens Flatgen; off gives the game's own superflat screen back
 */
public record Settings(boolean enabled) {
    /** The settings of a player who has changed nothing. */
    public static final Settings DEFAULTS = new Settings(true);

    private static final String ENABLED = "enabled";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Reads settings from JSON. A missing key, or one of the wrong type, keeps its default.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static Settings fromJson(String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("not JSON", exception);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("not a JSON object");
        }
        return new Settings(readBoolean(root.getAsJsonObject(), ENABLED, DEFAULTS.enabled()));
    }

    /** The settings as JSON, as the file stores them. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(ENABLED, enabled);
        return GSON.toJson(root);
    }

    private static boolean readBoolean(JsonObject object, String key, boolean fallback) {
        JsonElement value = object.get(key);
        boolean isBoolean = value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
        return isBoolean ? value.getAsBoolean() : fallback;
    }
}
