package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Chip;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.WidgetGroup;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.dimensions.client.DimensionTabs;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
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
 * Flatgen's superflat editor, opened by Customize, laid out as on the design canvas (docs/design/simple-editor.md):
 * a header with the title, the dimensions, the tools of the panels that go there and the Advanced switch; the world
 * from the side on the left, the layers in the middle, the world's settings on the right, scrolling when they do not
 * fit; and a footer with undo and redo, Cancel and Done. The panels of {@link EditorPanels} that show place their
 * widgets in these {@link EditorRegion}s. Every change is a step to undo. Done applies the preset to the Create World
 * screen; Cancel leaves it.
 */
public final class EditorScreen extends FlatgenScreen {
    /** The header's and the footer's heights, in GUI pixels. */
    private static final int HEADER = 32;
    private static final int FOOTER = 40;
    /** Space at the screen's sides, in GUI pixels. */
    private static final int MARGIN = 13;
    /** Space between the screen's parts, in GUI pixels. */
    private static final int REGION_GAP = 11;
    /** Space between the header's picture and the title, and between Cancel and Done, in GUI pixels. */
    private static final int TITLE_GAP = 4;
    private static final int FOOTER_BUTTON_GAP = 4;
    /** How wide the world on the left and the settings on the right are, in GUI pixels. */
    private static final int WORLD_WIDTH = 127;
    private static final int ASIDE_WIDTH = 152;
    /** Space between the bar above the layers and the layers, in GUI pixels. */
    private static final int BAR_GAP = 7;
    /** How wide the footer's Cancel and Done are, in GUI pixels. */
    private static final int FOOTER_BUTTON_WIDTH = 66;
    /** Space inside the world's panel below the column, in GUI pixels. */
    private static final int WORLD_PADDING = 8;
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";
    /**
     * Whether the editor shows everything, for adults who build flat worlds, rather than the simple view a child can
     * work out; remembered until the game closes.
     */
    private static boolean advanced;

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
    /** What the widgets were last laid out for. */
    private Layout laidOutFor;

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

    /** The header, the world, the regions the panels fill and the footer, for a screen this big. */
    @Override
    protected List<Widget> layOut(int width, int height) {
        Frame frame = new Frame(width, height);
        laidOutFor = layoutKey();
        EditorLayout laid = frame.layOut(frame.aside.y());
        List<Widget> widgets = new ArrayList<>(header(frame, laid));
        widgets.add(new WidgetGroup(worldWidgets(frame.world, laid)));
        widgets.add(new WidgetGroup(layersWidgets(frame.layers, laid)));
        widgets.add(new ScrollGroup(frame.aside, frame::aside));
        widgets.add(new WidgetGroup(footer(frame, laid)));
        return widgets;
    }

    /** The lines under the header and over the footer. */
    @Override
    protected void drawBehind(Canvas canvas) {
        canvas.fill(new Rect(0, HEADER, width, 1), Look.DIVIDER);
        canvas.fill(new Rect(0, height - FOOTER, width, 1), Look.DIVIDER);
    }

