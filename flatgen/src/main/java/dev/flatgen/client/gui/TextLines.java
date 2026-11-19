package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/** Lines of text from the top of a rectangle down, which may change from frame to frame. */
public final class TextLines implements Widget {
    /** Space between lines, in GUI pixels, on top of a line's own height. */
    private static final int LINE_GAP = 3;

    private final Rect bounds;
    private final Supplier<List<Component>> lines;

    /** How tall this many lines are drawn, in GUI pixels, for a line height. */
    public static int height(int count, int lineHeight) {
        return count * (lineHeight + LINE_GAP);
    }

    public TextLines(Rect bounds, Supplier<List<Component>> lines) {
        this.bounds = bounds;
        this.lines = lines;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        int y = bounds.y();
        for (Component line : lines.get()) {
            canvas.text(line, bounds.x(), y, Look.TEXT);
            y += canvas.lineHeight() + LINE_GAP;
        }
    }
}
