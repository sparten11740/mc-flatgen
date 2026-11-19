package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.Scrolling;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A grid of cards, as a picker's or the preset library's: sections of cards, each under its heading, scrolled with the
 * wheel a row of cards at a time. A left click picks a card's entry; in a grid with stars a right click stars or
 * unstars it and a starred card has a mark. What a card shows comes from {@link Cards}.
 */
public final class CardGrid implements Widget {
    /** Space above a section's heading, in GUI pixels. */
    private static final int SECTION_GAP = 4;
    /** The height of a section's heading, in GUI pixels: a line of text and a little space under it. */
    private static final int HEADING_HEIGHT = 12;
    private static final int HEADING_COLOR = 0xFFA0A0A0;
    private static final int HOVER = 0x60FFFFFF;
    private static final int STAR = 0xFFFFD040;
    /** The size of the mark on a starred card, in GUI pixels. */
    private static final int STAR_SIZE = 3;

    private final Rect bounds;
    private final int cardWidth;
    private final int cardHeight;
    private final Supplier<List<Section>> sections;
    private final Predicate<String> starred;
    private final Cards cards;
    /** Whether cards can be starred: a right click then stars or unstars one, and its tooltip says so. */
    private final boolean withStars;
    private final Consumer<String> onPick;
    private final Consumer<String> onStar;
    private int scroll;
    /** The sections {@link #placed} was laid out for, at the scroll it had, and that layout. */
    private List<Section> placedSections;
    private int placedScroll;
    private Placed placed;

    /** What a picker's cards show. */
    public interface Cards {
        /** Draws an entry's card in its place. */
        void draw(Canvas canvas, Search.Entry entry, Rect place);

        /** What a card's tooltip says of its entry besides its name; nothing by default. */
        default List<Component> describe(Search.Entry entry) {
            return List.of();
        }
    }

    /**
     * A part of the grid under a heading.
     *
     * @param heading what it is called
     * @param entries its entries, in order
     */
    public record Section(Component heading, List<Search.Entry> entries) {
    }

    /**
     * An entry's card where the grid draws it now.
     *
     * @param entry the entry
     * @param place where its card is, on the screen
     */
    public record Card(Search.Entry entry, Rect place) {
    }

    /**
     * A grid with stars in this rectangle of cards this size, of the sections given, which may change as the player
     * searches.
     *
     * @param starred whether an entry is starred, by id
     * @param onPick  told the id a left click picks
     * @param onStar  told the id a right click stars or unstars
     */
    public CardGrid(Rect bounds, int cardWidth, int cardHeight, Supplier<List<Section>> sections,
            Predicate<String> starred, Cards cards, Consumer<String> onPick, Consumer<String> onStar) {
        this(bounds, cardWidth, cardHeight, sections, starred, cards, onPick, onStar, true);
    }

    /** A grid without stars in this rectangle of cards this size, of the sections given; a left click picks. */
    public CardGrid(Rect bounds, int cardWidth, int cardHeight, Supplier<List<Section>> sections, Cards cards,
            Consumer<String> onPick) {
        this(bounds, cardWidth, cardHeight, sections, id -> false, cards, onPick, id -> { }, false);
    }

    private CardGrid(Rect bounds, int cardWidth, int cardHeight, Supplier<List<Section>> sections,
            Predicate<String> starred, Cards cards, Consumer<String> onPick, Consumer<String> onStar,
            boolean withStars) {
        this.bounds = bounds;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.sections = sections;
        this.starred = starred;
        this.cards = cards;
        this.onPick = onPick;
        this.onStar = onStar;
        this.withStars = withStars;
    }

    /** A section's heading where the grid draws it now. */
    private record Heading(Component text, int y) {
    }

    /** Where everything is drawn now: the headings and the cards, those scrolled out of sight included. */
    private record Placed(List<Heading> headings, List<Card> cards) {
    }

    /** Where the grid is drawn: the part of it in sight. */
    public Rect bounds() {
        return bounds;
    }

    /** Where each card is drawn now, those scrolled out of sight included. */
    public List<Card> cards() {
        return placed().cards();
    }

    /** Starts the grid at its top again, as when what it shows changes. */
    public void toTop() {
        scroll = 0;
    }

    /** Where everything is drawn now, laid out again only when the sections or the scroll change. */
    private Placed placed() {
        List<Section> current = sections.get();
        if (current != placedSections || scroll != placedScroll) {
            placedSections = current;
            placedScroll = scroll;
            placed = layOut(current);
        }
        return placed;
    }

    private Placed layOut(List<Section> current) {
        List<Heading> headings = new ArrayList<>();
        List<Card> placedCards = new ArrayList<>();
        int columns = Math.max(1, bounds.width() / cardWidth);
        int y = bounds.y() - scroll;
        for (Section section : current) {
            headings.add(new Heading(section.heading(), y + SECTION_GAP));
            y += SECTION_GAP + HEADING_HEIGHT;
            for (int index = 0; index < section.entries().size(); index++) {
                Rect place = new Rect(bounds.x() + index % columns * cardWidth, y + index / columns * cardHeight,
                        cardWidth, cardHeight);
                placedCards.add(new Card(section.entries().get(index), place));
            }
            y += (section.entries().size() + columns - 1) / columns * cardHeight;
        }
        return new Placed(headings, placedCards);
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Placed now = placed();
        canvas.clip(bounds);
        now.headings().forEach(heading -> canvas.text(heading.text(), bounds.x(), heading.y(), HEADING_COLOR));
        for (Card card : now.cards()) {
            if (card.place().bottom() > bounds.y() && card.place().y() < bounds.bottom()) {
                drawCard(canvas, card, card.place().contains(mouseX, mouseY) && bounds.contains(mouseX, mouseY));
            }
        }
        canvas.unclip();
        cardAt(mouseX, mouseY).ifPresent(card -> canvas.tooltip(describe(card.entry()), mouseX, mouseY));
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        Optional<Card> card = cardAt(x, y);
        boolean starClick = withStars && button == MouseButtons.RIGHT;
        if (card.isEmpty() || (button != MouseButtons.LEFT && !starClick)) {
            return false;
        }
        (button == MouseButtons.LEFT ? onPick : onStar).accept(card.get().entry().id());
        return true;
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        if (!bounds.contains(x, y)) {
            return false;
        }
        int bottom = cards().stream().mapToInt(card -> card.place().bottom()).max().orElse(bounds.y()) + scroll;
        scroll = Scrolling.scrolled(scroll, amount, cardHeight, bottom - bounds.bottom());
        return true;
    }

    private void drawCard(Canvas canvas, Card card, boolean hovered) {
        Rect place = card.place();
        if (hovered) {
            canvas.fill(place, HOVER);
        }
        cards.draw(canvas, card.entry(), place);
        if (starred.test(card.entry().id())) {
            canvas.fill(new Rect(place.right() - STAR_SIZE - 1, place.y() + 1, STAR_SIZE, STAR_SIZE), STAR);
        }
    }

    private Optional<Card> cardAt(double x, double y) {
        if (!bounds.contains(x, y)) {
            return Optional.empty();
        }
        return cards().stream().filter(card -> card.place().contains(x, y)).findFirst();
    }

    private List<Component> describe(Search.Entry entry) {
        List<Component> lines = new ArrayList<>(List.of(Component.literal(entry.name())));
        lines.addAll(cards.describe(entry));
        if (withStars) {
            lines.add(Component.translatable(starred.test(entry.id()) ? "flatgen.picks.unstar" : "flatgen.picks.star"));
        }
        return lines;
    }
}
