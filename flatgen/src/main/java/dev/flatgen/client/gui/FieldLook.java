package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import net.minecraft.network.chat.Component;

/** How Flatgen's text and number fields look: a rounded box, lit while it has the keyboard, and its text. */
final class FieldLook {
    /** Space between the box's edge and its text, in GUI pixels. */
    private static final int PADDING = 4;

    private FieldLook() {
    }

    /** Draws a field's box and its text. */
    static void draw(Canvas canvas, Rect bounds, boolean lit, Component text, int color) {
        canvas.field(bounds, lit);
        canvas.text(text, bounds.x() + PADDING, TextStyle.BODY.top(bounds.y(), bounds.height()), color);
    }

    /**
     * Draws a field square and flat, filling the area with the edge of the box it stands in along its top and bottom,
     * outlined in the accent colour while it has the keyboard, and its text in the middle of it.
     */
    static void drawFlat(Canvas canvas, Rect bounds, boolean lit, Component text, int color) {
        canvas.sprite(Look.FIELD_BAND, bounds);
        if (lit) {
            canvas.outline(bounds, Look.ACCENT);
        }
        canvas.centeredText(text, bounds, color);
    }

    /** Draws a field's box and its text in the middle of it, as a number's. */
    static void drawCentred(Canvas canvas, Rect bounds, boolean lit, Component text, int color) {
        canvas.field(bounds, lit);
        canvas.centeredText(text, bounds, color);
    }
}
