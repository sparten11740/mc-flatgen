package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import net.minecraft.network.chat.Component;

/** How Flatgen's text and number fields look, as the game's own: a box, lit while it has the keyboard, and its text. */
final class FieldLook {
    /** Space between the box's edge and its text, in GUI pixels. */
    private static final int PADDING = 4;

    private FieldLook() {
    }

    /** Draws a field's box and its text. */
    static void draw(Canvas canvas, Rect bounds, boolean lit, Component text, int color) {
        canvas.field(bounds, lit);
        canvas.text(text, bounds.x() + PADDING, bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1, color);
    }
}
