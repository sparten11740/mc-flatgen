package dev.flatgen.layers.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.rules.Rect;

/**
 * A small figure of a person standing on the top layer of the column, so a player who cannot read sees at once where
 * the ground they will stand on is.
 */
final class StandingFigure {
    /** The figure's parts, in GUI pixels: head, body and legs, side by side as wide as the head. */
    private static final int HEAD = 4;
    private static final int BODY = 6;
    private static final int LEGS = 6;
    /** How tall the figure is, in GUI pixels. */
    static final int HEIGHT = HEAD + BODY + LEGS;
    /** How far each arm stands out beside the body, in GUI pixels. */
    private static final int ARM = 1;
    /** The figure's colours: skin, hair, shirt, trousers. ARGB. */
    private static final int SKIN = 0xFFE0B48A;
    private static final int HAIR = 0xFF5A3A22;
    private static final int SHIRT = 0xFF4F9E5A;
    private static final int TROUSERS = 0xFF3D4A80;

    private StandingFigure() {
    }

    /** Draws the figure with its feet on this line, in the middle of this x. */
    static void draw(Canvas canvas, int middleX, int feetY) {
        int left = middleX - HEAD / 2;
        int legsTop = feetY - LEGS;
        int bodyTop = legsTop - BODY;
        int headTop = bodyTop - HEAD;
        canvas.fill(new Rect(left, legsTop, HEAD, LEGS), TROUSERS);
        canvas.fill(new Rect(left - ARM, bodyTop, HEAD + 2 * ARM, BODY), SKIN);
        canvas.fill(new Rect(left, bodyTop, HEAD, BODY), SHIRT);
        canvas.fill(new Rect(left, headTop, HEAD, HEAD), SKIN);
        canvas.fill(new Rect(left, headTop, HEAD, 1), HAIR);
    }
}
