package dev.flatgen.options.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import dev.flatgen.pictures.rules.Picture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads a world option's file. Only the icon and the effects are required; the name and description default to the
 * keys {@code option.<namespace>.<name>} and {@code option.<namespace>.<name>.description}, the group to the
 * namespace, and the group's picture to none of its own. An option with a part it cannot read is left out whole, never
 * half applied.
 */
public final class OptionReader {
    /** The version of the format this Flatgen reads; a file of a later one is left out. */
    private static final int FORMAT = 1;

    private OptionReader() {
    }

    /**
     * An option file read.
     *
     * @param option   the option; none when it is left out
     * @param problems what is wrong with the file
     */
    public record Reading(Optional<WorldOption> option, List<String> problems) {
        public Reading {
            problems = List.copyOf(problems);
        }
    }

    /** Reads the file of the option with this id, {@code <namespace>:<name>}. */
    public static Reading read(String id, String json) {
        List<String> problems = new ArrayList<>();
        JsonObject root;
        try {
            root = JsonFields.parseObject(json);
        } catch (IllegalArgumentException exception) {
            return new Reading(Optional.empty(), List.of("the file is " + exception.getMessage()));
        }
        int format = JsonFields.integer(root, "format", FORMAT, problems);
        if (format > FORMAT) {
            problems.add("it is format " + format + ", made for a later Flatgen than this one, which reads " + FORMAT);
        }
        Optional<Picture> icon = icon(root.get("icon"));
        if (icon.isEmpty()) {
            problems.add("it needs an icon: an item id, or {\"texture\": \"<namespace>:<path>.png\"}");
        }
        List<Effect> effects = effects(root, problems);
        String key = "option." + id.replace(':', '.');
        String name = JsonFields.text(root, "name", key, problems);
        String description = JsonFields.text(root, "description", key + ".description", problems);
        String group = JsonFields.text(root, "group", id.substring(0, Math.max(0, id.indexOf(':'))), problems);
        Optional<Picture> groupIcon = icon(root.get("group_icon"));
        if (root.has("group_icon") && groupIcon.isEmpty()) {
            problems.add("its group_icon must be an item id, or {\"texture\": \"<namespace>:<path>.png\"}");
        }
        boolean defaultOn = JsonFields.bool(root, "default", false, problems);
        boolean inverted = JsonFields.bool(root, "inverted", false, problems);
        List<String> requires = JsonFields.texts(root, "requires", problems);
        if (!problems.isEmpty()) {
            return new Reading(Optional.empty(), problems);
        }
        return new Reading(Optional.of(new WorldOption(id, name, description, icon.get(), group, groupIcon, defaultOn,
                inverted, requires, effects)), problems);
    }

    private static Optional<Picture> icon(JsonElement icon) {
        if (icon != null && JsonFields.isText(icon)) {
            return Optional.of(Picture.item(icon.getAsString()));
        }
        boolean isTexture = icon != null && icon.isJsonObject() && icon.getAsJsonObject().has("texture")
                && JsonFields.isText(icon.getAsJsonObject().get("texture"));
        return isTexture ? Optional.of(Picture.texture(icon.getAsJsonObject().get("texture").getAsString()))
                : Optional.empty();
    }

    /** Each effect of the file, noting every one that cannot be read and a file with none. */
    private static List<Effect> effects(JsonObject root, List<String> problems) {
        List<Effect> effects = new ArrayList<>();
        for (JsonElement element : JsonFields.array(root, "effects", problems).map(JsonArray::asList)
                .orElse(List.of())) {
            if (element.isJsonObject()) {
                EffectKinds.read(element.getAsJsonObject(), problems).ifPresent(effects::add);
            } else {
                problems.add("each effect should be an object with a type, so " + element + " cannot be read");
            }
        }
        if (effects.isEmpty() && problems.isEmpty()) {
            problems.add("it needs a list of effects, at least one");
        }
        return effects;
    }
}
