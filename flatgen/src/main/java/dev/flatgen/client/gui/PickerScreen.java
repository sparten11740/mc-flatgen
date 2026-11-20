package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.Screens;
import dev.flatgen.search.rules.PickSections;
import dev.flatgen.search.rules.Picks;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A picker, a sheet over the screen it came from: every entry of its kind as cards, the starred and recent ones first,
 * found by typing and narrowed to one mod. Picking one hands it on and goes back, as Close and Escape go back without;
 * the picks and stars are kept in the picker's own file for next time. The block and the biome pickers are this
 * screen, each with its {@link Picker}.
 */
public final class PickerScreen extends FlatgenScreen {
    /** The most room left around the sheet at the screen's sides and top and bottom, in GUI pixels. */
    private static final int SHEET_MARGIN_X = 64;
    private static final int SHEET_MARGIN_Y = 28;
    /** The widest and tallest the sheet grows on a large screen, in GUI pixels. */
    private static final int MAX_SHEET_WIDTH = 620;
    private static final int MAX_SHEET_HEIGHT = 400;
    /** Space inside the sheet's edges, in GUI pixels. */
    private static final int PADDING = 10;
    /** The height of the header's controls, in GUI pixels. */
    private static final int ROW_HEIGHT = 17;
    /** Space between the header's controls, in GUI pixels. */
    private static final int GAP = 6;
    /** Widths in GUI pixels: the search field and the mod filter. */
    private static final int SEARCH_WIDTH = 150;
    private static final int MOD_WIDTH = 130;
    /** The search field's picture, as the creative inventory's search tab has it. */
    private static final Icon SEARCH = Icon.item("minecraft:spyglass");
    /** The mod filter's picture while it shows every mod. */
    private static final Icon ALL_MODS = Icon.own("all_mods");

    private final Screen parent;
    private final Picker picker;
    private final Consumer<String> onPick;
    private final List<String> mods;
    private Picks picks;
    private String query = "";
    /** The mod the grid is narrowed to, by its place in {@link #mods}; -1 for every mod. */
    private int mod = -1;
    private CardGrid grid;
    /** The sections for {@link #sectionsFor}, kept until the search, the mod or the picks changes. */
    private List<CardGrid.Section> sections;
    private List<Object> sectionsFor;

    /**
     * What a picker picks from and how.
     *
     * @param title      the screen's title
     * @param searchHint the search field's hint
     * @param allHeading the heading of every entry, as "All blocks"
     * @param entries    every entry, in the order shown
     * @param cardWidth  how wide a card is at least, in GUI pixels; the grid's columns share what is left over
     * @param cardHeight a card's height, in GUI pixels
     * @param cards      what a card shows
     * @param picture    an entry's picture, by id: the mod filter shows a mod's first entry's
     * @param modName    a mod's name for players, by namespace, for the mod filter
     * @param readPicks  reads the picker's file of picks and stars
     * @param savePicks  keeps picks and stars in it
     */
    public record Picker(Component title, Component searchHint, Component allHeading, List<Search.Entry> entries,
            int cardWidth, int cardHeight, CardGrid.Cards cards, Function<String, Icon> picture,
            Function<String, String> modName,
            Supplier<Picks> readPicks, Consumer<Picks> savePicks) {
        public Picker {
            entries = List.copyOf(entries);
        }
    }

    /** A picker going back to {@code parent}, handing a picked entry's id to {@code onPick}. */
    public PickerScreen(Screen parent, Picker picker, Consumer<String> onPick) {
        super(picker.title());
        this.parent = parent;
        this.picker = picker;
        this.onPick = onPick;
        this.mods = picker.entries().stream().map(Search.Entry::namespace).distinct().sorted().toList();
        this.picks = picker.readPicks().get();
    }

    /** The grid, as last laid out. */
    public CardGrid grid() {
        return grid;
    }

