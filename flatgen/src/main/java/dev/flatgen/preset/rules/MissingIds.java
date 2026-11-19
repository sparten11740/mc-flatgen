package dev.flatgen.preset.rules;

import java.util.List;

/**
 * The ids of a preset that the game does not have, by what they name, typically from a mod that is not installed.
 *
 * @param blocks        layers' blocks
 * @param biomes        the biome
 * @param structureSets structure sets or tags of them
 */
public record MissingIds(List<String> blocks, List<String> biomes, List<String> structureSets) {
    /** Nothing missing: the game has every id. */
    public static final MissingIds NONE = new MissingIds(List.of(), List.of(), List.of());

    public MissingIds {
        blocks = List.copyOf(blocks);
        biomes = List.copyOf(biomes);
        structureSets = List.copyOf(structureSets);
    }

    /** Whether the game has everything the preset names. */
    public boolean isEmpty() {
        return blocks.isEmpty() && biomes.isEmpty() && structureSets.isEmpty();
    }
}
