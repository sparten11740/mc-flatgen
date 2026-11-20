package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A picker that sorts its entries onto shelves down its side, as round 3 of docs/design/simple-editor.md drew the biome
 * picker: the grid beside shows the shelf chosen, or what a search found among every entry, and a picker may put rows
 * above it, as the block picker's recent and starred blocks. A player who cannot read browses by the shelves' pictures
 * instead of searching. Which shelves there are, and which is shown first, is each picker's own: the biome picker's
 * starred, recent, kinds and every biome; the block picker's every block and the creative inventory's tabs.
 */
public abstract class ShelvedPickerScreen extends PickerScreen {
    /** The side's width, a shelf's row on it, the space parting its groups and the space beside it, in GUI pixels. */
    private static final int SIDE_WIDTH = 92;
    private static final int SIDE_ROW = 18;
    private static final int SIDE_SEPARATOR = 6;
    private static final int GAP = 8;
    /** Pictures of the starred and the recent entries' shelves. */
    private static final Icon STARRED = Icon.own("starred");
    private static final Icon RECENT = Icon.line("recent");

    /** The side's groups of shelves, made the first time the sheet is laid out, when the picker knows its kinds. */
    private List<List<Shelf>> groups;
    /** The shelf the grid shows. */
    private Shelf shelf;

    /**
     * A shelf of the side.
     *
     * @param name    its name
     * @param picture its picture
     * @param of      its entries among those given, in their order
     */
    public record Shelf(Component name, Icon picture, Function<List<Search.Entry>, List<Search.Entry>> of) {
    }

    /** A picker going back to {@code parent}, handing a picked entry's id to {@code onPick}. */
    protected ShelvedPickerScreen(Screen parent, Picker picker, Consumer<String> onPick) {
        super(parent, picker, onPick);
    }

    /** The side's groups of shelves, in order, gaps parting them; asked once, when the sheet is first laid out. */
    protected abstract List<List<Shelf>> shelves();

    /** The shelf shown first, one of these groups'. */
    protected abstract Shelf shownFirst(List<List<Shelf>> side);

    /** What stands beside the side: the grid, made by {@code grid}, and anything more. */
    protected abstract List<Widget> beside(Rect area);

    /** What the grid shows while nothing is searched for: the shelf chosen; a picker may put rows above it. */
    protected List<CardGrid.Section> browsing(List<Search.Entry> shown, Shelf chosen) {
        return List.of(new CardGrid.Section(chosen.name(), chosen.of().apply(shown)));
    }

    /** The shelf of the starred entries, whatever the mod chosen, as a star shows wherever it is. */
    protected final Shelf starredShelf() {
        return new Shelf(Component.translatable("flatgen.picks.starred"), STARRED, shown -> starred());
    }

    /** The shelf of the entries picked lately, newest first, whatever the mod chosen. */
    protected final Shelf recentShelf() {
        return new Shelf(Component.translatable("flatgen.picks.recent"), RECENT, shown -> recent());
    }

    /** The starred entries, in the order they were starred. */
    protected final List<Search.Entry> starred() {
        return picked(entries(), picks().starred());
    }

    /** The entries picked lately, newest first. */
    protected final List<Search.Entry> recent() {
        return picked(entries(), picks().recent());
    }

    /** The side at the left, and what the picker puts beside it. */
    @Override
    protected final List<Widget> body(Rect area) {
        List<List<Shelf>> side = groups();
        List<Widget> widgets = new ArrayList<>(List.of(new WidgetGroup(rows(side,
                new Rect(area.x(), area.y(), SIDE_WIDTH, area.height())))));
        int besideX = area.x() + SIDE_WIDTH + GAP;
        widgets.addAll(beside(new Rect(besideX, area.y(), area.right() - besideX, area.height())));
        return widgets;
    }

    /** What the picker shows while browsing, or what a search found among every entry shown. */
    @Override
    protected final List<CardGrid.Section> sections(List<Search.Entry> shown, String searched) {
        groups();
        return searched.isBlank() ? browsing(shown, shelf)
                : List.of(new CardGrid.Section(Component.translatable("flatgen.picks.found"),
                        Search.find(shown, searched)));
    }

    /** The side's groups, made once, with the shelf shown first. */
    private List<List<Shelf>> groups() {
        if (groups == null) {
            groups = shelves();
            shelf = shownFirst(groups);
        }
        return groups;
    }

    /** A row for each shelf, gaps parting its groups. */
    private List<Widget> rows(List<List<Shelf>> side, Rect area) {
        List<Widget> rows = new ArrayList<>();
        int y = area.y() - SIDE_SEPARATOR;
        for (List<Shelf> group : side) {
            y += SIDE_SEPARATOR;
            for (Shelf each : group) {
                rows.add(new PictureTab(each.picture(), each::name, new Rect(area.x(), y, area.width(), SIDE_ROW),
                        () -> shelf == each, () -> show(each), () -> List.of(each.name())));
                y += SIDE_ROW;
            }
        }
        return rows;
    }

    /** Shows a shelf's entries, from the grid's top. */
    private void show(Shelf shown) {
        shelf = shown;
        showAnew();
    }

    /** The entries shown whose ids are among these, in these ids' order. */
    private static List<Search.Entry> picked(List<Search.Entry> shown, List<String> ids) {
        return ids.stream().flatMap(id -> shown.stream().filter(entry -> entry.id().equals(id)).limit(1)).toList();
    }
}
