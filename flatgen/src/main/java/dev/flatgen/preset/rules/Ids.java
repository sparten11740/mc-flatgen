package dev.flatgen.preset.rules;

/** The game's ids of blocks, biomes and structure sets, {@code namespace:path}, as presets and pickers handle them. */
public final class Ids {
    /** The game's own namespace, which an id written without one has. */
    public static final String GAME = "minecraft";

    private Ids() {
    }

    /** An id's namespace, the mod or data pack it comes from; the game's for an id written without one. */
    public static String namespace(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? GAME : id.substring(0, colon);
    }

    /** An id with the game's namespace when it is written without one, as the game reads {@code stone}. */
    public static String withNamespace(String id) {
        return id.contains(":") ? id : GAME + ":" + id;
    }
}
