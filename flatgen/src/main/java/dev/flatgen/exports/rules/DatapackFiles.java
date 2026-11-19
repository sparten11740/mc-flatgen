package dev.flatgen.exports.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The files of a datapack that adds a preset as a World Type (F21), by their paths in the pack: its {@code
 * pack.mcmeta}, the world preset, the {@code #minecraft:normal} tag entry that lists it on the Create World screen,
 * and the World Type's name, read when the pack is also used as a resource pack.
 */
public final class DatapackFiles {
    /** The namespace the world presets are in. */
    public static final String NAMESPACE = "flatgen";
    /**
     * The first data pack format whose game reads the formats a pack takes as a range, {@code min_format} to {@code
     * max_format}, rather than {@code pack_format} with {@code supported_formats}.
     */
    static final int RANGED_FROM_DATA_FORMAT = 82;

    private DatapackFiles() {
    }

    /**
     * A pack's files.
     *
     * @param formats the resource and data pack formats of the game's version, the pack saying it takes both
     * @param id      the world preset's path, letters, digits and underscores
     * @param name    the World Type's name, the preset's
     * @param preset  the world preset, as the game writes one
     */
    public record Pack(PackFormats formats, String id, String name, String preset) {
    }

    /**
     * A version's pack formats.
     *
     * @param resources the resource pack format, its major version
     * @param data      the data pack format, its major version
     * @param dataMinor the data pack format's minor version, 0 for a version before formats had them
     */
    public record PackFormats(int resources, int data, int dataMinor) {
    }

    /** The pack's files, by path, in the order a zip lists them. */
    public static Map<String, String> of(Pack pack) {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("pack.mcmeta", JsonFields.PRETTY.toJson(mcmeta(pack)));
        files.put("data/" + NAMESPACE + "/worldgen/world_preset/" + pack.id() + ".json", pack.preset());
        files.put("data/minecraft/tags/worldgen/world_preset/normal.json", JsonFields.PRETTY.toJson(tag(pack.id())));
        JsonObject words = new JsonObject();
        words.addProperty("generator." + NAMESPACE + "." + pack.id(), pack.name());
        files.put("assets/" + NAMESPACE + "/lang/en_us.json", JsonFields.PRETTY.toJson(words));
        return files;
    }

    /** The pack's description and the formats it takes, from the resource pack's to the data pack's. */
    private static JsonObject mcmeta(Pack pack) {
        PackFormats formats = pack.formats();
        JsonObject about = new JsonObject();
        if (formats.data() >= RANGED_FROM_DATA_FORMAT) {
            JsonArray top = new JsonArray();
            top.add(formats.data());
            top.add(formats.dataMinor());
            about.addProperty("min_format", formats.resources());
            about.add("max_format", top);
        } else {
            JsonArray supported = new JsonArray();
            supported.add(Math.min(formats.resources(), formats.data()));
            supported.add(Math.max(formats.resources(), formats.data()));
            about.addProperty("pack_format", formats.data());
            about.add("supported_formats", supported);
        }
        about.addProperty("description", pack.name());
        JsonObject mcmeta = new JsonObject();
        mcmeta.add("pack", about);
        return mcmeta;
    }

    private static JsonObject tag(String id) {
        JsonArray values = new JsonArray();
        values.add(NAMESPACE + ":" + id);
        JsonObject tag = new JsonObject();
        tag.add("values", values);
        return tag;
    }
}
