package dev.flatgen.library.rules;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import java.util.Optional;

/**
 * What becomes of a saved preset's parts the game lacks when the player chooses to apply it anyway: a missing block
 * becomes stone, a missing biome the dimension's own (plains in the overworld), and a missing structure set is left
 * out. Nothing is dropped without asking.
 */
public final class MissingParts {
    /** The block a layer of a block the game lacks becomes. */
    public static final String STAND_IN_BLOCK = "minecraft:stone";

    private MissingParts() {
    }

    /** The preset with each part the game lacks replaced or left out, in each of its dimensions' stacks. */
    public static Preset replaced(Preset preset, MissingIds missing) {
        Preset result = preset.withStructureSets(preset.structureSets().map(sets -> sets.stream()
                .filter(set -> !missing.structureSets().contains(set)).toList()));
        for (Dimension dimension : Dimension.values()) {
            Optional<Stack> stack = result.stack(dimension).map(own -> replaced(own, dimension, missing));
            result = result.withStack(dimension, stack);
        }
        return result;
    }

    /** A dimension's stack with its missing blocks as stone and a missing biome as the dimension's own. */
    private static Stack replaced(Stack stack, Dimension dimension, MissingIds missing) {
        return new Stack(stack.layers().stream()
                .map(layer -> missing.blocks().contains(layer.block()) ? new Layer(STAND_IN_BLOCK, layer.height())
                        : layer)
                .toList(), missing.biomes().contains(stack.biome()) ? dimension.defaultBiome() : stack.biome());
    }
}
