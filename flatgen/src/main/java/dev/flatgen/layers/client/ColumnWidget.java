package dev.flatgen.layers.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Narration;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.Scrolling;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.layers.rules.ColumnLayout;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Biomes;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * The editor's column: the preset's layers seen from the side, the bottom layer lowest, each as tall as its share of
 * the column ({@link ColumnLayout}), with the Y of the lines between them beside it and lines at the heights that
 * matter. A click selects a layer; dragging a layer's top edge makes it deeper or shallower, dragging the layer moves
 * it, and the wheel over it changes its depth (Shift for 10 blocks, Control for 100). The wheel over the Y numbers
 * scrolls a column too tall for its space.
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
    /** How close to a layer's top edge a press starts resizing it rather than moving it, in GUI pixels. */
    private static final int EDGE = 2;
    /** How far the mouse goes with the button held before a press becomes a move, in GUI pixels. */
    private static final int MOVE_THRESHOLD = 3;
    /** Blocks one notch of the wheel adds or takes, alone, with Shift and with Control. */
    private static final int WHEEL_BLOCKS = 1;
    private static final int SHIFT_WHEEL_BLOCKS = 10;
    private static final int CONTROL_WHEEL_BLOCKS = 100;
    /** Length of each dash of a marker line, and of the gap after it, in GUI pixels. */
    private static final int DASH = 3;
    private static final int BACKGROUND = 0xFF101010;
    private static final int LINE_BETWEEN_LAYERS = 0x80000000;
    private static final int SELECTION = 0xFFFFFFFF;
    private static final int DROP_LINE = 0xFFFFD040;
    private static final int LABEL_COLOR = 0xFFE0E0E0;

    private final EditorContext editor;
    private final Supplier<List<Marker>> markers;
    private final Rect column;
    private final Rect labels;
    private Preset laidOut;
    private ColumnLayout layout;
    /** The biome {@link #fluidColor} is the water colour of, the colour water layers are tinted. */
    private String tintedBiome;
    private int fluidColor;
    private int scroll;
    private Drag drag = Drag.NONE;

    /**
     * A dashed line across the column at a height that matters, such as where slimes stop spawning, ending in the
     * picture of what it marks, so lines of different colours differ by more than their colour.
     *
     * @param y       the Y it is drawn at
     * @param color   its colour, ARGB
     * @param picture what it marks, drawn at its right end
     */
    public record Marker(int y, int color, Icon picture) {
    }

    /**
     * What a held button is doing: nothing, resizing a layer (from the Y it was pressed at, its depth then and blocks
     * per pixel of its drawing then), or about to move or moving one (to the place it would land).
     */
    private record Drag(Mode mode, double startY, int startHeight, double blocksPerPixel, int target) {
        static final Drag NONE = new Drag(Mode.NONE, 0, 0, 0, -1);

        Drag moving(int place) {
            return new Drag(Mode.MOVING, startY, startHeight, blocksPerPixel, place);
        }
    }

    /** The kinds of {@link Drag}. */
    private enum Mode { NONE, RESIZING, PRESSED, MOVING }

    /**
     * A column drawing what the editor shows, at the left of this space with its Y numbers to the right of it.
     *
     * @param markers the lines to draw across it now, which change with the preset
     */
    public ColumnWidget(EditorContext editor, Rect space, Supplier<List<Marker>> markers) {
        this.editor = editor;
        this.markers = markers;
        this.column = new Rect(space.x(), space.y(), COLUMN_WIDTH, space.height());
        this.labels = new Rect(column.right(), space.y(), space.width() - COLUMN_WIDTH, space.height());
    }

    /** Where each layer is drawn on the screen, bottom first; partly or wholly outside the column when scrolled. */
    public List<Rect> layerBounds() {
        List<Rect> bounds = new ArrayList<>();
        for (ColumnLayout.Span span : layout().spans()) {
            bounds.add(new Rect(column.x(), column.y() + span.top() - scroll, column.width(), span.height()));
        }
        return bounds;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        EditorState state = editor.shown();
        List<Rect> bounds = layerBounds();
        canvas.fill(column, BACKGROUND);
        canvas.clip(column);
        int fluid = fluidColor(state.preset().biome());
        for (int index = 0; index < bounds.size(); index++) {
            canvas.blockSide(state.preset().layers().get(index).block(), fluid, bounds.get(index));
            canvas.fill(new Rect(column.x(), bounds.get(index).y(), column.width(), 1), LINE_BETWEEN_LAYERS);
        }
        markers.get().forEach(marker -> drawMarker(canvas, marker));
        if (state.selected() >= 0) {
            canvas.outline(bounds.get(state.selected()), SELECTION);
        }
        if (drag.mode() == Mode.MOVING) {
            canvas.fill(dropLine(bounds, state.selected(), drag.target()), DROP_LINE);
        }
        canvas.unclip();
        drawLabels(canvas, bounds);
        OptionalInt hovered = hoveredLayer(mouseX, mouseY);
        if (drag.mode() == Mode.NONE && hovered.isPresent()) {
            canvas.tooltip(LayerText.describe(editor.shown(), editor.world(), hovered.getAsInt()), mouseX, mouseY);
        }
    }

    @Override
    public List<Widget> focusables() {
        return List.of(this);
    }

    /** The selected layer: its block, its depth and the Ys it covers. */
    @Override
    public Optional<Narration> narration() {
        int selected = editor.shown().selected();
        if (selected < 0) {
            return Optional.of(Narration.entry(Component.translatable("flatgen.editor.empty"), List.of()));
        }
        return Optional.of(Narration.entry(LayerText.describe(editor.shown(), editor.world(), selected)));
    }

    /** The selected layer's block, depth and Ys, shown while the column has the focus. */
    @Override
    public List<Component> tooltip() {
        int selected = editor.shown().selected();
        return selected < 0 ? List.of(Component.translatable("flatgen.editor.empty"))
                : LayerText.describe(editor.shown(), editor.world(), selected);
    }

    /** The layer under the mouse, as its tooltip says it. */
    @Override
    public Optional<Narration> narrationAt(double x, double y) {
        OptionalInt layer = hoveredLayer(x, y);
        return layer.isEmpty() ? Optional.empty()
                : Optional.of(Narration.entry(LayerText.describe(editor.shown(), editor.world(), layer.getAsInt())));
    }

    /** The focus ring goes round the selected layer, or the column when it has none. */
    @Override
    public Optional<Rect> focusArea() {
        int selected = editor.shown().selected();
        return Optional.of(selected >= 0 ? layerBounds().get(selected) : column);
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        OptionalInt layer = hoveredLayer(x, y);
        if (button != MouseButtons.LEFT || layer.isEmpty()) {
            return false;
        }
        int index = layer.getAsInt();
        editor.select(index);
        Rect bounds = layerBounds().get(index);
        int height = editor.shown().preset().layers().get(index).height();
        Mode mode = y < bounds.y() + EDGE ? Mode.RESIZING : Mode.PRESSED;
        drag = new Drag(mode, y, height, (double) height / bounds.height(), -1);
        return true;
    }

    @Override
    public void drag(double x, double y) {
        if (drag.mode() == Mode.RESIZING) {
            int blocks = (int) Math.round((drag.startY() - y) * drag.blocksPerPixel());
            editor.preview(new LayerEdits.SetHeight(drag.startHeight() + blocks));
        } else if (drag.mode() == Mode.MOVING
                || (drag.mode() == Mode.PRESSED && Math.abs(y - drag.startY()) > MOVE_THRESHOLD)) {
            drag = drag.moving(placeAt(y));
        }
    }

    @Override
    public void release(double x, double y) {
        if (drag.mode() == Mode.RESIZING) {
            editor.commitPreview();
        } else if (drag.mode() == Mode.MOVING) {
            editor.apply(new LayerEdits.Move(placeAt(y)));
        }
        drag = Drag.NONE;
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        OptionalInt layer = hoveredLayer(x, y);
        if (layer.isPresent()) {
            editor.select(layer.getAsInt());
            editor.apply(new LayerEdits.ChangeHeight((int) Math.signum(amount) * wheelBlocks(modifiers)));
            return true;
        }
        int hidden = layout().contentHeight() - column.height();
        if (hidden <= 0 || !labels.contains(x, y)) {
            return false;
        }
        scroll = Scrolling.scrolled(scroll, amount, Scrolling.WHEEL_STEP, hidden);
        return true;
    }

    private static int wheelBlocks(Modifiers modifiers) {
        return modifiers.control() ? CONTROL_WHEEL_BLOCKS : modifiers.shift() ? SHIFT_WHEEL_BLOCKS : WHEEL_BLOCKS;
    }

    /**
     * The layout of what is shown now, laid out again when the preset changes, with the scroll kept within it, so a
     * column that shrank to fit shows its top again.
     */
    private ColumnLayout layout() {
        Preset preset = editor.shown().preset();
        if (!preset.equals(laidOut)) {
            laidOut = preset;
            layout = ColumnLayout.of(heights(preset), column.height(), THINNEST_LAYER);
            scroll = Math.max(0, Math.min(layout.contentHeight() - column.height(), scroll));
        }
        return layout;
    }

    /** The water colour of the biome shown, looked up again only when the biome changes. */
    private int fluidColor(String biome) {
        if (!biome.equals(tintedBiome)) {
            tintedBiome = biome;
            fluidColor = Biomes.waterColor(biome, editor.registries());
        }
        return fluidColor;
    }

    private OptionalInt hoveredLayer(double x, double y) {
        return column.contains(x, y) ? layout().layerAt((int) y - column.y() + scroll) : OptionalInt.empty();
    }

    /** The place a moved layer lands when let go at this row: that of the layer there, or the nearest end. */
    private int placeAt(double y) {
        int pixel = (int) y - column.y() + scroll;
        int last = layout().spans().size() - 1;
        return layout().layerAt(pixel).orElse(pixel < 0 ? last : 0);
    }

    /** The line showing where a moved layer lands: above the layer at that place when moving up, below it if down. */
    private Rect dropLine(List<Rect> bounds, int from, int to) {
        Rect target = bounds.get(to);
        int lineY = to > from ? target.y() : target.bottom() - 1;
        return new Rect(column.x(), lineY - 1, column.width(), 2);
    }

    private void drawMarker(Canvas canvas, Marker marker) {
        OptionalInt pixel = layout().pixelOfLine(marker.y(), editor.world().floor(), heights(editor.shown().preset()));
        if (pixel.isEmpty()) {
            return;
        }
        int lineY = column.y() + pixel.getAsInt() - scroll;
        for (int x = column.x(); x < column.right(); x += 2 * DASH) {
            canvas.fill(new Rect(x, lineY, Math.min(DASH, column.right() - x), 1), marker.color());
        }
        marker.picture().draw(canvas, column.right() - Icon.SIZE - 1, lineY - Icon.SIZE / 2);
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
                canvas.text(Component.literal(Integer.toString(line.y())), labels.x() + LABEL_GAP, textY, LABEL_COLOR);
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
        Preset preset = editor.shown().preset();
        List<Line> lines = new ArrayList<>();
        int y = editor.world().floor() + preset.height();
        for (int index = bounds.size() - 1; index >= 0; index--) {
            lines.add(new Line(bounds.get(index).y(), y));
            y -= preset.layers().get(index).height();
        }
        if (!bounds.isEmpty()) {
            lines.add(new Line(bounds.get(0).bottom(), editor.world().floor()));
        }
        return lines;
    }

    private static List<Integer> heights(Preset preset) {
        return preset.layers().stream().map(Layer::height).toList();
    }

    /** A line between blocks: its pixel row on the screen and the Y of the block above it. */
    private record Line(int pixel, int y) {
    }
}
