package dev.flatgen.search.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * What a player picked lately and starred in a picker, which it shows first; the block and biome pickers each keep
 * their own in a file of their own.
 *
 * @param recent  the ids picked lately, the latest first, at most {@link #MOST_RECENT}
 * @param starred the ids starred, in the order they were starred
 */
public record Picks(List<String> recent, List<String> starred) {
    /** How many recent picks are kept. */
    public static final int MOST_RECENT = 16;
    /** The picks of a player who has picked and starred nothing. */
    public static final Picks EMPTY = new Picks(List.of(), List.of());

    private static final String RECENT = "recent";
    private static final String STARRED = "starred";

    public Picks {
        recent = List.copyOf(recent.subList(0, Math.min(recent.size(), MOST_RECENT)));
        starred = List.copyOf(starred);
    }

    /** The picks once an id is picked: it becomes the latest. */
    public Picks used(String id) {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(id);
        ids.addAll(recent);
        return new Picks(new ArrayList<>(ids), starred);
    }

    /** The picks with an id starred if it was not, and unstarred if it was. */
    public Picks toggleStar(String id) {
        List<String> ids = new ArrayList<>(starred);
        if (!ids.remove(id)) {
            ids.add(id);
        }
        return new Picks(recent, ids);
    }

    /**
     * Reads picks from JSON; a missing list is empty, and anything in a list but text is left out.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static Picks fromJson(String json) {
        JsonObject root = JsonFields.parseObject(json);
        List<String> ignored = new ArrayList<>();
        return new Picks(JsonFields.texts(root, RECENT, ignored), JsonFields.texts(root, STARRED, ignored));
    }

    /** The picks as JSON, as their file stores them. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.add(RECENT, JsonFields.textArray(recent));
        root.add(STARRED, JsonFields.textArray(starred));
        return JsonFields.PRETTY.toJson(root);
    }
}
