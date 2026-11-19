package dev.flatgen.client.gui;

/** A picture 16 GUI pixels square that a widget shows beside or in place of words: an item, a block or a texture. */
@FunctionalInterface
public interface Icon {
    /** The size of an icon, in GUI pixels. */
    int SIZE = 16;
    /** Yes, use this, done: the picture of every button that accepts. */
    Icon TICK = own("tick");
    /** No, cancel: the picture of every button that throws away what was being done. */
    Icon CROSS = own("cross");
    /** Something the game lacks or cannot read: the barrier. */
    Icon MISSING = item("minecraft:barrier");
    /** Back to where the player came from, keeping what was done: a left arrow. */
    Icon BACK = own("back");
    /** Text leaving the game for the clipboard: paper with an arrow out. */
    Icon TO_CLIPBOARD = badged(item("minecraft:paper"), "badge_out");
    /** Text coming in from the clipboard: paper with an arrow in. */
    Icon FROM_CLIPBOARD = badged(item("minecraft:paper"), "badge_in");

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

    /**
     * This picture under the "no" sign, a red ring and slash with a dark rim, saying "no" by its shape on any picture,
     * green ones too.
     */
    static Icon struck(Icon icon) {
        Icon no = own("no");
        return (canvas, x, y) -> drawOver(icon, no, canvas, x, y);
    }

    /** A sprite of the game's GUI atlas, by its id, such as {@code flatgen:icon/undo}. */
    static Icon sprite(String id) {
        return (canvas, x, y) -> canvas.sprite(id, x, y);
    }

    /** One of Flatgen's own pictures, by its name in {@code textures/gui/sprites/icon/}, such as {@code undo}. */
    static Icon own(String name) {
        return sprite("flatgen:icon/" + name);
    }

    /** This picture with a badge over it, one of Flatgen's own drawn where an item's count goes, such as a plus. */
    static Icon badged(Icon icon, String badge) {
        Icon over = own(badge);
        return (canvas, x, y) -> drawOver(icon, over, canvas, x, y);
    }

    private static void drawOver(Icon below, Icon above, Canvas canvas, int x, int y) {
        below.draw(canvas, x, y);
        above.draw(canvas, x, y);
    }

    /** A texture 16 by 16 pixels, by its location, such as {@code skyliners:textures/gui/airport.png}. */
    static Icon texture(String location) {
        return (canvas, x, y) -> canvas.texture(location, x, y);
    }
}
