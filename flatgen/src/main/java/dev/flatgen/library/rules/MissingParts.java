package dev.flatgen.library.rules;

import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;

/**
 * What becomes of a saved preset's parts the game lacks when the player chooses to apply it anyway: a missing block
 * becomes stone, a missing biome plains, and a missing structure set is left out. Nothing is dropped without asking.
 */
public final class MissingParts {
    /** The block a layer of a block the game lacks becomes. */
    public static final String STAND_IN_BLOCK = "minecraft:stone";

    private MissingParts() {
    }

    /** The preset with each part the game lacks replaced or left out. */
    public static Preset replaced(Preset preset, MissingIds missing) {
        return preset
                .withLayers(preset.layers().stream()
                        .map(layer -> missing.blocks().contains(layer.block())
                                ? new Layer(STAND_IN_BLOCK, layer.height()) : layer)
                        .toList())
                .withBiome(missing.biomes().contains(preset.biome()) ? Preset.DEFAULT_BIOME : preset.biome())
                .withStructureSets(preset.structureSets().map(sets -> sets.stream()
                        .filter(set -> !missing.structureSets().contains(set)).toList()));
    }
}
