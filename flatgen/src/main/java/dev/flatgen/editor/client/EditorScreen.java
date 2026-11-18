package dev.flatgen.editor.client;

import dev.flatgen.mc.WorldCreation;
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
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Flatgen's superflat editor, opened by Customize. For now it shows the world's layers and biome, applies them on Done,
 * leaves them on Cancel, and opens the game's own editor for what it cannot do yet.
 */
public final class EditorScreen extends Screen {
    /** Space between the footer's buttons, in GUI pixels. */
    private static final int BUTTON_SPACING = 8;
    /** Width of each footer button, in GUI pixels: three fit the narrowest screen at the largest GUI scale. */
    private static final int BUTTON_WIDTH = 100;

    private final CreateWorldScreen parent;
    private final WorldCreationContext context;
    private final FlatLevelGeneratorSettings settings;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public EditorScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("flatgen.editor.title"));
        this.parent = parent;
        this.context = context;
        this.settings = WorldCreation.flatSettings(context);
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
        WorldCreation.applyFlatSettings(parent, settings);
        onClose();
    }

    private void openVanilla() {
        minecraft.setScreen(PresetEditors.VANILLA.createEditScreen(parent, context));
    }

    /** The layers from the top down, a line each, then the biome. */
    private Component summary() {
        MutableComponent text = Component.empty();
        List<FlatLayerInfo> layers = settings.getLayersInfo();
        for (int index = layers.size() - 1; index >= 0; index--) {
            FlatLayerInfo layer = layers.get(index);
            Component block = layer.getBlockState().getBlock().getName();
            text.append(Component.translatable("flatgen.editor.layer", layer.getHeight(), block)).append("\n");
        }
        Component biome = settings.getBiome().unwrapKey()
                .map(key -> Component.translatable(key.location().toLanguageKey("biome")))
                .orElse(Component.empty());
        return text.append(Component.translatable("flatgen.editor.biome", biome));
    }
}
