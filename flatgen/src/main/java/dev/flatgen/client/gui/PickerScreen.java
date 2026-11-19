package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.search.rules.PickSections;
import dev.flatgen.search.rules.Picks;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A picker: every entry of its kind as cards, the starred and recent ones first, found by typing and narrowed to one
 * mod. Picking one hands it on and goes back; the picks and stars are kept in the picker's own file for next time.
 * The block and the biome pickers are this screen, each with its {@link Picker}.
 */
public final class PickerScreen extends FlatgenScreen {
    /** Space below the grid for Cancel, in GUI pixels. */
    private static final int FOOTER = 32;
    private static final int MARGIN = 12;
    /** Where the search row starts, below the title, in GUI pixels. */
    private static final int ROW_TOP = 22;
    private static final int ROW_HEIGHT = 20;
    /** Space between the search row's controls, and between it and the grid, in GUI pixels. */
    private static final int GAP = 8;
    /** Widths in GUI pixels; the search and the mod button fit side by side on the narrowest screen, 320 wide. */
    private static final int SEARCH_WIDTH = 124;
    private static final int MOD_WIDTH = 140;
    private static final int CANCEL_WIDTH = 100;
    /** The headings of the found, starred and recent sections, by their translation keys; all's is the picker's. */
    private static final Map<PickSections.Kind, String> HEADINGS = Map.of(PickSections.Kind.FOUND,
            "flatgen.picks.found", PickSections.Kind.STARRED, "flatgen.picks.starred", PickSections.Kind.RECENT,
            "flatgen.picks.recent");
    /** The search field's picture, as the creative inventory's search tab has it. */
    private static final Icon SEARCH = Icon.item("minecraft:compass");
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
     * @param cardWidth  a card's width, in GUI pixels
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

    @Override
    protected List<Widget> layOut(int width, int height) {
        FlowLayout row = new FlowLayout(new Rect(MARGIN, ROW_TOP, width - 2 * MARGIN, 0), ROW_HEIGHT, GAP);
        Label lookingGlass = new Label(SEARCH, Component.empty(), row.next(Icon.SIZE));
        TextField search = new TextField("search", row.next(SEARCH_WIDTH), picker.searchHint(), query, this::search);
        TextButton modButton = new TextButton((canvas, x, y) -> modPicture().draw(canvas, x, y), this::modLabel,
                row.next(MOD_WIDTH), this::nextMod, () -> true, List::of);
        int gridTop = row.bottom() + GAP;
        grid = new CardGrid(new Rect(MARGIN, gridTop, width - 2 * MARGIN, height - gridTop - FOOTER),
                picker.cardWidth(), picker.cardHeight(), this::sections, id -> picks.starred().contains(id),
                new IdAndMod(), this::pick, this::star);
        return List.of(new WidgetGroup(List.of(lookingGlass, search, modButton)), grid,
                new TextButton(Icon.CROSS, CommonComponents.GUI_CANCEL,
                        new Rect((width - CANCEL_WIDTH) / 2, height - (FOOTER + ROW_HEIGHT) / 2, CANCEL_WIDTH,
                                ROW_HEIGHT), this::onClose));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
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
        return kind == PickSections.Kind.ALL ? picker.allHeading() : Component.translatable(HEADINGS.get(kind));
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
