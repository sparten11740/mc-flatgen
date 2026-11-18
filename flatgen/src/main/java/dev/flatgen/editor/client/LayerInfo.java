package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/** The editor's panel beside the column: what is selected, a line each, then the world's surface and biome. */
final class LayerInfo implements Widget {
    /** Space between lines, in GUI pixels, on top of a line's own height. */
    private static final int LINE_GAP = 3;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final Rect bounds;
    private final Supplier<List<Component>> lines;

    LayerInfo(Rect bounds, Supplier<List<Component>> lines) {
        this.bounds = bounds;
        this.lines = lines;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        int y = bounds.y();
        for (Component line : lines.get()) {
            canvas.text(line, bounds.x(), y, TEXT_COLOR);
            y += canvas.lineHeight() + LINE_GAP;
        }
    }
}
