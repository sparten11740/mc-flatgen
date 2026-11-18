package dev.flatgen.blocks.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The blocks a player picked lately and the ones they starred, which the block picker shows first, as
 * config/flatgen/blocks.json keeps them.
 *
 * @param recent  the blocks picked lately, the latest first, at most {@link #MOST_RECENT}
 * @param starred the blocks starred, in the order they were starred
 */
public record BlockMemory(List<String> recent, List<String> starred) {
    /** How many recent blocks are kept. */
    public static final int MOST_RECENT = 16;
    /** The memory of a player who has picked and starred nothing. */
    public static final BlockMemory EMPTY = new BlockMemory(List.of(), List.of());

    private static final String RECENT = "recent";
    private static final String STARRED = "starred";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public BlockMemory {
        recent = List.copyOf(recent.subList(0, Math.min(recent.size(), MOST_RECENT)));
        starred = List.copyOf(starred);
    }

    /** The memory once a block is picked: it becomes the latest recent block. */
    public BlockMemory used(String block) {
        Set<String> blocks = new LinkedHashSet<>();
        blocks.add(block);
        blocks.addAll(recent);
        return new BlockMemory(new ArrayList<>(blocks), starred);
    }

    /** The memory with a block starred if it was not, and unstarred if it was. */
    public BlockMemory toggleStar(String block) {
        List<String> blocks = new ArrayList<>(starred);
        if (!blocks.remove(block)) {
            blocks.add(block);
        }
        return new BlockMemory(recent, blocks);
    }

    /**
     * Reads the memory from JSON; a missing list is empty, and anything in a list but ids is left out.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static BlockMemory fromJson(String json) {
        JsonObject root = JsonFields.parseObject(json);
        List<String> ignored = new ArrayList<>();
        return new BlockMemory(ids(root, RECENT, ignored), ids(root, STARRED, ignored));
    }

    /** The memory as JSON, as the file stores it. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.add(RECENT, array(recent));
        root.add(STARRED, array(starred));
        return GSON.toJson(root);
    }

    private static List<String> ids(JsonObject root, String key, List<String> problems) {
        List<String> ids = new ArrayList<>();
        for (JsonElement element : JsonFields.array(root, key, problems).orElse(new JsonArray())) {
            if (JsonFields.isText(element)) {
                ids.add(element.getAsString());
            }
        }
        return ids;
    }

    private static JsonArray array(List<String> ids) {
        JsonArray array = new JsonArray();
        ids.forEach(array::add);
        return array;
    }
}
