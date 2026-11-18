package dev.flatgen.biomes.rules;

import java.util.List;

/**
 * What the biome picker shows of a biome: its id and its colours, RGB, as the game tints the world in it.
 *
 * @param id      the biome's id, such as {@code minecraft:plains}
 * @param grass   the colour of grass
 * @param foliage the colour of leaves
 * @param water   the colour of water
 * @param sky     the colour of the sky
 */
public record BiomeInfo(String id, int grass, int foliage, int water, int sky) {
    /** Fully opaque, as a colour drawn from an RGB one needs. */
    private static final int OPAQUE = 0xFF000000;

    /** The colours a card shows, in order: grass, leaves, water and sky, each ARGB and opaque. */
    public List<Integer> swatches() {
        return List.of(OPAQUE | grass, OPAQUE | foliage, OPAQUE | water, OPAQUE | sky);
    }

    /** The namespace of its id: the mod or data pack it comes from. */
    public String namespace() {
        int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }
}
