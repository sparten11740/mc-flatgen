package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A line of text in one of the text styles, such as a heading or a caption, at the left or in the middle of its
 * rectangle and in the middle of its height; it takes no input.
 *
 * @param text     the words, which may change
 * @param bounds   where they are drawn
 * @param style    how they are drawn
 * @param color    their colour, ARGB
 * @param centered whether they are in the middle of the rectangle rather than at its left
 */
public record Caption(Supplier<Component> text, Rect bounds, TextStyle style, int color, boolean centered)
        implements Widget {
    /** Words at the left of the rectangle. */
    public Caption(Supplier<Component> text, Rect bounds, TextStyle style, int color) {
        this(text, bounds, style, color, false);
    }

    /** The words, cut with an ellipsis where they would run past the rectangle's width. */
    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Component words = canvas.fitted(text.get(), bounds.width(), style);
        if (centered) {
            canvas.centeredText(words, bounds, color, style);
            return;
        }
        canvas.text(words, bounds.x(), style.top(bounds.y(), bounds.height()), color, style);
    }
}