    /**
     * A sheet over the screen it came from: the title, then the search, the mod filter and Close at the right of the
     * header, and the grid below.
     */
    @Override
    protected List<Widget> layOut(int width, int height) {
        int sheetWidth = Math.min(MAX_SHEET_WIDTH, width - 2 * SHEET_MARGIN_X);
        int sheetHeight = Math.min(MAX_SHEET_HEIGHT, height - 2 * SHEET_MARGIN_Y);
        Rect sheet = new Rect((width - sheetWidth) / 2, (height - sheetHeight) / 2, sheetWidth, sheetHeight);
        int headerTop = sheet.y() + PADDING;
        Rect close = new Rect(sheet.right() - PADDING - ROW_HEIGHT, headerTop, ROW_HEIGHT, ROW_HEIGHT);
        Rect modPlace = new Rect(close.x() - GAP - MOD_WIDTH, headerTop, MOD_WIDTH, ROW_HEIGHT);
        Rect searchPlace = new Rect(modPlace.x() - GAP - SEARCH_WIDTH, headerTop, SEARCH_WIDTH, ROW_HEIGHT);
        Rect glass = new Rect(searchPlace.x() - Icon.SIZE - GAP / 2, headerTop, Icon.SIZE, ROW_HEIGHT);
        TextField search = new TextField("search", searchPlace, picker.searchHint(), query, this::search);
        TextButton modButton = new TextButton((canvas, x, y) -> modPicture().draw(canvas, x, y), this::modLabel,
                modPlace, this::nextMod, () -> true, List::of);
        int gridTop = headerTop + ROW_HEIGHT + PADDING;
        grid = new CardGrid(new Rect(sheet.x() + PADDING, gridTop, sheet.width() - 2 * PADDING,
                sheet.bottom() - PADDING - gridTop), picker.cardWidth(), picker.cardHeight(), this::sections,
                id -> picks.starred().contains(id), new IdAndMod(), this::pick, this::star);
        return List.of(new Surface(Look.SHEET, sheet),
                new Caption(this::getTitle, new Rect(sheet.x() + PADDING, headerTop, glass.x() - sheet.x() - PADDING,
                        ROW_HEIGHT), TextStyle.TITLE, Look.TEXT),
                new WidgetGroup(List.of(new Label(SEARCH, Component.empty(), glass), search, modButton)), grid,
                new PictureButton(Icon.line("cross"), close, this::onClose,
                        () -> List.of(CommonComponents.GUI_CANCEL)));
    }

    /** Nothing: the title is on the sheet. */
    @Override
    protected void drawBehind(Canvas canvas) {
    }

    /** The screen the picker came from, dimmed behind the sheet. */
    @Override
    protected Optional<Screen> behind() {
        return Optional.of(parent);
    }

    @Override
    public void onClose() {
        Screens.show(parent);
    }

    /** What the grid shows, worked out again only when the search, the mod or the picks changed. */
    private List<CardGrid.Section> sections() {
        List<Object> key = List.of(query, mod, picks);
        if (!key.equals(sectionsFor)) {
            sectionsFor = key;
            sections = workOutSections();
        }
        return sections;
    }

    /** What the grid shows: what a search found, or the starred, the recent and then every entry of the mod. */
    private List<CardGrid.Section> workOutSections() {
        List<Search.Entry> shown = mod < 0 ? picker.entries()
                : picker.entries().stream().filter(entry -> entry.namespace().equals(mods.get(mod))).toList();
        return PickSections.of(picker.entries(), shown, query, picks).stream()
                .map(section -> new CardGrid.Section(heading(section.kind()), section.entries())).toList();
    }

    /** A picker's cards, their tooltips saying each entry's id and mod before what the picker's cards add. */
    private final class IdAndMod implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            picker.cards().draw(canvas, entry, place);
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            List<Component> lines = new ArrayList<>(List.of(Component.literal(entry.id()),
                    Component.literal(picker.modName().apply(entry.namespace()))));
            lines.addAll(picker.cards().describe(entry));
            return lines;
        }
    }

    private Component heading(PickSections.Kind kind) {
        return switch (kind) {
            case FOUND -> Component.translatable("flatgen.picks.found");
            case STARRED -> Component.translatable("flatgen.picks.starred");
            case RECENT -> Component.translatable("flatgen.picks.recent");
            case ALL -> picker.allHeading();
        };
    }

    private void pick(String id) {
        picks = picks.used(id);
        picker.savePicks().accept(picks);
        onPick.accept(id);
        onClose();
    }

    private void star(String id) {
        picks = picks.toggleStar(id);
        picker.savePicks().accept(picks);
    }

    private void search(String text) {
        query = text;
        grid.toTop();
    }

    /** The mod filter's picture: every mod's, or the mod's first entry's. */
    private Icon modPicture() {
        return mod < 0 ? ALL_MODS : picker.entries().stream().filter(entry -> entry.namespace().equals(mods.get(mod)))
                .findFirst().map(entry -> picker.picture().apply(entry.id())).orElse(ALL_MODS);
    }

    private Component modLabel() {
        return mod < 0 ? Component.translatable("flatgen.picks.all_mods")
                : Component.literal(picker.modName().apply(mods.get(mod)));
    }

    private void nextMod() {
        mod = mod + 1 < mods.size() ? mod + 1 : -1;
        grid.toTop();
    }
}
