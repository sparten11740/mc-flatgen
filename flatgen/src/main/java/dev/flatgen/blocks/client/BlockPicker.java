package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.GridPickerScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.mc.Names;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.PicksFile;
import dev.flatgen.search.rules.Search;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/**
 * The block picker (F05): every block the world allows, each a card with its picture and its marks ({@link
 * BlockMarks}) and its name; picks and stars kept in blocks.json.
 */
public final class BlockPicker {
    /** A block's card, and the picture of the block on it, in GUI pixels. */
    private static final int CARD_WIDTH = 50;
    private static final int CARD_HEIGHT = 42;
    private static final int PICTURE = 22;
    /** Space above the picture and between it and the name, in GUI pixels. */
    private static final int SPACING = 4;
    /** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("blocks.json");

    private BlockPicker() {
    }

    /** The picker over the blocks these features allow, going back to {@code parent}, handing on the block picked. */
    public static PickerScreen screen(Screen parent, FeatureFlagSet enabled, Consumer<String> onPick) {
        return new GridPickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.blocks.title"),
                Component.translatable("flatgen.blocks.search"), BlockCatalog.blocks(enabled), Icon::block,
                ClientPlatform.get()::modName, PICKS::read, PICKS::save),
                Component.translatable("flatgen.blocks.all"), CARD_WIDTH, CARD_HEIGHT, new Cards(), onPick);
    }

    /** A block's card: its picture with its marks, and its name below. */
    private static final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            Rect picture = new Rect(place.x() + (place.width() - PICTURE) / 2, place.y() + SPACING, PICTURE, PICTURE);
            canvas.blockIcon(entry.id(), picture);
            BlockMarks.draw(canvas, entry.id(), picture);
            Rect name = new Rect(place.x() + 1, picture.bottom() + SPACING, place.width() - 2,
                    TextStyle.BODY.lineHeight());
            canvas.centeredText(canvas.fitted(Names.block(entry.id()), name.width(), TextStyle.BODY), name,
                    Look.MUTED);
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            return BlockCatalog.traits(entry.id()).falls() ? List.of(Component.translatable("flatgen.hint.falls"))
                    : List.of();
        }
    }
}
