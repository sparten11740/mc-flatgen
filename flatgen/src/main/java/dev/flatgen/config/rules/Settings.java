package dev.flatgen.config.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;

/**
 * The player's settings for Flatgen, as config/flatgen/flatgen.json holds them.
 *
 * @param enabled  whether Customize opens Flatgen; off gives the game's own superflat screen back
 * @param advanced whether the editor shows everything, for adults who build flat worlds, rather than the simple view a
 *                 child can work out; the editor's Advanced switch turns it over
 */
public record Settings(boolean enabled, boolean advanced) {
    /** The settings of a player who has changed nothing. */
    public static final Settings DEFAULTS = new Settings(true, false);

    private static final String ENABLED = "enabled";
    private static final String ADVANCED = "advanced";

    /**
     * Reads settings from JSON. A missing key, or one of the wrong type, keeps its default.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static Settings fromJson(String json) {
        JsonObject root = JsonFields.parseObject(json);
        return new Settings(JsonFields.bool(root, ENABLED, DEFAULTS.enabled(), new ArrayList<>()),
                JsonFields.bool(root, ADVANCED, DEFAULTS.advanced(), new ArrayList<>()));
    }

    /** These settings with the editor's Advanced on or off. */
    public Settings withAdvanced(boolean on) {
        return new Settings(enabled, on);
    }

    /** The settings as JSON, as the file stores them. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(ENABLED, enabled);
        root.addProperty(ADVANCED, advanced);
        return JsonFields.PRETTY.toJson(root);
    }
}
