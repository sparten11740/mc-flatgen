package dev.flatgen.editor.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.History;
import dev.flatgen.editor.rules.World;
import dev.flatgen.layers.client.ColumnWidget;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Flatgen's superflat editor, opened by Customize: the world's layers in a column, beside it the panels of
 * {@link EditorPanels}, top down, wrapping within the space left and scrolling when taller than it, and Done, the
 * game's own editor and Cancel below. Every change is a step to undo. Done applies the preset to the Create World
 * screen; Cancel leaves it.
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
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    private final World world;
    private final History history;
    private final Context editor = new Context();
    private Edit previewed;
    /** What the last copy or paste said, until the next change; null for nothing. */
    private Component message;
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
        return editor.shown().preset();
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
        return editor.shown().selected();
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        Rect space = new Rect(MARGIN, HEADER, ColumnWidget.COLUMN_WIDTH + ColumnWidget.LABEL_WIDTH,
                height - HEADER - FOOTER);
        column = new ColumnWidget(editor, space);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(column);
        int left = space.right() + MARGIN;
        Rect panelSpace = new Rect(left, HEADER, width - left - MARGIN, space.height());
        widgets.add(new ScrollGroup(panelSpace, top -> panels(new Rect(left, top, panelSpace.width(), 0))));
        widgets.addAll(footer(width, height));
        return widgets;
    }

    /** Every panel's widgets, from the top of this area down within its width. */
    private ScrollGroup.Laid panels(Rect area) {
        FlowLayout flow = new FlowLayout(area, PanelSizes.ROW_HEIGHT, PanelSizes.GAP);
        List<Widget> widgets = new ArrayList<>();
        EditorPanels.ALL.forEach(panel -> widgets.addAll(panel.layOut(editor, flow)));
        return new ScrollGroup.Laid(widgets, flow.bottom());
    }

    @Override
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        for (EditorPanel panel : EditorPanels.ALL) {
            if (panel.shortcut(editor, keyCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private List<Widget> footer(int width, int height) {
        int gap = 2 * PanelSizes.GAP;
        int rowWidth = 3 * FOOTER_BUTTON_WIDTH + 2 * gap;
        FlowLayout row = new FlowLayout(new Rect((width - rowWidth) / 2, height - (FOOTER + PanelSizes.ROW_HEIGHT) / 2,
                rowWidth, 0), PanelSizes.ROW_HEIGHT, gap);
        return List.of(
                new TextButton(CommonComponents.GUI_DONE, row.next(FOOTER_BUTTON_WIDTH), this::done),
                new TextButton(Component.translatable("flatgen.editor.vanilla"), row.next(FOOTER_BUTTON_WIDTH),
                        this::openVanilla),
                new TextButton(CommonComponents.GUI_CANCEL, row.next(FOOTER_BUTTON_WIDTH), this::onClose));
    }

    private void done() {
        WorldCreation.applyFlatSettings(parent,
                FlatSettings.toSettings(history.current().preset(), context.worldgenLoadContext()));
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
            return previewed == null ? history.current() : previewed.apply(history.current(), world);
        }

        @Override
        public World world() {
            return world;
        }

        @Override
        public void apply(Edit edit) {
            history.apply(edit, world);
            message = null;
        }

        @Override
        public void select(int layer) {
            history.select(layer);
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
        }

        @Override
        public void redo() {
            history.redo();
            message = null;
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
