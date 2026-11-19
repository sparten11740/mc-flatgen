package dev.flatgen.editor.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.WidgetGroup;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.dimensions.client.DimensionTabs;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.History;
import dev.flatgen.editor.rules.World;
import dev.flatgen.facts.client.FactMarkers;
import dev.flatgen.layers.client.ColumnWidget;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.options.InstalledOptions;
import dev.flatgen.options.OptionSessions;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Preset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Flatgen's superflat editor, opened by Customize: the dimensions' tabs above a column of the edited stack's layers,
 * beside it the panels of {@link EditorPanels} that show for it, top down, wrapping within the space left and
 * scrolling when taller than it, and Done, the game's own editor and Cancel below. Every change is a step to undo.
 * Done applies the preset to the Create World screen; Cancel leaves it.
 */
public final class EditorScreen extends FlatgenScreen {
    /** Space above the tabs and the panel for the title, in GUI pixels. */
    private static final int HEADER = 24;
    /** Space below the column for the footer's buttons, in GUI pixels. */
    private static final int FOOTER = 32;
    /** Space between the dimensions' tabs and the column, in GUI pixels: room for half the column's top Y label. */
    private static final int TABS_GAP = 5;
    /** Space at the sides and between the column and the panel, in GUI pixels. */
    private static final int MARGIN = 12;
    /** A footer button's size, in GUI pixels: three fit the narrowest screen at the largest GUI scale. */
    private static final int FOOTER_BUTTON_WIDTH = 100;
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    /** Each dimension's world, which its stack stands in. */
    private final Map<Dimension, World> worlds = new EnumMap<>(Dimension.class);
    private final History history;
    /** The world options installed, read once as the editor opens. */
    private final List<WorldOption> worldOptions;
    private final Context editor = new Context();
    private Edit previewed;
    /** What the last copy or paste said, until the next change; null for nothing. */
    private Component message;
    private ColumnWidget column;
    /** What the widgets were last laid out for: the edited dimension and whether it has a stack. */
    private List<Object> laidOutFor = List.of();

