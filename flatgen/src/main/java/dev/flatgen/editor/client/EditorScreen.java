package dev.flatgen.editor.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.layers.client.ColumnWidget;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.Names;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Flatgen's superflat editor, opened by Customize: the world's layers in a column, what is selected beside it, and
 * Done, the game's own editor and Cancel below. Done applies the preset to the Create World screen; Cancel leaves it.
 */
public final class EditorScreen extends FlatgenScreen {
    /** Space above the column for the title, in GUI pixels. */
    private static final int HEADER = 24;
    /** Space below the column for the buttons, in GUI pixels. */
    private static final int FOOTER = 32;
    /** Space at the sides and between the column and the panel, in GUI pixels. */
    private static final int MARGIN = 12;
    /** A footer button's size, in GUI pixels: three fit the narrowest screen at the largest GUI scale. */
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 8;
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    private final Preset preset;
    private final int floor;
    private int selected;
    private ColumnWidget column;

    public EditorScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("flatgen.editor.title"));
        this.parent = parent;
        this.context = context;
        this.preset = FlatSettings.toPreset(WorldCreation.flatSettings(context), context.worldgenLoadContext(), "",
                ICON).preset();
        this.floor = WorldCreation.overworldType(context).minY();
        this.selected = preset.layers().size() - 1;
    }

    /** The preset the editor holds. */
    public Preset preset() {
        return preset;
    }

    /** The Y of the world's floor, where the bottom layer starts. */
    public int worldBottom() {
        return floor;
    }

    /** The column, as last laid out. */
    public ColumnWidget column() {
        return column;
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        Rect space = new Rect(MARGIN, HEADER, ColumnWidget.COLUMN_WIDTH + ColumnWidget.LABEL_WIDTH,
                height - HEADER - FOOTER);
        int fluidColor = Biomes.waterColor(preset.biome(), context.worldgenLoadContext());
        column = new ColumnWidget(preset, floor, fluidColor, space, selected, layer -> selected = layer);
        int panelX = space.right() + MARGIN;
        List<Widget> widgets = new ArrayList<>();
        widgets.add(column);
        widgets.add(new LayerInfo(new Rect(panelX, HEADER, width - panelX - MARGIN, space.height()), this::info));
        widgets.addAll(footer(width, height));
        return widgets;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** What the panel says: the selected layer as its tooltip does, then the surface and the biome. */
    private List<Component> info() {
        List<Component> lines = new ArrayList<>();
        if (selected >= 0) {
            lines.addAll(column.describe(selected));
            lines.add(Component.empty());
        }
        lines.add(Component.translatable("flatgen.editor.surface", floor + preset.height()));
        lines.add(Component.translatable("flatgen.editor.biome", Names.biome(preset.biome())));
        return lines;
    }

    private List<Widget> footer(int width, int height) {
        int top = height - (FOOTER + BUTTON_HEIGHT) / 2;
        int left = (width - 3 * BUTTON_WIDTH - 2 * BUTTON_SPACING) / 2;
        int step = BUTTON_WIDTH + BUTTON_SPACING;
        return List.of(
                new TextButton(CommonComponents.GUI_DONE, new Rect(left, top, BUTTON_WIDTH, BUTTON_HEIGHT), this::done),
                new TextButton(Component.translatable("flatgen.editor.vanilla"),
                        new Rect(left + step, top, BUTTON_WIDTH, BUTTON_HEIGHT), this::openVanilla),
                new TextButton(CommonComponents.GUI_CANCEL,
                        new Rect(left + 2 * step, top, BUTTON_WIDTH, BUTTON_HEIGHT), this::onClose));
    }

    private void done() {
        WorldCreation.applyFlatSettings(parent, FlatSettings.toSettings(preset, context.worldgenLoadContext()));
        onClose();
    }

    private void openVanilla() {
        minecraft.setScreen(PresetEditors.VANILLA.createEditScreen(parent, context));
    }
}
