package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.PicksFile;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/**
 * The block picker (F05) over every block the world allows, sorted by the creative inventory's tabs, its picks and
 * stars kept in blocks.json; {@link BlockPickerScreen} is how it looks.
 */
public final class BlockPicker {
    /** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("blocks.json");

    private BlockPicker() {
    }

    /**
     * The picker over the blocks these features allow, going back to {@code parent}, handing on the block picked.
     *
     * @param registries the world's registries, which the creative inventory's tabs are filled from
     */
    public static PickerScreen screen(Screen parent, FeatureFlagSet enabled, HolderLookup.Provider registries,
            Consumer<String> onPick) {
        PickerScreen.Picker picker = new PickerScreen.Picker(Component.translatable("flatgen.blocks.title"),
                Component.translatable("flatgen.blocks.search"), BlockCatalog.blocks(enabled), Icon::block,
                ClientPlatform.get()::modName, PICKS::read, PICKS::save);
        return new BlockPickerScreen(parent, picker, BlockCatalog.tabs(enabled, registries), onPick);
    }
}
