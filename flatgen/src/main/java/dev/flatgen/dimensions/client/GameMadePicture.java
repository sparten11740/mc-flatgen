package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.preset.rules.Dimension;

/**
 * A dimension as the game makes it, seen from the side, in the world's place while it has no layers of its own: the
 * Nether's jagged netherrack between its ceiling and a lava sea, the End's island of end stone over the void. It says
 * by its shape that the dimension is not flat; it takes no input.
 *
 * @param dimension the Nether or the End
 * @param bounds    where it is drawn
 */
public record GameMadePicture(Dimension dimension, Rect bounds) implements Widget {
    /** How far the picture sits inside its area, in GUI pixels, clear of the panel's rounded corners. */
    private static final int INSET = 3;
    /** How wide each step of the jagged ground is, in GUI pixels. */
    private static final int STEP = 8;
    /** The jagged ground's height at each step, in hundredths of the height it stands in, repeating along it. */
    private static final int[] GROUND = {22, 28, 18, 32, 24, 36, 20, 30, 26, 34, 19, 27};
    /** How far the Nether's ceiling hangs down at each step, in hundredths of the picture's height. */
    private static final int[] CEILING = {12, 18, 9, 16, 22, 11, 19, 14, 8, 17, 13, 20};
    /** Where the Nether's lava sea starts, and how deep it is, in hundredths of the picture's height. */
    private static final int LAVA_TOP = 72;
    private static final int LAVA_DEPTH = 8;
    /** Where the End's island is, in hundredths of the picture's height and width. */
    private static final int ISLAND_TOP = 55;
    private static final int ISLAND_DEPTH = 18;
    private static final int ISLAND_LEFT = 15;
    private static final int ISLAND_WIDTH = 70;
    /** The Nether's ceiling and ground, lava and bedrock, and the End's end stone, ARGB. */
    private static final int NETHERRACK = 0xFF6E2A2A;
    private static final int CEILING_ROCK = 0xFF4A1A1A;
    private static final int LAVA = 0xFFE8742A;
    private static final int BEDROCK = 0xFF2B2B2B;
    private static final int END_STONE = 0xFFDBD8A0;

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Rect area = new Rect(bounds.x() + INSET, bounds.y() + INSET, bounds.width() - 2 * INSET,
                bounds.height() - 2 * INSET);
        if (dimension == Dimension.END) {
            drawEnd(canvas, area);
            return;
        }
        canvas.fill(area, DimensionPictures.air(Dimension.NETHER));
        drawSteps(canvas, area, CEILING, true, CEILING_ROCK);
        int lavaTop = area.y() + area.height() * LAVA_TOP / 100;
        canvas.fill(new Rect(area.x(), lavaTop, area.width(), area.bottom() - lavaTop), NETHERRACK);
        drawSteps(canvas, new Rect(area.x(), area.y(), area.width(), lavaTop - area.y()), GROUND, false,
                NETHERRACK);
        canvas.fill(new Rect(area.x(), lavaTop, area.width(), area.height() * LAVA_DEPTH / 100), LAVA);
        canvas.fill(new Rect(area.x(), area.bottom() - STEP, area.width(), STEP), BEDROCK);
    }

    /** The End: a jagged island of end stone in the middle of a dark sky. */
    private static void drawEnd(Canvas canvas, Rect area) {
        canvas.fill(area, DimensionPictures.air(Dimension.END));
        Rect island = new Rect(area.x() + area.width() * ISLAND_LEFT / 100, area.y() + area.height() * ISLAND_TOP / 100,
                area.width() * ISLAND_WIDTH / 100, area.height() * ISLAND_DEPTH / 100);
        canvas.fill(new Rect(island.x(), island.y() + island.height() / 2, island.width(), island.height() / 2),
                END_STONE);
        drawSteps(canvas, new Rect(island.x(), island.y(), island.width(), island.height() / 2), GROUND, false,
                END_STONE);
    }

    /**
     * Steps of jagged rock across an area: hanging from its top when {@code hanging}, else standing on its bottom,
     * each as tall as the pattern says.
     */
    private static void drawSteps(Canvas canvas, Rect area, int[] heights, boolean hanging, int color) {
        for (int index = 0; index * STEP < area.width(); index++) {
            int height = area.height() * heights[index % heights.length] / 100;
            int width = Math.min(STEP, area.width() - index * STEP);
            int top = hanging ? area.y() : area.bottom() - Math.min(height, area.height());
            canvas.fill(new Rect(area.x() + index * STEP, top, width, Math.min(height, area.height())), color);
        }
    }
}
