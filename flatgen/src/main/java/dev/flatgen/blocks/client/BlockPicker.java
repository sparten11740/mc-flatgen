package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Canvas;
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
 * The block picker (F05): every block the world allows, each a card with its picture, its name and a mark when it falls
 * with nothing under it; picks and stars kept in blocks.json.
 */
public final class BlockPicker {
    /** A block's card, and the picture of the block on it, in GUI pixels. */
    private static final int CARD_WIDTH = 50;
    private static final int CARD_HEIGHT = 42;
    private static final int PICTURE = 22;
    /** Space above the picture and between it and the name, in GUI pixels. */
    private static final int SPACING = 4;
    /** How far above and below its line a name may draw, in GUI pixels, so its letters keep their tails. */
    private static final int NAME_SLACK = 2;
    /** The mark of a block that falls: its size, and the picture inside its yellow pill, in GUI pixels. */
    private static final int MARK = 10;
    private static final int MARK_PICTURE = 8;
    /** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("blocks.json");

    private BlockPicker() {
    }

    /** The picker over the blocks these features allow, going back to {@code parent}, handing on the block picked. */
    public static PickerScreen screen(Screen parent, FeatureFlagSet enabled, Consumer<String> onPick) {
        return new PickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.blocks.title"),
                Component.translatable("flatgen.blocks.search"), Component.translatable("flatgen.blocks.all"),
                BlockCatalog.blocks(enabled), CARD_WIDTH, CARD_HEIGHT, new Cards(), Icon::block,
                ClientPlatform.get()::modName,
                PICKS::read, PICKS::save),
                onPick);
    }

    /** A block's card: its picture, the mark when it falls beside it, and its name below. */
    private static final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            Rect picture = new Rect(place.x() + (place.width() - PICTURE) / 2, place.y() + SPACING, PICTURE, PICTURE);
            canvas.blockIcon(entry.id(), picture);
            if (BlockCatalog.traits(entry.id()).falls()) {
                Rect mark = new Rect(picture.right() + 1, picture.y(), MARK, MARK);
                canvas.sprite(Look.WARNING, mark);
                int inset = (MARK - MARK_PICTURE) / 2;
                canvas.sprite(Look.icon("falls"), new Rect(mark.x() + inset, mark.y() + inset, MARK_PICTURE,
                        MARK_PICTURE));
            }
            Rect name = new Rect(place.x() + 1, picture.bottom() + SPACING, place.width() - 2,
                    TextStyle.BODY.lineHeight());
            canvas.clip(new Rect(name.x(), name.y() - NAME_SLACK, name.width(), name.height() + 2 * NAME_SLACK));
            canvas.centeredText(Names.block(entry.id()), name, Look.MUTED);
            canvas.unclip();
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            return BlockCatalog.traits(entry.id()).falls() ? List.of(Component.translatable("flatgen.hint.falls"))
                    : List.of();
        }
    }
}
