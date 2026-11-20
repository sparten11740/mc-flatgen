package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.rules.Rect;

/** A biome's picture as an icon: its landscape ({@link BiomeLandscape}) 16 pixels square, framed. */
final class BiomeIcon implements Icon {
    private static final int RIM = 0xFF000000;

    private final BiomeInfo biome;

    BiomeIcon(BiomeInfo biome) {
        this.biome = biome;
    }

    @Override
    public void draw(Canvas canvas, int x, int y) {
        Rect area = new Rect(x, y, SIZE, SIZE);
        BiomeLandscape.draw(canvas, biome, area);
        canvas.outline(area, RIM);
    }
}
