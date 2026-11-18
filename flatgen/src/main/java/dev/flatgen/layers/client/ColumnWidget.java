package dev.flatgen.layers.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.layers.rules.ColumnLayout;
import dev.flatgen.mc.Names;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;

/**
 * The editor's column: the preset's layers seen from the side, the bottom layer lowest, each as tall as its share of
 * the column ({@link ColumnLayout}), with the Y of the lines between them beside it and lines at the heights that
 * matter. A click selects a layer; the wheel scrolls a column too tall for its space.
 */
public final class ColumnWidget implements Widget {
    /** The thinnest a layer is drawn, in GUI pixels, so it stays visible and clickable. */
    private static final int THINNEST_LAYER = 8;
    /** How wide the layers are drawn, in GUI pixels: three blocks. */
    public static final int COLUMN_WIDTH = 48;
    /** How wide the space for the Y numbers beside the column is, in GUI pixels. */
    public static final int LABEL_WIDTH = 30;
    /** Space between the column and its Y numbers, in GUI pixels. */
    private static final int LABEL_GAP = 4;
    /** How far one notch of the wheel scrolls, in GUI pixels. */
    private static final int SCROLL_STEP = 16;
    /** The lines drawn across the column, by Y: slime chunks spawn below 40; cool biomes may snow above 80. */
    private static final List<Marker> MARKERS = List.of(new Marker(40, 0xFF7FD34F), new Marker(80, 0xFF8FD8FF));
    /** Length of each dash of a marker line, and of the gap after it, in GUI pixels. */
    private static final int DASH = 3;
    private static final int BACKGROUND = 0xFF101010;
    private static final int LINE_BETWEEN_LAYERS = 0x80000000;
    private static final int SELECTION = 0xFFFFFFFF;
    private static final int LABEL_COLOR = 0xFFE0E0E0;
    private static final int LEFT_BUTTON = 0;

    private final Preset preset;
    private final List<Integer> heights;
    private final int floor;
    private final int fluidColor;
    private final Rect column;
    private final ColumnLayout layout;
    private final IntConsumer onSelect;
    private int selected;
    private int scroll;

    /** A line across the column at a Y, in a colour. */
    private record Marker(int y, int color) {
    }

    /**
     * A column for a preset in a world whose floor is at this Y, drawn at the left of this space, its Y numbers to the
     * right of it.
     *
     * @param fluidColor the colour water layers are tinted, the biome's water colour
     * @param selected   the layer selected at first, bottom first, or -1 for none
     * @param onSelect   told the layer a click selects
     */
    public ColumnWidget(Preset preset, int floor, int fluidColor, Rect space, int selected, IntConsumer onSelect) {
        this.preset = preset;
        this.heights = preset.layers().stream().map(Layer::height).toList();
        this.floor = floor;
        this.fluidColor = fluidColor;
        this.column = new Rect(space.x(), space.y(), COLUMN_WIDTH, space.height());
        this.layout = ColumnLayout.of(heights, space.height(), THINNEST_LAYER);
        this.selected = selected;
        this.onSelect = onSelect;
    }

    /** Where each layer is drawn on the screen, bottom first; partly or wholly outside the column when scrolled. */
    public List<Rect> layerBounds() {
        List<Rect> bounds = new ArrayList<>();
        for (ColumnLayout.Span span : layout.spans()) {
            bounds.add(new Rect(column.x(), column.y() + span.top() - scroll, column.width(), span.height()));
        }
        return bounds;
    }

    /** The selected layer, bottom first, or -1 for none. */
    public int selected() {
        return selected;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.fill(column, BACKGROUND);
        List<Rect> bounds = layerBounds();
        canvas.clip(column);
        for (int index = 0; index < bounds.size(); index++) {
            canvas.blockSide(preset.layers().get(index).block(), fluidColor, bounds.get(index));
            canvas.fill(new Rect(column.x(), bounds.get(index).y(), column.width(), 1), LINE_BETWEEN_LAYERS);
        }
        MARKERS.forEach(marker -> drawMarker(canvas, marker));
        if (selected >= 0 && selected < bounds.size()) {
            canvas.outline(bounds.get(selected), SELECTION);
        }
        canvas.unclip();
        drawLabels(canvas, bounds);
        hoveredLayer(mouseX, mouseY).ifPresent(index -> canvas.tooltip(describe(index), mouseX, mouseY));
    }

