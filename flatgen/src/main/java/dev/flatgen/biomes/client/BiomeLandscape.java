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

    private BiomeLandscape() {
    }

    /** Paints a biome over this area. */
    static void draw(Canvas canvas, BiomeInfo biome, Rect area) {
        BiomeScenes.Scene scene = BiomeScenes.of(biome);
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
    private static void drawGround(Canvas canvas, BiomeScenes.Ground look, BiomeInfo.Colours colours, Rect ground) {
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
    private static void drawPatch(Canvas canvas, BiomeScenes.Patch patch, BiomeInfo.Colours colours, Rect area,
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
