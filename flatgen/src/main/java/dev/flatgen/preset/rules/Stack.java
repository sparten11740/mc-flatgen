package dev.flatgen.preset.rules;

import java.util.List;

/**
 * A dimension's superflat stack: its layers from the bottom up and its biome, an id.
 *
 * @param layers the layers from the bottom up
 * @param biome  the biome, an id
 */
public record Stack(List<Layer> layers, String biome) {
    public Stack {
        layers = List.copyOf(layers);
    }

    /** How tall the stack is, in blocks. */
    public int height() {
        return Layer.total(layers);
    }
}
