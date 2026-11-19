package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import net.minecraft.network.chat.Component;

/** A picture with words beside it, such as a group's heading, on one line of its rectangle. */
public final class Label implements Widget {
    /** Space between the picture and the words, in GUI pixels. */
    private static final int GAP = 4;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final Icon icon;
    private final Component text;
    private final Rect bounds;

    public Label(Icon icon, Component text, Rect bounds) {
        this.icon = icon;
        this.text = text;
        this.bounds = bounds;
    }

    /** How wide a label is whose words are this wide, in GUI pixels. */
    public static int width(int textWidth) {
        return Icon.SIZE + GAP + textWidth;
    }

    /** How tall a label is, in GUI pixels: its picture. */
    public static int height() {
        return Icon.SIZE;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        icon.draw(canvas, bounds.x(), bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        canvas.text(text, bounds.x() + Icon.SIZE + GAP, bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1,
                TEXT_COLOR);
    }
}
