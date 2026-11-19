package dev.flatgen.exports.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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
     * @param resources the resource pack format
     * @param data      the data pack format
     */
    public record PackFormats(int resources, int data) {
    }

    /** The pack's files, by path, in the order a zip lists them. */
    public static Map<String, String> of(Pack pack) {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("pack.mcmeta", PRETTY.toJson(mcmeta(pack)));
        files.put("data/" + NAMESPACE + "/worldgen/world_preset/" + pack.id() + ".json", pack.preset());
        files.put("data/minecraft/tags/worldgen/world_preset/normal.json", PRETTY.toJson(tag(pack.id())));
        JsonObject words = new JsonObject();
        words.addProperty("generator." + NAMESPACE + "." + pack.id(), pack.name());
        files.put("assets/" + NAMESPACE + "/lang/en_us.json", PRETTY.toJson(words));
        return files;
    }

    private static JsonObject mcmeta(Pack pack) {
        JsonArray formats = new JsonArray();
        formats.add(Math.min(pack.formats().resources(), pack.formats().data()));
        formats.add(Math.max(pack.formats().resources(), pack.formats().data()));
        JsonObject about = new JsonObject();
        about.addProperty("pack_format", pack.formats().data());
        about.add("supported_formats", formats);
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
