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
 * picker: the starred and the recent entries, then each kind the picker has, then every entry, gaps parting the three;
 * the grid beside shows the shelf chosen, or what a search found among every entry. A player who cannot read browses
 * by the shelves' pictures instead of searching. The biome and the block pickers are this screen.
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

    /** A shelf for each kind of entry the picker has that holds any, in the order shown; the first is shown first. */
    protected abstract List<Shelf> kinds();

    /** The shelf of every entry, last on the side. */
    protected abstract Shelf every();

    /** What stands beside the side: the grid, made by {@code grid}, and anything more. */
    protected abstract List<Widget> beside(Rect area);

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

    /** The shelf's entries under its name, or what a search found among every entry shown. */
    @Override
    protected List<CardGrid.Section> sections(List<Search.Entry> shown, String searched) {
        groups();
        return List.of(searched.isBlank() ? new CardGrid.Section(shelf.name(), shelf.of().apply(shown))
                : new CardGrid.Section(Component.translatable("flatgen.picks.found"), Search.find(shown, searched)));
    }

    /** The side's groups: the picks, the kinds, every entry; made once, the first kind shown, else every entry. */
    private List<List<Shelf>> groups() {
        if (groups == null) {
            List<Shelf> kinds = kinds();
            Shelf all = every();
            groups = List.of(List.of(
                    new Shelf(Component.translatable("flatgen.picks.starred"), STARRED,
                            shown -> picked(shown, picks().starred())),
                    new Shelf(Component.translatable("flatgen.picks.recent"), RECENT,
                            shown -> picked(shown, picks().recent()))), kinds, List.of(all));
            shelf = kinds.isEmpty() ? all : kinds.get(0);
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
