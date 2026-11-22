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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The block picker (F05): every block, the game's own first; down its side the starred and the recent blocks, as the
 * biome picker has them, all the blocks, and the creative inventory's tabs of blocks with their names and pictures as
 * the game shows them, to narrow them to one. Each block is a card with its picture, its marks ({@link BlockMarks}) and
 * its name, the block the layer has now ringed; a click on a card picks its block.
 */
final class BlockPickerScreen extends ShelvedPickerScreen {
    /** A block's card, and the picture of the block on it, in GUI pixels. */
    private static final int CARD_WIDTH = 58;
    private static final int CARD_HEIGHT = 48;
    private static final int PICTURE = 22;
    /** Space above the picture and between it and the name, in GUI pixels. */
    private static final int SPACING = 5;
    /** Space between a card's edge and its name, in GUI pixels, so the focus ring keeps clear of the words. */
    private static final int NAME_INSET = 5;
    /** The blocks Every block's picture shows, four in a square. */
    private static final List<String> EVERY = List.of("minecraft:grass_block", "minecraft:stone",
            "minecraft:oak_planks", "minecraft:glass");

    private final List<BlockCatalog.Tab> tabs;
    /** The block the picker is for now, ringed among the cards; none for a block not there yet. */
    private final Optional<String> current;

    /**
     * A picker of blocks going back to {@code parent}, handing a picked block's id to {@code onPick}.
     *
     * @param tabs    the creative inventory's tabs of blocks, which the side lists as kinds
     * @param current the block it is for now, as a layer's, ringed among the cards; none for a new layer
     */
    BlockPickerScreen(Screen parent, Picker picker, List<BlockCatalog.Tab> tabs, Optional<String> current,
            Consumer<String> onPick) {
        super(parent, picker, onPick);
        this.tabs = List.copyOf(tabs);
        this.current = current;
    }

    /** The picker's title. */
    @Override
    protected List<Widget> header(Rect area) {
        return List.of(new Caption(this::getTitle, area, TextStyle.TITLE, Look.TEXT));
    }

    /** The starred and the recent blocks, all the blocks, then each inventory tab with blocks a layer can be of. */
    @Override
    protected List<List<Shelf>> shelves() {
        return List.of(List.of(starredShelf(), recentShelf()), List.of(every()), tabs.stream()
                .map(BlockPickerScreen::shelf).filter(shelf -> !shelf.of().apply(entries()).isEmpty()).toList());
    }

    /** A tab's shelf: its blocks among those shown, in the creative inventory's order. */
    private static Shelf shelf(BlockCatalog.Tab tab) {
        Map<String, Integer> order = new HashMap<>();
        tab.blocks().forEach(block -> order.put(block, order.size()));
        return new Shelf(tab.name(), Icon.item(tab.icon()), shown -> shown.stream()
                .filter(entry -> order.containsKey(entry.id()))
                .sorted(Comparator.comparing(entry -> order.get(entry.id()))).toList());
    }

    /** All the blocks, its picture four blocks in a square. */
    private Shelf every() {
        return everyShelf(Component.translatable("flatgen.blocks.every"), Icon.quartered(EVERY.stream()
                .<BiConsumer<Canvas, Rect>>map(block -> (canvas, area) -> canvas.blockIcon(block, area)).toList()));
    }

    /** The grid of blocks. */
    @Override
    protected List<Widget> beside(Rect area) {
        return List.of(grid(area, CARD_WIDTH, CARD_HEIGHT, new Cards(), this::pick));
    }

    /**
     * A block's card: its picture with its marks, and its name below, ringed while it is the block now; its tooltip
     * says so, and when it falls.
     */
    private final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            boolean isNow = current.filter(entry.id()::equals).isPresent();
            Rect picture = new Rect(place.x() + (place.width() - PICTURE) / 2, place.y() + SPACING, PICTURE, PICTURE);
            canvas.blockIcon(entry.id(), picture);
            BlockMarks.draw(canvas, entry.id(), picture);
            Rect name = new Rect(place.x() + NAME_INSET, picture.bottom() + SPACING, place.width() - 2 * NAME_INSET,
                    TextStyle.BODY.lineHeight());
            canvas.centeredText(canvas.fitted(Names.block(entry.id()), name.width(), TextStyle.BODY), name,
                    isNow ? Look.TEXT : Look.MUTED);
            if (isNow) {
                canvas.sprite(Look.CHOSEN_RING, place);
            }
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            Stream<Component> now = current.filter(entry.id()::equals)
                    .<Component>map(id -> Component.translatable("flatgen.picks.now")).stream();
            Stream<Component> falls = BlockCatalog.traits(entry.id()).falls()
                    ? Stream.of(Component.translatable("flatgen.hint.falls")) : Stream.empty();
            return Stream.concat(now, falls).toList();
        }
    }
}
