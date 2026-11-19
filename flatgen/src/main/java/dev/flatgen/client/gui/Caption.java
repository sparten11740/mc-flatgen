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
    /** How far above and below its rectangle a caption may draw, in GUI pixels, so tall letters keep their tops. */
    private static final int CLIP_SLACK = 2;

    /** Words at the left of the rectangle. */
    public Caption(Supplier<Component> text, Rect bounds, TextStyle style, int color) {
        this(text, bounds, style, color, false);
    }

    /** The words, cut off at the rectangle's edges so a long name never runs over what is beside it. */
    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.clip(new Rect(bounds.x(), bounds.y() - CLIP_SLACK, bounds.width(), bounds.height() + 2 * CLIP_SLACK));
        if (centered) {
            canvas.centeredText(text.get(), bounds, color, style);
        } else {
            canvas.text(text.get(), bounds.x(), bounds.y() + (bounds.height() - style.lineHeight()) / 2 + 1, color,
                    style);
        }
        canvas.unclip();
    }
}
