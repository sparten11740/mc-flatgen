package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import java.util.function.BiConsumer;

/** A picture 16 GUI pixels square that a widget shows beside or in place of words: an item, a block or a texture. */
@FunctionalInterface
public interface Icon {
    /** The size of an icon, in GUI pixels. */
    int SIZE = 16;
    /** Something the game lacks or cannot read: the barrier. */
    Icon MISSING = item("minecraft:barrier");
    /** Back to where the player came from, keeping what was done: a left arrow. */
    Icon BACK = line("arrow_left");
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
     * This picture dimmed, with a small "no" sign, a red ring and slash, at its bottom right corner where an item's
     * count goes: what it shows does not happen, while the picture can still be told apart from the others.
     */
    static Icon struck(Icon icon) {
        return (canvas, x, y) -> drawStruck(icon, canvas, x, y);
    }

    private static void drawStruck(Icon icon, Canvas canvas, int x, int y) {
        icon.draw(canvas, x, y);
        canvas.dim(new Rect(x, y, SIZE, SIZE));
        drawMark(canvas, "no", x, y);
    }

    /** This picture with one of the look's line drawings small at its bottom right corner, such as an up arrow. */
    static Icon marked(Icon icon, String line) {
        return (canvas, x, y) -> drawMarked(icon, line, canvas, x, y);
    }

    private static void drawMarked(Icon icon, String line, Canvas canvas, int x, int y) {
        icon.draw(canvas, x, y);
        drawMark(canvas, line, x, y);
    }

    /** A line drawing small on a dark pill at an icon's bottom right corner, over what is drawn there. */
    private static void drawMark(Canvas canvas, String line, int x, int y) {
        Rect mark = new Rect(x + SIZE - NO_MARK, y + SIZE - NO_MARK, NO_MARK, NO_MARK);
        canvas.markOver(Look.COUNT, mark);
        canvas.markOver(Look.icon(line), mark);
    }

    /** A sprite of the game's GUI atlas, by its id, such as {@code flatgen:icon/undo}. */
    static Icon sprite(String id) {
        return (canvas, x, y) -> canvas.sprite(id, x, y);
    }

    /** The size of a mark at a picture's corner, such as the "no" sign of a struck one, in GUI pixels. */
    int NO_MARK = 9;

    /** How far a line drawing sits inside an icon's square, in GUI pixels, so it matches the size of text beside it. */
    int LINE_INSET = 3;

    /** One of {@link Look}'s line drawings, by its name, such as {@code undo}, in the middle of an icon's square. */
    static Icon line(String name) {
        String sprite = Look.icon(name);
        return (canvas, x, y) -> canvas.sprite(sprite, new Rect(x + LINE_INSET, y + LINE_INSET, SIZE - 2 * LINE_INSET,
                SIZE - 2 * LINE_INSET));
    }

    /** One of Flatgen's own pictures, by its name in {@code textures/gui/sprites/icon/}, such as {@code undo}. */
    static Icon own(String name) {
        return sprite("flatgen:icon/" + name);
    }

    /**
     * Up to four pictures, each drawn in a quarter of an icon, left to right and top to bottom, as a picture of every
     * entry of a kind: each draws itself in the area it is given.
     */
    static Icon quartered(List<BiConsumer<Canvas, Rect>> parts) {
        return (canvas, x, y) -> drawQuartered(parts, canvas, x, y);
    }

    private static void drawQuartered(List<BiConsumer<Canvas, Rect>> parts, Canvas canvas, int x, int y) {
        int half = SIZE / 2;
        for (int index = 0; index < parts.size(); index++) {
            parts.get(index).accept(canvas, new Rect(x + index % 2 * half, y + index / 2 * half, half, half));
        }
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

    /** An item's picture, by its id, or a 16 by 16 texture's, by its location ending in {@code .png}. */
    static Icon itemOrTexture(String id) {
        return id.endsWith(".png") ? texture(id) : item(id);
    }
}
