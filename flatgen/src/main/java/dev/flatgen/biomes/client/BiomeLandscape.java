package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.biomes.rules.BiomeKinds;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.rules.Rect;

/**
 * A biome painted as a little landscape at any size: its sky, its ground topped with its grass, a tree of its leaves
 * where it is wooded, its water where it is wet and the sun over the Overworld, in the biome's own colours and as its
 * kind's scene has it, so a player who cannot read tells a desert from a jungle (docs/design/simple-editor.md).
 */
final class BiomeLandscape {
    /** Shares of the picture, in hundredths: the ground's of its height, the grass's of the ground's height. */
    private static final int GROUND = 30;
    private static final int GRASS = 25;
    /** The tree's trunk and crown, in hundredths of the picture's width and height, from its left and the ground. */
    private static final int TRUNK_X = 22;
    private static final int TRUNK_WIDTH = 6;
    private static final int TRUNK_HEIGHT = 18;
    private static final int CROWN_X = 12;
    private static final int CROWN_WIDTH = 26;
    private static final int CROWN_HEIGHT = 28;
    /** The sun's size and its distance from the top, in hundredths of the picture's height. */
    private static final int SUN = 14;
    private static final int SUN_TOP = 12;
    /** The sun's distance from the right, in hundredths of the picture's width: clear of a card's star mark. */
    private static final int SUN_RIGHT = 24;
    private static final int TRUNK_COLOR = 0xFF6B4A2C;
    private static final int SUN_COLOR = 0xFFFFF3B0;
    private static final int OPAQUE = 0xFF000000;
    private static final int WHOLE = 100;

    private BiomeLandscape() {
    }

    /** Paints a biome over this area. */
    static void draw(Canvas canvas, BiomeInfo biome, Rect area) {
        BiomeKinds.Scene scene = BiomeKinds.scene(biome);
        BiomeInfo.Colours colours = biome.colours();
        boolean ownSky = scene.sky() == BiomeKinds.Scene.OWN;
        canvas.fill(area, OPAQUE | (ownSky ? colours.sky() : scene.sky()));
        int groundHeight = share(area.height(), GROUND);
        Rect ground = new Rect(area.x(), area.bottom() - groundHeight, area.width(), groundHeight);
        canvas.fill(ground, OPAQUE | scene.ground());
        if (scene.grass()) {
            canvas.fill(new Rect(ground.x(), ground.y(), ground.width(), share(groundHeight, GRASS)),
                    OPAQUE | colours.grass());
        }
        int waterWidth = area.width() * scene.water() / WHOLE;
        Rect water = new Rect(area.right() - waterWidth, ground.y(), waterWidth, groundHeight);
        canvas.fill(water, OPAQUE | colours.water());
        if (scene.tree()) {
            drawTree(canvas, colours.foliage(), area, ground.y());
        }
        if (ownSky) {
            int sun = share(area.height(), SUN);
            canvas.fill(new Rect(area.right() - share(area.width(), SUN_RIGHT) - sun,
                    area.y() + share(area.height(), SUN_TOP), sun, sun), SUN_COLOR);
        }
    }

    /** A tree of these leaves standing on the ground's top. */
    private static void drawTree(Canvas canvas, int leaves, Rect area, int groundTop) {
        int trunkHeight = share(area.height(), TRUNK_HEIGHT);
        canvas.fill(new Rect(area.x() + share(area.width(), TRUNK_X), groundTop - trunkHeight,
                share(area.width(), TRUNK_WIDTH), trunkHeight), TRUNK_COLOR);
        int crownHeight = share(area.height(), CROWN_HEIGHT);
        canvas.fill(new Rect(area.x() + share(area.width(), CROWN_X), groundTop - trunkHeight - crownHeight + 1,
                share(area.width(), CROWN_WIDTH), crownHeight), OPAQUE | leaves);
    }

    /** A share of a length, in hundredths, at least one pixel. */
    private static int share(int length, int hundredths) {
        return Math.max(1, length * hundredths / WHOLE);
    }
}