    @Override
    public boolean click(double x, double y, int button) {
        OptionalInt layer = hoveredLayer(x, y);
        if (button != LEFT_BUTTON || layer.isEmpty()) {
            return false;
        }
        selected = layer.getAsInt();
        onSelect.accept(selected);
        return true;
    }

    @Override
    public boolean scroll(double x, double y, double amount) {
        int hidden = layout.contentHeight() - column.height();
        if (hidden <= 0 || !column.contains(x, y)) {
            return false;
        }
        scroll = Math.max(0, Math.min(hidden, scroll - (int) Math.signum(amount) * SCROLL_STEP));
        return true;
    }

    /** What a layer's tooltip says: its block, how deep it is and the Ys it covers. */
    public List<Component> describe(int index) {
        Layer layer = preset.layers().get(index);
        int bottomY = floor + heights.subList(0, index).stream().mapToInt(Integer::intValue).sum();
        return List.of(Names.block(layer.block()),
                Component.translatable("flatgen.layer.height", layer.height()),
                Component.translatable("flatgen.layer.y_range", bottomY, bottomY + layer.height() - 1));
    }

    private OptionalInt hoveredLayer(double x, double y) {
        return column.contains(x, y) ? layout.layerAt((int) y - column.y() + scroll) : OptionalInt.empty();
    }

    private void drawMarker(Canvas canvas, Marker marker) {
        OptionalInt pixel = layout.pixelOfLine(marker.y(), floor, heights);
        if (pixel.isEmpty()) {
            return;
        }
        int lineY = column.y() + pixel.getAsInt() - scroll;
        for (int x = column.x(); x < column.right(); x += 2 * DASH) {
            canvas.fill(new Rect(x, lineY, Math.min(DASH, column.right() - x), 1), marker.color());
        }
    }

    /**
     * The Y of the lines between layers beside the column: the surface and the world's floor always, the others where
     * they do not crowd a number already there.
     */
    private void drawLabels(Canvas canvas, List<Rect> bounds) {
        List<Integer> drawnTops = new ArrayList<>();
        for (Line line : surfaceAndFloorFirst(lines(bounds))) {
            int textY = line.pixel() - canvas.lineHeight() / 2;
            boolean visible = line.pixel() >= column.y() && line.pixel() <= column.bottom();
            boolean crowded = drawnTops.stream().anyMatch(top -> Math.abs(top - textY) < canvas.lineHeight());
            if (visible && !crowded) {
                canvas.text(Component.literal(Integer.toString(line.y())), column.right() + LABEL_GAP, textY,
                        LABEL_COLOR);
                drawnTops.add(textY);
            }
        }
    }

    /** The lines with the surface's and the floor's first, as they are labelled first. */
    private static List<Line> surfaceAndFloorFirst(List<Line> topDown) {
        if (topDown.size() < 2) {
            return topDown;
        }
        List<Line> ordered = new ArrayList<>();
        ordered.add(topDown.get(0));
        ordered.add(topDown.get(topDown.size() - 1));
        ordered.addAll(topDown.subList(1, topDown.size() - 1));
        return ordered;
    }

    /** The lines between layers, top down, the surface first and the world's floor last: each one's pixel row and Y. */
    private List<Line> lines(List<Rect> bounds) {
        List<Line> lines = new ArrayList<>();
        int y = floor + preset.height();
        for (int index = bounds.size() - 1; index >= 0; index--) {
            lines.add(new Line(bounds.get(index).y(), y));
            y -= heights.get(index);
        }
        if (!bounds.isEmpty()) {
            lines.add(new Line(bounds.get(0).bottom(), floor));
        }
        return lines;
    }

    /** A line between blocks: its pixel row on the screen and the Y of the block above it. */
    private record Line(int pixel, int y) {
    }
}
