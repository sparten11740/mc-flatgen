package dev.flatgen.biomes.rules;

import java.util.List;

/**
 * What Flatgen shows and works out of a biome: its colours for the biome picker, and its climate and spawns for the
 * world facts (F08).
 *
 * @param id      the biome's id, such as {@code minecraft:plains}
 * @param colours its colours, as the game tints the world in it
 * @param climate its weather
 * @param spawns  what spawns in it
 */
public record BiomeInfo(String id, Colours colours, Climate climate, Spawns spawns) {
    /**
     * A biome's colours, RGB.
     *
     * @param grass   the colour of grass
     * @param foliage the colour of leaves
     * @param water   the colour of water
     * @param sky     the colour of the sky
     */
    public record Colours(int grass, int foliage, int water, int sky) {
        /** Fully opaque, as a colour drawn from an RGB one needs. */
        private static final int OPAQUE = 0xFF000000;

        /** The colours a card shows, in order: grass, leaves, water and sky, each ARGB and opaque. */
        public List<Integer> swatches() {
            return List.of(OPAQUE | grass, OPAQUE | foliage, OPAQUE | water, OPAQUE | sky);
        }
    }

    /**
     * A biome's weather.
     *
     * @param temperature   its base temperature, before height cools it
     * @param precipitation whether it rains or snows in it at all
     */
    public record Climate(float temperature, boolean precipitation) {
    }

    /**
     * What spawns in a biome.
     *
     * @param monsters      whether any monster does
     * @param slimes        whether slimes are among its monsters, which spawn in slime chunks low down
     * @param surfaceSlimes whether slimes also spawn on its surface at night, as in swamps
     */
    public record Spawns(boolean monsters, boolean slimes, boolean surfaceSlimes) {
    }

    /** The namespace of its id: the mod or data pack it comes from. */
    public String namespace() {
        int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }
}