    /** The title, the dimensions in the middle, the panels' tools after them and the Advanced switch at the right. */
    private List<Widget> header(Frame frame, EditorLayout laid) {
        Component advancedLabel = Component.translatable("flatgen.editor.advanced");
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Label(Icon.item(ICON), Component.empty(), new Rect(MARGIN, (HEADER - Icon.SIZE) / 2,
                Icon.SIZE, Icon.SIZE)));
        widgets.add(new Caption(() -> title, new Rect(MARGIN + Icon.SIZE + TITLE_GAP, 0,
                frame.tabs.x() - MARGIN - Icon.SIZE - TITLE_GAP, HEADER), TextStyle.TITLE, Look.TEXT));
        widgets.add(new WidgetGroup(DimensionTabs.layOut(editor, frame.tabs)));
        widgets.add(new WidgetGroup(laid.widgets(EditorRegion.HEADER)));
        widgets.add(new Toggle(Icon.line("advanced"), advancedLabel, frame.advanced, () -> advanced,
                this::turnAdvancedOver, () -> true,
                () -> List.of(advancedLabel, Component.translatable("flatgen.editor.advanced_tip"))));
        return widgets;
    }

    /** The world's panel with the column below the chips over it, or what a dimension without layers shows there. */
    private List<Widget> worldWidgets(Rect world, EditorLayout laid) {
        List<Widget> widgets = new ArrayList<>(List.of(new Surface(Look.PANEL, world)));
        int columnWidth = ColumnWidget.COLUMN_WIDTH + ColumnWidget.LABEL_WIDTH;
        int columnTop = world.y() + Chip.clearBelow();
        Rect space = new Rect(world.x() + (world.width() - columnWidth) / 2, columnTop, columnWidth,
                world.bottom() - WORLD_PADDING - columnTop);
        column = new ColumnWidget(editor, space, FactMarkers.of(editor));
        if (editor.shown().stack().isPresent()) {
            widgets.add(column);
        }
        widgets.addAll(laid.widgets(EditorRegion.WORLD));
        return widgets;
    }

    /** The heading and the bar above the layers, and the layers. */
    private static List<Widget> layersWidgets(Rect layers, EditorLayout laid) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Caption(() -> Component.translatable("flatgen.editor.layers"), new Rect(layers.x(),
                layers.y(), layers.width(), EditorRegion.LAYERS_BAR.rowHeight()), TextStyle.TITLE, Look.TEXT));
        widgets.addAll(laid.widgets(EditorRegion.LAYERS_BAR));
        widgets.addAll(laid.widgets(EditorRegion.LAYERS));
        return widgets;
    }

    /** Undo, redo and what the panels place at the left; Cancel and Done at the right. */
    private List<Widget> footer(Frame frame, EditorLayout laid) {
        Rect row = frame.footerRow;
        Rect done = new Rect(row.right() - FOOTER_BUTTON_WIDTH, row.y(), FOOTER_BUTTON_WIDTH, row.height());
        Rect cancel = new Rect(done.x() - FOOTER_BUTTON_GAP - FOOTER_BUTTON_WIDTH, row.y(), FOOTER_BUTTON_WIDTH,
                row.height());
        List<Widget> widgets = new ArrayList<>(laid.widgets(EditorRegion.FOOTER));
        widgets.add(new TextButton(Icon.line("cross"), CommonComponents.GUI_CANCEL, cancel, this::onClose));
        widgets.add(new TextButton(Icon.line("check"), CommonComponents.GUI_DONE, done, this::done)
                .kind(Look.ButtonKind.PRIMARY));
        return widgets;
    }

    /** Where the editor's parts and regions are on a screen this big, and the panels laid out in them. */
    private final class Frame {
        private final Rect world;
        private final Rect layers;
        private final Rect aside;
        private final Rect tabs;
        private final Rect advanced;
        private final Rect footerRow;
        private final Map<EditorRegion, Rect> areas = new EnumMap<>(EditorRegion.class);

        Frame(int width, int height) {
            int bodyTop = HEADER + REGION_GAP;
            int bodyBottom = height - FOOTER - REGION_GAP;
            world = new Rect(MARGIN, bodyTop, WORLD_WIDTH, bodyBottom - bodyTop);
            aside = new Rect(width - MARGIN - ASIDE_WIDTH, bodyTop, ASIDE_WIDTH, bodyBottom - bodyTop);
            layers = new Rect(world.right() + REGION_GAP, bodyTop, aside.x() - 2 * REGION_GAP - world.right(),
                    world.height());
            int controlTop = (HEADER - DimensionTabs.HEIGHT) / 2;
            int tabsWidth = DimensionTabs.width(editor);
            tabs = new Rect((width - tabsWidth) / 2, controlTop, tabsWidth, DimensionTabs.HEIGHT);
            int switchWidth = Toggle.width(textWidth(Component.translatable("flatgen.editor.advanced")));
            advanced = new Rect(width - MARGIN - switchWidth, controlTop, switchWidth, DimensionTabs.HEIGHT);
            int footerHeight = EditorRegion.FOOTER.rowHeight();
            footerRow = new Rect(MARGIN, height - FOOTER + (FOOTER - footerHeight) / 2, width - 2 * MARGIN,
                    footerHeight);
            areas.put(EditorRegion.HEADER, new Rect(tabs.right() + REGION_GAP, (HEADER
                    - EditorRegion.HEADER.rowHeight()) / 2, advanced.x() - REGION_GAP - tabs.right() - REGION_GAP, 0));
            int barHeight = EditorRegion.LAYERS_BAR.rowHeight();
            int barX = layers.x() + textWidth(Component.translatable("flatgen.editor.layers"), TextStyle.TITLE)
                    + REGION_GAP;
            areas.put(EditorRegion.LAYERS_BAR, new Rect(barX, layers.y(), layers.right() - barX, barHeight));
            areas.put(EditorRegion.LAYERS, new Rect(layers.x(), layers.y() + barHeight + BAR_GAP, layers.width(),
                    layers.height() - barHeight - BAR_GAP));
            areas.put(EditorRegion.WORLD, world);
            areas.put(EditorRegion.FOOTER, new Rect(footerRow.x(), footerRow.y(), footerRow.width() / 2,
                    footerHeight));
        }

        /** Every panel that shows laid out with the aside's top here, the game's own editor last in Advanced. */
        EditorLayout layOut(int asideTop) {
            Map<EditorRegion, Rect> placed = new EnumMap<>(areas);
            placed.put(EditorRegion.ASIDE, new Rect(aside.x(), asideTop, aside.width(), 0));
            EditorLayout layout = new EditorLayout(placed, EditorScreen.this::textWidth);
            shownPanels().forEach(panel -> panel.layOut(editor, layout));
            if (EditorScreen.advanced) {
                Rect place = layout.flow(EditorRegion.ASIDE).line(EditorRegion.ASIDE.rowHeight());
                layout.add(EditorRegion.ASIDE, new TextButton(Icon.line("external"),
                        Component.translatable("flatgen.editor.vanilla"), place, EditorScreen.this::openVanilla));
            }
            return layout;
        }

        /** The aside's widgets with its top here, as it scrolls. */
        ScrollGroup.Laid aside(int top) {
            EditorLayout layout = layOut(top);
            return new ScrollGroup.Laid(layout.widgets(EditorRegion.ASIDE), layout.flow(EditorRegion.ASIDE).bottom());
        }
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

    /** What decides the widgets now. */
    private Layout layoutKey() {
        EditorState current = history.current();
        return new Layout(current.edited(), current.stack().isPresent(), current.layers().size(), shownPanels());
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

    /** Turns Advanced on or off, which lays the editor out again. */
    private void turnAdvancedOver() {
        advanced = !advanced;
        rebuildWidgets();
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

    /**
     * What decides the editor's widgets: the edited dimension, whose panels name it, whether it has a stack, which
     * decides the column, how many layers it has, each a card, and the panels that show.
     */
    private record Layout(Dimension edited, boolean stack, int layers, List<EditorPanel> panels) {
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
        public boolean packDecides(Dimension dimension) {
            return WorldCreation.packDecides(context, dimension);
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
        public int textWidth(Component text, TextStyle style) {
            return EditorScreen.this.textWidth(text, style);
        }

        @Override
        public boolean advanced() {
            return advanced;
        }
    }
}
