package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.PicksFile;
import dev.flatgen.search.rules.Search;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/** The block picker (F05): every block the world allows, as its icon; picks and stars kept in blocks.json. */
public final class BlockPicker {
    /** The size of a block's card, in GUI pixels: its icon and a pixel around it. */
    private static final int SLOT = Icon.SIZE + 2;
    /** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("blocks.json");

    private BlockPicker() {
    }

    /** The picker over the blocks these features allow, going back to {@code parent}, handing on the block picked. */
    public static PickerScreen screen(Screen parent, FeatureFlagSet enabled, Consumer<String> onPick) {
        return new PickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.blocks.title"),
                Component.translatable("flatgen.blocks.search"), Component.translatable("flatgen.blocks.all"),
                BlockCatalog.blocks(enabled), SLOT, SLOT, new Cards(), Icon::block, ClientPlatform.get()::modName,
                PICKS::read, PICKS::save),
                onPick);
    }

    /** A block's card: its icon. */
    private static final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            canvas.blockIcon(entry.id(), place.x() + 1, place.y() + 1);
        }
    }
}
