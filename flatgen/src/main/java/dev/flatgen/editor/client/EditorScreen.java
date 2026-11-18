package dev.flatgen.editor.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.NumberField;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.RowLayout;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.History;
import dev.flatgen.editor.rules.World;
import dev.flatgen.layers.client.ColumnActions;
import dev.flatgen.layers.client.ColumnWidget;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.Names;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Flatgen's superflat editor, opened by Customize: the world's layers in a column, beside it what is selected and the
 * controls that change it, and Done, the game's own editor and Cancel below. Every change is a step to undo. Done
 * applies the preset to the Create World screen; Cancel leaves it.
 */
public final class EditorScreen extends FlatgenScreen {
    /** Space above the column for the title, in GUI pixels. */
    private static final int HEADER = 24;
    /** Space below the column for the footer's buttons, in GUI pixels. */
    private static final int FOOTER = 32;
    /** Space at the sides and between the column and the panel, in GUI pixels. */
    private static final int MARGIN = 12;
    /** A footer button's size, in GUI pixels: three fit the narrowest screen at the largest GUI scale. */
    private static final int FOOTER_BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    /** Space between buttons and between the panel's rows, in GUI pixels. */
    private static final int GAP = 4;
    /** Widths of the panel's controls, in GUI pixels. */
    private static final int SMALL = 20;
    private static final int QUICK = 22;
    private static final int FIELD = 40;
    private static final int WIDE = 70;
    /** How many lines the panel's text has: the selected layer's block and Ys, and the surface and biome. */
    private static final int INFO_LINES = 3;
    /** The depths a single click sets the selected layer to. */
    private static final List<Integer> QUICK_HEIGHTS = List.of(1, 2, 3, 5, 10, 50, 100);
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    private final World world;
    private final History history;
    private final Actions actions = new Actions();
    private Edit previewed;
    private ColumnWidget column;

