package dev.flatgen.dimensions.rules;

/**
 * The colours the dimensions are painted in wherever Flatgen draws them, so that the column, the biomes' landscapes,
 * the ground the game makes and the presets' cards agree: each dimension's air, and the blocks of its ground. ARGB.
 */
public final class DimensionColours {
    /** The air of each dimension: the Overworld's sky, the Nether's haze, the End's dark. */
    public static final int OVERWORLD_SKY = 0xFF9CC3F7;
    public static final int NETHER_SKY = 0xFF3A1414;
    public static final int END_SKY = 0xFF15111F;
    /** The Nether's ground and the End's. */
    public static final int NETHERRACK = 0xFF6E2A2A;
    public static final int END_STONE = 0xFFDBD8A0;

    private DimensionColours() {
    }
}