    public EditorScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("flatgen.editor.title"));
        this.parent = parent;
        this.context = context;
        for (Dimension dimension : Dimension.values()) {
            DimensionType type = WorldCreation.dimensionType(context, dimension);
            worlds.put(dimension, new World(type.minY(), type.height()));
        }
        this.worldOptions = InstalledOptions.load();
        Preset overworld = FlatSettings.toPreset(WorldCreation.flatSettings(context), context.worldgenLoadContext(), "",
                ICON).preset();
        Preset preset = OptionSessions.shown(parent, overworld.withOtherStacks(dimension -> WorldCreation.flatSettings(
                context, dimension).map(settings -> FlatSettings.toStack(settings, context.worldgenLoadContext()))),
                worldOptions);
        this.history = new History(EditorState.of(preset));
    }

    /** What the editor shows, with a change being dragged made on it. */
    public EditorState shown() {
        return editor.shown();
    }

    /** The Y of the edited dimension's floor, where the bottom layer starts. */
    public int worldBottom() {
        return world().floor();
    }

    /** The column, as last laid out. */
    public ColumnWidget column() {
        return column;
    }

    /**
     * The tabs of the dimensions above the column, the column of the edited stack (an empty area for a dimension
     * without one, so the keyboard's focus keeps its place among the areas), the panels that show for it beside, and
     * the footer.
     */
    @Override
    protected List<Widget> layOut(int width, int height) {
        Rect tabs = new Rect(MARGIN, HEADER, ColumnWidget.COLUMN_WIDTH + ColumnWidget.LABEL_WIDTH,
                DimensionTabs.HEIGHT);
        int columnTop = tabs.bottom() + TABS_GAP;
        Rect space = new Rect(MARGIN, columnTop, tabs.width(), height - columnTop - FOOTER);
        column = new ColumnWidget(editor, space, FactMarkers.of(editor));
        laidOutFor = layoutKey();
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new WidgetGroup(DimensionTabs.layOut(editor, tabs)));
        widgets.add(editor.shown().stack().isPresent() ? column : new WidgetGroup(List.of()));
        int left = space.right() + MARGIN;
        Rect panelSpace = new Rect(left, HEADER, width - left - MARGIN, space.bottom() - HEADER);
        widgets.add(new ScrollGroup(panelSpace, top -> panels(new Rect(left, top, panelSpace.width(), 0))));
        widgets.add(new WidgetGroup(footer(width, height)));
        return widgets;
    }

    /** Every panel's widgets, from the top of this area down within its width. */
    private ScrollGroup.Laid panels(Rect area) {
        FlowLayout flow = new FlowLayout(area, PanelSizes.ROW_HEIGHT, PanelSizes.GAP);
        List<Widget> widgets = new ArrayList<>();
        shownPanels().forEach(panel -> widgets.addAll(panel.layOut(editor, flow)));
        return new ScrollGroup.Laid(widgets, flow.bottom());
    }

    @Override
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        for (EditorPanel panel : shownPanels()) {
            if (panel.shortcut(editor, keyCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /** Files dropped on the editor go to the first panel that takes them. */
    @Override
    protected void filesDropped(List<Path> files) {
        for (EditorPanel panel : shownPanels()) {
            if (panel.filesDropped(editor, files)) {
                return;
            }
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** The panels that show for what the editor holds now. */
    private List<EditorPanel> shownPanels() {
        return EditorPanels.ALL.stream().filter(panel -> panel.shows(editor.shown())).toList();
    }

    /** The edited dimension's world. */
    private World world() {
        return worlds.get(history.current().edited());
    }

    /** What decides the widgets: the edited dimension, and whether it has a stack. */
    private List<Object> layoutKey() {
        return List.of(history.current().edited(), history.current().stack().isPresent());
    }

    /**
     * Lays the widgets out again when a change decides other ones, as choosing a dimension does, dropping a drag's
     * preview, as the widget dragging is gone.
     */
    private void layOutAgainWhenNeeded() {
        if (!layoutKey().equals(laidOutFor)) {
            previewed = null;
            rebuildWidgets();
        }
    }

    private List<Widget> footer(int width, int height) {
        int gap = 2 * PanelSizes.GAP;
        int rowWidth = 3 * FOOTER_BUTTON_WIDTH + 2 * gap;
        FlowLayout row = new FlowLayout(new Rect((width - rowWidth) / 2, height - (FOOTER + PanelSizes.ROW_HEIGHT) / 2,
                rowWidth, 0), PanelSizes.ROW_HEIGHT, gap);
        return List.of(
                new TextButton(Icon.TICK, CommonComponents.GUI_DONE, row.next(FOOTER_BUTTON_WIDTH), this::done),
                new TextButton(Icon.item("minecraft:command_block"), Component.translatable("flatgen.editor.vanilla"),
                        row.next(FOOTER_BUTTON_WIDTH), this::openVanilla),
                new TextButton(Icon.CROSS, CommonComponents.GUI_CANCEL, row.next(FOOTER_BUTTON_WIDTH), this::onClose));
    }

    private void done() {
        OptionsAtDone.apply(parent, history.current().preset(), context.worldgenLoadContext(), worldOptions);
        onClose();
    }

    /** Opens the game's own editor on the layers as Flatgen has them now; its Cancel leaves the world as it was. */
    private void openVanilla() {
        FlatLevelGeneratorSettings edited = FlatSettings.toSettings(history.current().preset(),
                context.worldgenLoadContext());
        minecraft.setScreen(PresetEditors.VANILLA.createEditScreen(parent,
                WorldCreation.withFlatSettings(context, edited)));
    }

    /** What the column and the panels see of the editor: its history, with a dragged change shown until let go. */
    private final class Context implements EditorContext {
        @Override
        public EditorState shown() {
            return previewed == null ? history.current() : previewed.apply(history.current(), world());
        }

        @Override
        public World world() {
            return EditorScreen.this.world();
        }

        @Override
        public World world(Dimension dimension) {
            return worlds.get(dimension);
        }

        @Override
        public void apply(Edit edit) {
            history.apply(edit, world());
            message = null;
            layOutAgainWhenNeeded();
        }

        @Override
        public void select(int layer) {
            history.select(layer);
        }

        @Override
        public void choose(Dimension dimension) {
            history.choose(dimension);
            layOutAgainWhenNeeded();
        }

        @Override
        public void preview(Edit edit) {
            previewed = edit;
        }

        @Override
        public void commitPreview() {
            if (previewed != null) {
                apply(previewed);
                previewed = null;
            }
        }

        @Override
        public void undo() {
            history.undo();
            message = null;
            layOutAgainWhenNeeded();
        }

        @Override
        public void redo() {
            history.redo();
            message = null;
            layOutAgainWhenNeeded();
        }

        @Override
        public boolean canUndo() {
            return history.canUndo();
        }

        @Override
        public boolean canRedo() {
            return history.canRedo();
        }

        @Override
        public void say(Component text) {
            message = text;
        }

        @Override
        public Component message() {
            return message;
        }

        @Override
        public List<WorldOption> worldOptions() {
            return worldOptions;
        }

        @Override
        public HolderLookup.Provider registries() {
            return context.worldgenLoadContext();
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            return context.dataConfiguration().enabledFeatures();
        }

        @Override
        public Screen screen() {
            return EditorScreen.this;
        }

        @Override
        public void open(Screen screen) {
            minecraft.setScreen(screen);
        }

        @Override
        public int textWidth(Component text) {
            return font.width(text);
        }

        @Override
        public int lineHeight() {
            return font.lineHeight;
        }
    }
}