    public EditorScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("flatgen.editor.title"));
        this.parent = parent;
        this.context = context;
        this.world = new World(WorldCreation.overworldType(context).minY(),
                WorldCreation.overworldType(context).height());
        Preset preset = FlatSettings.toPreset(WorldCreation.flatSettings(context), context.worldgenLoadContext(), "",
                ICON).preset();
        this.history = new History(new EditorState(preset, preset.layers().size() - 1));
    }

    /** The preset the editor shows, with a change being dragged made on it. */
    public Preset preset() {
        return actions.shown().preset();
    }

    /** The Y of the world's floor, where the bottom layer starts. */
    public int worldBottom() {
        return world.floor();
    }

    /** The column, as last laid out. */
    public ColumnWidget column() {
        return column;
    }

    /** The selected layer, bottom first, or -1 when there are none. */
    public int selected() {
        return actions.shown().selected();
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        Rect space = new Rect(MARGIN, HEADER, ColumnWidget.COLUMN_WIDTH + ColumnWidget.LABEL_WIDTH,
                height - HEADER - FOOTER);
        column = new ColumnWidget(actions, world, Biomes.waterColor(preset().biome(), context.worldgenLoadContext()),
                space);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(column);
        widgets.addAll(panel(space.right() + MARGIN, width - space.right() - 2 * MARGIN));
        widgets.addAll(footer(width, height));
        return widgets;
    }

    @Override
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        int selected = selected();
        Runnable action = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> modifiers.alt() ? () -> apply(new LayerEdits.Move(selected + 1))
                    : () -> history.select(selected + 1);
            case GLFW.GLFW_KEY_DOWN -> modifiers.alt() ? () -> apply(new LayerEdits.Move(selected - 1))
                    : () -> history.select(Math.max(0, selected - 1));
            case GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> () -> apply(new LayerEdits.Remove());
            case GLFW.GLFW_KEY_INSERT -> () -> apply(new LayerEdits.Add(true));
            case GLFW.GLFW_KEY_D -> modifiers.control() ? () -> apply(new LayerEdits.Duplicate()) : null;
            case GLFW.GLFW_KEY_Z -> modifiers.control() ? (modifiers.shift() ? history::redo : history::undo) : null;
            case GLFW.GLFW_KEY_Y -> modifiers.control() ? history::redo : null;
            default -> null;
        };
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** The panel beside the column, its rows top down, this wide from this left edge. */
    private List<Widget> panel(int left, int width) {
        List<Widget> widgets = new ArrayList<>();
        int infoHeight = TextLines.height(INFO_LINES, font.lineHeight);
        widgets.add(new TextLines(new Rect(left, HEADER, width, infoHeight), this::info));
        int rowY = HEADER + infoHeight + GAP;
        widgets.addAll(heightRow(new RowLayout(left, rowY, BUTTON_HEIGHT, GAP)));
        rowY += BUTTON_HEIGHT + GAP;
        widgets.addAll(surfaceRow(new RowLayout(left, rowY, BUTTON_HEIGHT, GAP)));
        rowY += BUTTON_HEIGHT + GAP;
        widgets.addAll(layerRow(new RowLayout(left, rowY, BUTTON_HEIGHT, GAP)));
        rowY += BUTTON_HEIGHT + GAP;
        widgets.addAll(historyRow(new RowLayout(left, rowY, BUTTON_HEIGHT, GAP)));
        return widgets;
    }

    /** Shallower, the depth to type, deeper, then a button for each quick depth. */
    private List<Widget> heightRow(RowLayout row) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(button("−", row.next(SMALL), new LayerEdits.ChangeHeight(-1),
                () -> hasLayer() && selectedHeight() > 1, () -> tip("flatgen.layer.shallower")));
        widgets.add(new NumberField("height", row.next(FIELD), this::selectedHeight,
                height -> apply(new LayerEdits.SetHeight(height))));
        widgets.add(button("+", row.next(SMALL), new LayerEdits.ChangeHeight(1),
                () -> hasLayer() && selectedHeight() < room(), this::deeperTip));
        for (int quick : QUICK_HEIGHTS) {
            widgets.add(button(Integer.toString(quick), row.next(QUICK), new LayerEdits.SetHeight(quick),
                    this::hasLayer, List::of));
        }
        return widgets;
    }

    /** The surface's Y to type: the selected layer's depth changes so the surface is there. */
    private List<Widget> surfaceRow(RowLayout row) {
        Component label = Component.translatable("flatgen.editor.surface_field");
        Rect labelPlace = row.next(font.width(label));
        Rect text = new Rect(labelPlace.x(), labelPlace.y() + (BUTTON_HEIGHT - font.lineHeight) / 2,
                labelPlace.width(), font.lineHeight);
        return List.of(new TextLines(text, () -> List.of(label)),
                new NumberField("surface", row.next(FIELD), () -> world.floor() + preset().height(),
                        y -> apply(new LayerEdits.SurfaceAt(y))));
    }

    /** Add a layer above, add one below, copy the selected layer, remove it. */
    private List<Widget> layerRow(RowLayout row) {
        Supplier<List<Component>> fullTip = () -> roomLeft() > 0 ? List.of() : List.of(full());
        return List.of(
                button(Component.translatable("flatgen.editor.add_above"), row.next(WIDE), new LayerEdits.Add(true),
                        () -> roomLeft() > 0, fullTip),
                button(Component.translatable("flatgen.editor.add_below"), row.next(WIDE), new LayerEdits.Add(false),
                        () -> roomLeft() > 0, fullTip),
                button(Component.translatable("flatgen.editor.duplicate"), row.next(WIDE), new LayerEdits.Duplicate(),
                        () -> hasLayer() && roomLeft() > 0, fullTip),
                button(Component.translatable("flatgen.editor.remove"), row.next(WIDE), new LayerEdits.Remove(),
                        this::hasLayer, List::of));
    }

    /** Undo and redo. */
    private List<Widget> historyRow(RowLayout row) {
        return List.of(
                new TextButton(Component.translatable("flatgen.editor.undo"), row.next(WIDE), history::undo,
                        history::canUndo, List::of),
                new TextButton(Component.translatable("flatgen.editor.redo"), row.next(WIDE), history::redo,
                        history::canRedo, List::of));
    }

    private List<Widget> footer(int width, int height) {
        RowLayout row = new RowLayout((width - 3 * FOOTER_BUTTON_WIDTH - 2 * 2 * GAP) / 2,
                height - (FOOTER + BUTTON_HEIGHT) / 2, BUTTON_HEIGHT, 2 * GAP);
        return List.of(
                new TextButton(CommonComponents.GUI_DONE, row.next(FOOTER_BUTTON_WIDTH), this::done),
                new TextButton(Component.translatable("flatgen.editor.vanilla"), row.next(FOOTER_BUTTON_WIDTH),
                        this::openVanilla),
                new TextButton(CommonComponents.GUI_CANCEL, row.next(FOOTER_BUTTON_WIDTH), this::onClose));
    }

    private TextButton button(String label, Rect place, Edit edit, BooleanSupplier active,
            Supplier<List<Component>> tooltip) {
        return button(Component.literal(label), place, edit, active, tooltip);
    }

    private TextButton button(Component label, Rect place, Edit edit, BooleanSupplier active,
            Supplier<List<Component>> tooltip) {
        return new TextButton(label, place, () -> apply(edit), active, tooltip);
    }

    /** What the panel's text says: the selected layer's block and Ys, then the surface and the biome. */
    private List<Component> info() {
        List<Component> lines = new ArrayList<>();
        if (hasLayer()) {
            List<Component> layer = column.describe(selected());
            lines.add(layer.get(0));
            lines.add(layer.get(2));
        } else {
            lines.add(Component.translatable("flatgen.editor.empty"));
            lines.add(Component.empty());
        }
        lines.add(Component.translatable("flatgen.editor.surface", world.floor() + preset().height(),
                Names.biome(preset().biome())));
        return lines;
    }

    private List<Component> tip(String key) {
        return List.of(Component.translatable(key));
    }

    private List<Component> deeperTip() {
        return hasLayer() && selectedHeight() >= room() ? List.of(full()) : tip("flatgen.layer.deeper");
    }

    private Component full() {
        return Component.translatable("flatgen.editor.full", world.height());
    }

    private boolean hasLayer() {
        return selected() >= 0;
    }

    private int selectedHeight() {
        return hasLayer() ? preset().layers().get(selected()).height() : 0;
    }

    private int room() {
        return LayerEdits.room(actions.shown(), world);
    }

    private int roomLeft() {
        return world.height() - preset().height();
    }

    private void apply(Edit edit) {
        history.apply(edit, world);
    }

    private void done() {
        WorldCreation.applyFlatSettings(parent,
                FlatSettings.toSettings(history.current().preset(), context.worldgenLoadContext()));
        onClose();
    }

    private void openVanilla() {
        minecraft.setScreen(PresetEditors.VANILLA.createEditScreen(parent, context));
    }

    /** What the column asks of the editor: the history, with a dragged change shown on it until it is let go. */
    private final class Actions implements ColumnActions {
        @Override
        public EditorState shown() {
            return previewed == null ? history.current() : previewed.apply(history.current(), world);
        }

        @Override
        public void select(int layer) {
            history.select(layer);
        }

        @Override
        public void apply(Edit edit) {
            history.apply(edit, world);
        }

        @Override
        public void preview(Edit edit) {
            previewed = edit;
        }

        @Override
        public void commitPreview() {
            if (previewed != null) {
                history.apply(previewed, world);
                previewed = null;
            }
        }
    }
}
