package dev.flatgen.config.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;

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
        JsonObject root = JsonFields.parseObject(json);
        return new Settings(JsonFields.bool(root, ENABLED, DEFAULTS.enabled(), new ArrayList<>()));
    }

    /** The settings as JSON, as the file stores them. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(ENABLED, enabled);
        return GSON.toJson(root);
    }
}
