package dev.flatgen.blocks.client;

import dev.flatgen.blocks.BlockPicksFile;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.rules.Search;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/** The block picker (F05): every block the world allows, as its icon; picks and stars kept in blocks.json. */
public final class BlockPicker {
    /** The size of a block's card, in GUI pixels: its icon and a pixel around it. */
    private static final int SLOT = Icon.SIZE + 2;

    private BlockPicker() {
    }

    /** The picker over the blocks these features allow, going back to {@code parent}, handing on the block picked. */
    public static PickerScreen screen(Screen parent, FeatureFlagSet enabled, Consumer<String> onPick) {
        return new PickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.blocks.title"),
                Component.translatable("flatgen.blocks.search"), Component.translatable("flatgen.blocks.all"),
                BlockCatalog.blocks(enabled), SLOT, SLOT, new Cards(), ClientPlatform.get()::modName,
                BlockPicksFile::read, BlockPicksFile::save),
                onPick);
    }

    /** A block's card: its icon; its tooltip, its name, id and mod. */
    private static final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            canvas.blockIcon(entry.id(), place.x() + 1, place.y() + 1);
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            return List.of(Component.literal(entry.name()), Component.literal(entry.id()),
                    Component.literal(ClientPlatform.get().modName(entry.namespace())));
        }
    }
}
