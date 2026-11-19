package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A short fact on a dark pill over a picture: its own picture, then a few words, cut with an ellipsis where they would
 * run past its widest, with its top left corner at a point; it takes no input.
 *
 * @param icon     the fact's picture
 * @param text     the words, which may change
 * @param x        its left edge
 * @param y        its top edge
 * @param maxWidth the widest it is drawn, in GUI pixels
 */
public record Chip(Icon icon, Supplier<Component> text, int x, int y, int maxWidth) implements Widget {
    /** How far a chip sits in from the corner of what it is over, and how tall it is, in GUI pixels. */
    private static final int INSET = 6;
    private static final int HEIGHT = Icon.SIZE;
    /** Space after the words, in GUI pixels. */
    private static final int PADDING = 5;

    /** A chip in the top left corner of an area, such as the world's picture, as wide as the area leaves it. */
    public static Chip inCorner(Icon icon, Supplier<Component> text, Rect area) {
        return new Chip(icon, text, area.x() + INSET, area.y() + INSET, area.width() - 2 * INSET);
    }

    /** How far below an area's top what a chip in its corner leaves clear starts, in GUI pixels. */
    public static int clearBelow() {
        return INSET + HEIGHT + INSET / 2;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Component words = canvas.fitted(text.get(), maxWidth - Icon.SIZE - PADDING, TextStyle.BODY);
        canvas.sprite(Look.CHIP, new Rect(x, y, Icon.SIZE + canvas.textWidth(words) + PADDING, HEIGHT));
        icon.draw(canvas, x, y);
        canvas.text(words, x + Icon.SIZE, y + (HEIGHT - canvas.lineHeight()) / 2 + 1, Look.TEXT);
    }
}
