package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.ShelvedPickerScreen;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.mc.Names;
import dev.flatgen.search.rules.Search;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The block picker (F05) as the biome picker is laid out: the creative inventory's tabs of blocks down its side as the
 * kinds, with their names and pictures as the game shows them, beside the starred, the recent and every block; each
 * block a card with its picture, its marks ({@link BlockMarks}) and its name. A click on a card picks its block.
 */
final class BlockPickerScreen extends ShelvedPickerScreen {
    /** A block's card, and the picture of the block on it, in GUI pixels. */
    private static final int CARD_WIDTH = 50;
    private static final int CARD_HEIGHT = 42;
    private static final int PICTURE = 22;
    /** Space above the picture and between it and the name, in GUI pixels. */
    private static final int SPACING = 4;
    /** The blocks Every block's picture shows, four in a square. */
    private static final List<String> EVERY = List.of("minecraft:grass_block", "minecraft:stone",
            "minecraft:oak_planks", "minecraft:glass");

    private final List<BlockCatalog.Tab> tabs;

    /**
     * A picker of blocks going back to {@code parent}, handing a picked block's id to {@code onPick}.
     *
     * @param tabs the creative inventory's tabs of blocks, which the side lists as kinds
     */
    BlockPickerScreen(Screen parent, Picker picker, List<BlockCatalog.Tab> tabs, Consumer<String> onPick) {
        super(parent, picker, onPick);
        this.tabs = List.copyOf(tabs);
    }

    /** The picker's title. */
    @Override
    protected List<Widget> header(Rect area) {
        return List.of(new Caption(this::getTitle, area, TextStyle.TITLE, Look.TEXT));
    }

    /** A shelf for each tab of the creative inventory with blocks a layer can be of. */
    @Override
    protected List<Shelf> kinds() {
        return tabs.stream().map(BlockPickerScreen::shelf).filter(shelf -> !shelf.of().apply(entries()).isEmpty())
                .toList();
    }

    /** A tab's shelf: its blocks among those shown, in the creative inventory's order. */
    private static Shelf shelf(BlockCatalog.Tab tab) {
        Map<String, Integer> order = new HashMap<>();
        tab.blocks().forEach(block -> order.putIfAbsent(block, order.size()));
        return new Shelf(tab.name(), Icon.item(tab.icon()), shown -> shown.stream()
                .filter(entry -> order.containsKey(entry.id()))
                .sorted(Comparator.comparing(entry -> order.get(entry.id()))).toList());
    }

    /** Every block, its picture four blocks in a square. */
    @Override
    protected Shelf every() {
        return new Shelf(Component.translatable("flatgen.blocks.every"), BlockPickerScreen::drawFour, all -> all);
    }

    private static void drawFour(Canvas canvas, int x, int y) {
        int half = Icon.SIZE / 2;
        for (int index = 0; index < EVERY.size(); index++) {
            canvas.blockIcon(EVERY.get(index), new Rect(x + index % 2 * half, y + index / 2 * half, half, half));
        }
    }

    /** The grid of blocks. */
    @Override
    protected List<Widget> beside(Rect area) {
        return List.of(grid(area, CARD_WIDTH, CARD_HEIGHT, new Cards(), this::pick));
    }

    /** A block's card: its picture with its marks, and its name below; its tooltip says when it falls. */
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
