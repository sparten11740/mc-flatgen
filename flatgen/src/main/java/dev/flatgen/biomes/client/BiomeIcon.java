package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.rules.Rect;

/**
 * A biome's picture: a little landscape in its own colours, sky above, a tree of its leaves on its grass, and its
 * water, so a player who cannot read tells a desert from a jungle.
 */
final class BiomeIcon implements Icon {
    /** Where the landscape's parts are, in the picture's 16 pixels: sky down to the horizon, water from its left. */
    private static final int HORIZON = 9;
    private static final int WATER_LEFT = 10;
    private static final int WATER_TOP = 11;
    private static final Rect LEAVES = new Rect(2, 3, 5, 4);
    private static final Rect TRUNK = new Rect(4, 7, 1, 2);
    private static final int TRUNK_COLOR = 0xFF6B4A2C;
    private static final int RIM = 0xFF000000;
    private static final int OPAQUE = 0xFF000000;

    private final BiomeInfo.Colours colours;

    BiomeIcon(BiomeInfo.Colours colours) {
        this.colours = colours;
    }

    @Override
    public void draw(Canvas canvas, int x, int y) {
        canvas.fill(new Rect(x, y, SIZE, HORIZON), OPAQUE | colours.sky());
        canvas.fill(new Rect(x, y + HORIZON, SIZE, SIZE - HORIZON), OPAQUE | colours.grass());
        canvas.fill(new Rect(x + WATER_LEFT, y + WATER_TOP, SIZE - WATER_LEFT, SIZE - WATER_TOP),
                OPAQUE | colours.water());
        canvas.fill(at(LEAVES, x, y), OPAQUE | colours.foliage());
        canvas.fill(at(TRUNK, x, y), TRUNK_COLOR);
        canvas.outline(new Rect(x, y, SIZE, SIZE), RIM);
    }

    /** A part of the picture where the picture is drawn. */
    private static Rect at(Rect part, int x, int y) {
        return new Rect(x + part.x(), y + part.y(), part.width(), part.height());
    }
}
