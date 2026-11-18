package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import net.minecraft.network.chat.Component;

/** How Flatgen's text and number fields look, as the game's own: a box, lit while it has the keyboard, and its text. */
final class FieldLook {
    /** The colour of a field's text. */
    static final int TEXT_COLOR = 0xFFE0E0E0;
    /** The colour of a hint shown in an empty field. */
    static final int HINT_COLOR = 0xFF808080;
    private static final String SPRITE = "minecraft:widget/text_field";
    private static final String LIT_SPRITE = "minecraft:widget/text_field_highlighted";
    /** Space between the box's edge and its text, in GUI pixels. */
    private static final int PADDING = 4;

    private FieldLook() {
    }

    /** Draws a field's box and its text. */
    static void draw(Canvas canvas, Rect bounds, boolean lit, Component text, int color) {
        canvas.sprite(lit ? LIT_SPRITE : SPRITE, bounds);
        canvas.text(text, bounds.x() + PADDING, bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1, color);
    }
}
