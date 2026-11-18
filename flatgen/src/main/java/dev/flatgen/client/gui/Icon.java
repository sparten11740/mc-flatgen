package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;

/** A picture 16 GUI pixels square that a widget shows beside or in place of words: an item, a block or a texture. */
@FunctionalInterface
public interface Icon {
    /** The size of an icon, in GUI pixels. */
    int SIZE = 16;
    /** The colour of the stroke that strikes an icon through, saying "no". */
    int STRIKE = 0xFFE03030;
    /** The thickness of that stroke, in GUI pixels. */
    int STROKE = 2;

    /** Draws the picture with its top left corner here. */
    void draw(Canvas canvas, int x, int y);

    /** An item's picture, by its id, such as {@code minecraft:clock}. */
    static Icon item(String id) {
        return (canvas, x, y) -> canvas.itemIcon(id, x, y);
    }

    /** A block's picture, by its id: its item, or its side when it has none. */
    static Icon block(String id) {
        return (canvas, x, y) -> canvas.blockIcon(id, x, y);
    }

    /** This picture struck through in red from its top left corner to its bottom right, saying "no". */
    static Icon struck(Icon icon) {
        return (canvas, x, y) -> drawStruck(icon, canvas, x, y);
    }

    private static void drawStruck(Icon icon, Canvas canvas, int x, int y) {
        icon.draw(canvas, x, y);
        for (int step = 0; step <= SIZE - STROKE; step++) {
            canvas.fill(new Rect(x + step, y + step, STROKE, STROKE), STRIKE);
        }
    }

    /** A texture 16 by 16 pixels, by its location, such as {@code skyliners:textures/gui/airport.png}. */
    static Icon texture(String location) {
        return (canvas, x, y) -> canvas.texture(location, x, y);
    }
}
