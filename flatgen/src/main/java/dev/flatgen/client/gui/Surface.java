package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A sprite drawn behind other widgets, such as the raised panel a group of controls sits on; it takes no input.
 *
 * @param sprite the sprite's id, one of {@link Look}'s
 * @param bounds where it is drawn
 */
public record Surface(String sprite, Rect bounds) implements Widget {
    /** Space inside a box around what it holds, in GUI pixels. */
    private static final int BOX_PADDING = 6;

    /** A box on a raised panel across a flow's width, below what it placed, holding what the content places in it. */
    public static List<Widget> box(FlowLayout flow, Function<FlowLayout, List<Widget>> content) {
        FlowLayout inside = flow.boxed(BOX_PADDING);
        List<Widget> placed = content.apply(inside);
        List<Widget> widgets = new ArrayList<>(List.of(new Surface(Look.PANEL, flow.endBox(inside, BOX_PADDING))));
        widgets.addAll(placed);
        return widgets;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.sprite(sprite, bounds);
    }
}
