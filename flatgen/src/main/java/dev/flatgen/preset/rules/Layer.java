package dev.flatgen.preset.rules;

/**
 * One layer of a superflat world.
 *
 * @param block  the layer's block, an id such as {@code minecraft:stone}
 * @param height how many blocks deep it is, at least one
 */
public record Layer(String block, int height) {
    public Layer {
        if (height < 1) {
            throw new IllegalArgumentException("a layer is at least one block deep, not " + height);
        }
    }
}
