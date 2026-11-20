package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.search.rules.PickSections;
import dev.flatgen.search.rules.Search;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A picker of one grid under its title: the starred, the recent and then every entry, or what a search found; a click
 * on a card picks it. The block picker is this screen.
 */
public final class GridPickerScreen extends PickerScreen {
    private final Component allHeading;
    private final int cardWidth;
    private final int cardHeight;
    private final CardGrid.Cards cards;

    /**
     * A picker going back to {@code parent}, handing a picked entry's id to {@code onPick}.
     *
     * @param allHeading the heading of every entry, as "All blocks"
     * @param cardWidth  how wide a card is at least, in GUI pixels; the grid's columns share what is left over
     * @param cardHeight a card's height, in GUI pixels
     * @param cards      what a card shows
     */
    public GridPickerScreen(Screen parent, Picker picker, Component allHeading, int cardWidth, int cardHeight,
            CardGrid.Cards cards, Consumer<String> onPick) {
        super(parent, picker, onPick);
        this.allHeading = allHeading;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.cards = cards;
    }

    /** The picker's title. */
    @Override
    protected List<Widget> header(Rect area) {
        return List.of(new Caption(this::getTitle, area, TextStyle.TITLE, Look.TEXT));
    }

    /** The grid, all of it. */
    @Override
    protected List<Widget> body(Rect area) {
        return List.of(grid(area, cardWidth, cardHeight, cards, this::pick));
    }

    @Override
    protected List<CardGrid.Section> sections(List<Search.Entry> shown, String searched) {
        return PickSections.of(entries(), shown, searched, picks()).stream()
                .map(section -> new CardGrid.Section(heading(section.kind()), section.entries())).toList();
    }

    private Component heading(PickSections.Kind kind) {
        return switch (kind) {
            case FOUND -> Component.translatable("flatgen.picks.found");
            case STARRED -> Component.translatable("flatgen.picks.starred");
            case RECENT -> Component.translatable("flatgen.picks.recent");
            case ALL -> allHeading;
        };
    }
}
