package dev.flatgen.editor.client;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.Names;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Flatgen's superflat editor, opened by Customize. For now it shows the world's layers and biome, applies them on Done,
 * leaves them on Cancel, and opens the game's own editor for what it cannot do yet.
 */
public final class EditorScreen extends Screen {
    /** Space between the footer's buttons, in GUI pixels. */
    private static final int BUTTON_SPACING = 8;
    /** Width of each footer button, in GUI pixels: three fit the narrowest screen at the largest GUI scale. */
    private static final int BUTTON_WIDTH = 100;
    /** The icon of the preset the editor starts from, which the world's settings do not carry. */
    private static final String ICON = "minecraft:grass_block";

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    private final Preset preset;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public EditorScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("flatgen.editor.title"));
        this.parent = parent;
        this.context = context;
        this.preset = FlatSettings.toPreset(WorldCreation.flatSettings(context), context.worldgenLoadContext(), "",
                ICON).preset();
    }

    /** The preset the editor holds. */
    public Preset preset() {
        return preset;
    }

    /** The Y of the world's floor, where the bottom layer starts. */
    public int worldBottom() {
        return WorldCreation.overworldType(context).minY();
    }

    @Override
    protected void init() {
        layout.addTitleHeader(title, font);
        layout.addToContents(new MultiLineTextWidget(summary(), font).setCentered(true));
        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(BUTTON_SPACING));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> done()).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(Component.translatable("flatgen.editor.vanilla"), button -> openVanilla())
                .width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).width(BUTTON_WIDTH).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void done() {
        WorldCreation.applyFlatSettings(parent, FlatSettings.toSettings(preset, context.worldgenLoadContext()));
        onClose();
    }

    private void openVanilla() {
        minecraft.setScreen(PresetEditors.VANILLA.createEditScreen(parent, context));
    }

    /** The layers from the top down, a line each, then the biome. */
    private Component summary() {
        MutableComponent text = Component.empty();
        List<Layer> layers = preset.layers();
        for (int index = layers.size() - 1; index >= 0; index--) {
            Layer layer = layers.get(index);
            text.append(Component.translatable("flatgen.editor.layer", layer.height(), Names.block(layer.block())))
                    .append("\n");
        }
        return text.append(Component.translatable("flatgen.editor.biome", Names.biome(preset.biome())));
    }
}
