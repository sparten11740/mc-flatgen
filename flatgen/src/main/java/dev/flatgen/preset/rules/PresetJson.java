package dev.flatgen.preset.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Flatgen's preset files. Every release reads what any earlier one wrote: a missing key gets its default, a key of the
 * wrong kind is noted and gets its default, and keys a later release added are left alone.
 */
public final class PresetJson {
    /** The format this release writes; a file of a later format is still read, as far as it goes. */
    public static final int FORMAT = 1;
    /** The icon of a preset that names none, nor any layer. */
    static final String DEFAULT_ICON = "minecraft:grass_block";

    private static final String FORMAT_KEY = "format";
    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String LAYERS = "layers";
    private static final String BIOME = "biome";
    private static final String STRUCTURES = "structures";
    private static final String DECORATIONS = "decorations";
    private static final String LAKES = "lakes";
    private static final String OPTIONS = "options";

    private PresetJson() {
    }

    /**
     * Reads a preset file.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static PresetReading read(String json) {
        JsonObject root = JsonFields.parseObject(json);
        List<String> problems = new ArrayList<>();
        int format = JsonFields.integer(root, FORMAT_KEY, FORMAT, problems);
        if (format > FORMAT) {
            problems.add("written by a later Flatgen (format " + format + "), so what is new in it was left out");
        }
        List<Layer> layers = readLayers(root, "the world", problems);
        Preset preset = new Preset(
                JsonFields.text(root, NAME, "", problems),
                JsonFields.text(root, ICON, topBlock(layers), problems),
                layers,
                JsonFields.text(root, BIOME, Preset.DEFAULT_BIOME, problems),
                JsonFields.array(root, STRUCTURES, problems).map(sets -> JsonFields.texts(sets, STRUCTURES, problems)),
                JsonFields.bool(root, DECORATIONS, false, problems),
                JsonFields.bool(root, LAKES, false, problems),
                JsonFields.object(root, OPTIONS, problems).map(options -> switches(options, problems))
                        .orElse(Map.of()),
                otherStacks(root, problems));
        return new PresetReading(preset, problems);
    }

    /** A preset as its file holds it. */
    public static String write(Preset preset) {
        JsonObject root = new JsonObject();
        root.addProperty(FORMAT_KEY, FORMAT);
        root.addProperty(NAME, preset.name());
        root.addProperty(ICON, preset.icon());
        root.add(LAYERS, LayerJson.write(preset.layers()));
        root.addProperty(BIOME, preset.biome());
        preset.structureSets().ifPresent(sets -> root.add(STRUCTURES, JsonFields.textArray(sets)));
        root.addProperty(DECORATIONS, preset.decorations());
        root.addProperty(LAKES, preset.lakes());
        JsonObject options = new JsonObject();
        preset.options().forEach(options::addProperty);
        root.add(OPTIONS, options);
        preset.stacks().forEach((dimension, stack) -> root.add(dimension.key(), writeStack(stack)));
        return JsonFields.PRETTY.toJson(root);
    }

    /** The Nether's and the End's own stacks a file gives, by dimension; a file without them gives none. */
    private static Map<Dimension, Stack> otherStacks(JsonObject root, List<String> problems) {
        Map<Dimension, Stack> stacks = new EnumMap<>(Dimension.class);
        for (Dimension dimension : Dimension.OTHERS) {
            JsonFields.object(root, dimension.key(), problems)
                    .ifPresent(stack -> stacks.put(dimension, readStack(stack, dimension, problems)));
        }
        return stacks;
    }

    private static Stack readStack(JsonObject stack, Dimension dimension, List<String> problems) {
        return new Stack(readLayers(stack, "the " + dimension.key(), problems),
                JsonFields.text(stack, BIOME, dimension.defaultBiome(), problems));
    }

    private static JsonObject writeStack(Stack stack) {
        JsonObject written = new JsonObject();
        written.add(LAYERS, LayerJson.write(stack.layers()));
        written.addProperty(BIOME, stack.biome());
        return written;
    }

    /** The top layer's block, which stands for a preset that names no icon. */
    static String topBlock(List<Layer> layers) {
        return layers.isEmpty() ? DEFAULT_ICON : layers.get(layers.size() - 1).block();
    }

    /** A stack's layers, noting when there are none, which leaves {@code what}, as "the nether", empty. */
    private static List<Layer> readLayers(JsonObject root, String what, List<String> problems) {
        Optional<JsonArray> layers = JsonFields.array(root, LAYERS, problems);
        if (layers.isEmpty()) {
            problems.add("no layers, so " + what + " is empty");
        }
        return layers.map(array -> LayerJson.read(array, problems)).orElse(List.of());
    }

    private static Map<String, Boolean> switches(JsonObject options, List<String> problems) {
        Map<String, Boolean> switches = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> option : options.entrySet()) {
            if (JsonFields.isBoolean(option.getValue())) {
                switches.put(option.getKey(), option.getValue().getAsBoolean());
            } else {
                problems.add("option " + option.getKey() + " should be true or false, so it was left out");
            }
        }
        return switches;
    }
}
