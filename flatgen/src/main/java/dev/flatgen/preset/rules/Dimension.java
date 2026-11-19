package dev.flatgen.preset.rules;

/**
 * A dimension a preset can give a superflat stack (F22): the overworld always has one; the Nether and the End have one
 * only when the preset says so, and are otherwise as the game makes them.
 */
public enum Dimension {
    OVERWORLD("overworld", "minecraft:plains"),
    NETHER("nether", "minecraft:nether_wastes"),
    END("end", "minecraft:the_end");

    private final String key;
    private final String defaultBiome;

    Dimension(String key, String defaultBiome) {
        this.key = key;
        this.defaultBiome = defaultBiome;
    }

    /** The dimension's name in a preset file, as {@code nether}. */
    public String key() {
        return key;
    }

    /** The biome a new stack of this dimension gets, the dimension's own. */
    public String defaultBiome() {
        return defaultBiome;
    }
}
