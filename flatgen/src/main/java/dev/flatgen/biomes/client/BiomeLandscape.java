package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.biomes.rules.BiomeScenes;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.rules.Rect;
import java.util.List;

/**
 * A biome painted as a little landscape at any size, as {@link BiomeScenes} says it looks: its sky with the sun over
 * the Overworld, its ground and water, and what stands on the ground, in the biome's own colours where the scene asks
 * for them, so a player who cannot read tells a desert from a badlands and a taiga from a birch forest.
 */
final class BiomeLandscape {
    /** The sun's size and its distance from the top, in hundredths of the picture's height. */
    private static final int SUN = 14;
    private static final int SUN_TOP = 12;
    /** The sun's distance from the right, in hundredths of the picture's width: clear of a card's star mark. */
    private static final int SUN_RIGHT = 24;
    private static final int SUN_COLOR = 0xFFFFF3B0;
    /** How much of the ground its top is, in hundredths of the ground's height. */
    private static final int TOP = 25;
    private static final int WHOLE = 100;
    /** How much larger than its picture a close-up paints the landscape, in hundredths. */
    private static final int CLOSE_UP_SIZE = 150;
    /** Where a close-up's horizon is, in hundredths of the picture's height from its top: high, the ground big. */
    private static final int CLOSE_UP_HORIZON = 60;

    private BiomeLandscape() {
    }

    /** Paints a biome over this area. */
    static void draw(Canvas canvas, BiomeInfo biome, Rect area) {
        draw(canvas::fill, biome, BiomeScenes.of(biome), area);
    }

    /** Fills a rectangle with a colour: the canvas's own fill, or one that keeps a close-up inside its picture. */
    private interface Fill {
        void fill(Rect area, int colour);
    }

    /**
     * Paints a biome near, for a picture too small for its whole landscape: the landscape larger than the area and cut
     * to it, its horizon high and what stands on the ground in the middle as far as the landscape reaches, so a tree,
     * a cactus or a peak is big.
     */
    static void drawCloseUp(Canvas canvas, BiomeInfo biome, Rect area) {
        BiomeScenes.Scene scene = BiomeScenes.of(biome);
        int width = area.width() * CLOSE_UP_SIZE / WHOLE;
        int height = area.height() * CLOSE_UP_SIZE / WHOLE;
        int x = within(area.x() + area.width() / 2 - width * focus(scene) / WHOLE, area.right() - width, area.x());
        int horizon = area.y() + area.height() * CLOSE_UP_HORIZON / WHOLE;
        int y = within(horizon - height * (WHOLE - BiomeScenes.GROUND_SHARE) / WHOLE, area.bottom() - height,
                area.y());
        draw((part, colour) -> canvas.fill(part.within(area), colour), biome, scene, new Rect(x, y, width, height));
    }

    /** Where a scene's shapes stand, the middle of them all, in hundredths of the width; the middle without any. */
    private static int focus(BiomeScenes.Scene scene) {
        int left = scene.patches().stream().mapToInt(BiomeScenes.Patch::x).min().orElse(WHOLE / 2);
        int right = scene.patches().stream().mapToInt(patch -> patch.x() + patch.width()).max().orElse(WHOLE / 2);
        return (left + right) / 2;
    }

    /** A number kept between a least and a most. */
    private static int within(int value, int least, int most) {
        return Math.max(least, Math.min(most, value));
    }

    private static void draw(Fill canvas, BiomeInfo biome, BiomeScenes.Scene scene, Rect area) {
        BiomeInfo.Colours colours = biome.colours();
        boolean ownSky = scene.sky() == BiomeScenes.OWN;
        canvas.fill(area, ownSky ? colours.skyArgb() : scene.sky());
        if (ownSky) {
            int sun = share(area.height(), SUN);
            canvas.fill(new Rect(area.right() - share(area.width(), SUN_RIGHT) - sun,
                    area.y() + share(area.height(), SUN_TOP), sun, sun), SUN_COLOR);
        }
        int groundTop = area.bottom() - share(area.height(), BiomeScenes.GROUND_SHARE);
        Rect ground = new Rect(area.x(), groundTop, area.width(), area.bottom() - groundTop);
        if (scene.ground().body() != BiomeScenes.NONE) {
            drawGround(canvas, scene.ground(), colours, ground);
        }
        int waterWidth = area.width() * scene.water() / WHOLE;
        canvas.fill(new Rect(area.right() - waterWidth, ground.y(), waterWidth, ground.height()),
                colours.waterArgb());
        scene.patches().forEach(patch -> drawPatch(canvas, patch, colours, area, groundTop));
    }

    /** A ground: its colour or its bands, then its top. */
    private static void drawGround(Fill canvas, BiomeScenes.Ground look, BiomeInfo.Colours colours, Rect ground) {
        canvas.fill(ground, look.body());
        List<Integer> bands = look.bands();
        for (int index = 0; index < bands.size(); index++) {
            int top = ground.y() + ground.height() * index / bands.size();
            int bottom = ground.y() + ground.height() * (index + 1) / bands.size();
            canvas.fill(new Rect(ground.x(), top, ground.width(), bottom - top), bands.get(index));
        }
        if (look.top() != BiomeScenes.NONE) {
            int top = look.top() == BiomeScenes.OWN ? colours.grassArgb() : look.top();
            canvas.fill(new Rect(ground.x(), ground.y(), ground.width(), share(ground.height(), TOP)), top);
        }
    }

    /** A rectangle standing on the ground, in the biome's leaves where the scene asks for them. */
    private static void drawPatch(Fill canvas, BiomeScenes.Patch patch, BiomeInfo.Colours colours, Rect area,
            int groundTop) {
        int height = share(area.height(), patch.height());
        int bottom = groundTop - area.height() * patch.bottom() / WHOLE;
        int colour = patch.colour() == BiomeScenes.OWN ? colours.foliageArgb() : patch.colour();
        canvas.fill(new Rect(area.x() + area.width() * patch.x() / WHOLE, bottom - height,
                share(area.width(), patch.width()), height), colour);
    }

    /** A share of a length, in hundredths, at least one pixel. */
    private static int share(int length, int hundredths) {
        return Math.max(1, length * hundredths / WHOLE);
    }
}
