package dev.flatgen.client.gui;

/** A picture 16 GUI pixels square that a widget shows beside or in place of words: an item, a block or a texture. */
@FunctionalInterface
public interface Icon {
    /** The size of an icon, in GUI pixels. */
    int SIZE = 16;

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

    /** A texture 16 by 16 pixels, by its location, such as {@code skyliners:textures/gui/airport.png}. */
    static Icon texture(String location) {
        return (canvas, x, y) -> canvas.texture(location, x, y);
    }
}
