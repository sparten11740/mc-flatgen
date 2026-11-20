package dev.flatgen.layers.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.layers.rules.ColumnLayout;
import dev.flatgen.preset.rules.Layer;
import java.util.List;

/** A stack of layers drawn small, as the preset library's cards show a preset: each layer as tall as its share. */
public final class ColumnPicture {
    /** The thinnest a layer is drawn, in GUI pixels. */
    private static final int THINNEST_LAYER = 1;

    private ColumnPicture() {
    }

    /**
     * Draws the layers, the bottom one lowest, filling this rectangle; water tinted with this colour, and air left
     * clear, showing what is behind, such as a card's sky.
     */
    public static void draw(Canvas canvas, List<Layer> layers, int fluidColor, Rect area) {
        if (layers.isEmpty()) {
            return;
        }
        ColumnLayout layout = ColumnLayout.of(layers.stream().map(Layer::height).toList(), area.height(),
                THINNEST_LAYER);
        canvas.clip(area);
        for (int index = 0; index < layers.size(); index++) {
            ColumnLayout.Span span = layout.spans().get(index);
            canvas.blockSide(layers.get(index).block(), fluidColor,
                    new Rect(area.x(), area.y() + span.top(), area.width(), span.height()));
        }
        canvas.unclip();
    }
}
