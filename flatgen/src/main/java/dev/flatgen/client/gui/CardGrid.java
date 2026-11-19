package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.GridSteps;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.Scrolling;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A grid of cards, as a picker's or the preset library's: sections of cards, each under its heading, scrolled with the
 * wheel a row of cards at a time. A left click picks a card's entry; in a grid with stars a right click stars or
 * unstars it and a starred card has a mark. What a card shows comes from {@link Cards}.
 */
public final class CardGrid implements Control {
    /** Space above a section's heading, in GUI pixels. */
    private static final int SECTION_GAP = 4;
    /** The height of a section's heading, in GUI pixels: a line of text and a little space under it. */
    private static final int HEADING_HEIGHT = 12;
    /** The mark on a starred card, an outlined star in its top right corner. */
    private static final Icon STAR = Icon.own("star");

    private final Rect bounds;
    private final int cardWidth;
    private final int cardHeight;
    private final Supplier<List<Section>> sections;
    private final Predicate<String> starred;
    private final Cards cards;
    private final Consumer<String> onPick;
    private final Consumer<String> onStar;
    private int scroll;
    /**
     * The entry whose card the keyboard is on while the grid has the focus, by its id, so it stays on it as the
     * sections change; the first card when none is or it is gone.
     */
    private Optional<String> focusedEntry = Optional.empty();
    /**
     * The card the keyboard is on, by its place among the cards, kept beside its entry as an entry can have more than
     * one card (starred, recent and among all): the place counts while its card is still that entry's.
     */
    private int focusedCard;
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
     * A grid in this rectangle of cards this size, of the sections given, which may change as the player searches;
     * a right click on a card stars or unstars it, and its tooltip says so.
     *
     * @param starred whether an entry is starred, by id
     * @param onPick  told the id a left click picks
     * @param onStar  told the id a right click stars or unstars
     */
    public CardGrid(Rect bounds, int cardWidth, int cardHeight, Supplier<List<Section>> sections,
            Predicate<String> starred, Cards cards, Consumer<String> onPick, Consumer<String> onStar) {
        this.bounds = bounds;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.sections = sections;
        this.starred = starred;
        this.cards = cards;
        this.onPick = onPick;
        this.onStar = onStar;
    }

    /** A section's heading where the grid draws it now. */
    private record Heading(Component text, int y) {
    }

    /** Where everything is drawn now: the headings and the cards, those scrolled out of sight included. */
    private record Placed(List<Heading> headings, List<Card> cards) {
    }

    /** Where the grid is drawn: the part of it in sight. */
    @Override
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
        now.headings().forEach(heading -> canvas.text(heading.text(), bounds.x(), heading.y(), Look.MUTED));
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
        if (card.isEmpty() || (button != MouseButtons.LEFT && button != MouseButtons.RIGHT)) {
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
        scroll = Scrolling.scrolled(scroll, amount, cardHeight, hidden());
        return true;
    }

    /**
     * The keys while the grid has the focus: the arrows move from card to card, Enter picks the card and Shift with
     * Enter stars it, as the mouse's buttons do.
     */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        List<Card> all = cards();
        if (all.isEmpty()) {
            return false;
        }
        if (Keys.uses(keyCode)) {
            (modifiers.shift() ? onStar : onPick).accept(focused(all).entry().id());
            reveal(bounds);
            return true;
        }
        OptionalInt next = GridSteps.next(all.stream().map(Card::place).toList(), focusedIndex(all),
                Keys.across(keyCode), Keys.down(keyCode));
        next.ifPresent(this::moveTo);
        return Keys.isArrow(keyCode);
    }

    /** The tooltip of the card the keyboard is on. */
    @Override
    public List<Component> tooltip() {
        List<Card> all = cards();
        return all.isEmpty() ? List.of() : describe(focused(all).entry());
    }

    /** The card the keyboard is on: its name, what its tooltip says, and whether it is starred. */
    @Override
    public Optional<Narration> narration() {
        List<Card> all = cards();
        return all.isEmpty() ? Optional.empty() : Optional.of(narration(focused(all).entry()));
    }

    @Override
    public Optional<Narration> narrationAt(double x, double y) {
        return cardAt(x, y).map(card -> narration(card.entry()));
    }

    /** A card's name, whether it is starred, and what its tooltip says of it. */
    private Narration narration(Search.Entry entry) {
        List<Component> more = new ArrayList<>(cards.describe(entry));
        if (starred.test(entry.id())) {
            more.add(0, Component.translatable("flatgen.picks.starred"));
        }
        return Narration.entry(Component.literal(entry.name()), more);
    }

    /** The ring goes round the card the keyboard is on, or the grid while it shows none. */
    @Override
    public Optional<Rect> focusArea() {
        List<Card> all = cards();
        return Optional.of(all.isEmpty() ? bounds : focused(all).place());
    }

    /** Puts the keyboard on a card, scrolling it into sight. */
    private void moveTo(int card) {
        Card moved = cards().get(card);
        focusedEntry = Optional.of(moved.entry().id());
        focusedCard = card;
        scroll = Scrolling.showing(scroll, moved.place(), bounds, hidden());
    }

    /** The card the keyboard is on, of these cards, the grid shows: its entry's, or else the first. */
    private Card focused(List<Card> all) {
        return all.get(focusedIndex(all));
    }

    /** The place of the card the keyboard is on: its own while it is still its entry's, else the entry's first card. */
    private int focusedIndex(List<Card> all) {
        boolean still = focusedCard < all.size()
                && focusedEntry.filter(all.get(focusedCard).entry().id()::equals).isPresent();
        int found = still ? focusedCard : focusedEntry.map(id -> indexOf(all, id)).orElse(-1);
        return Math.max(0, found);
    }

    /** Scrolls the card the keyboard is on into sight, as when the focus comes back to the grid. */
    @Override
    public void reveal(Rect area) {
        List<Card> all = cards();
        if (!all.isEmpty()) {
            scroll = Scrolling.showing(scroll, focused(all).place(), bounds, hidden());
        }
    }

    private static int indexOf(List<Card> all, String id) {
        for (int index = 0; index < all.size(); index++) {
            if (all.get(index).entry().id().equals(id)) {
                return index;
            }
        }
        return -1;
    }

    /** How far the cards reach below the grid's window when it is scrolled to the top, in GUI pixels. */
    private int hidden() {
        int bottom = cards().stream().mapToInt(card -> card.place().bottom()).max().orElse(bounds.y()) + scroll;
        return Math.max(0, bottom - bounds.bottom());
    }

    private void drawCard(Canvas canvas, Card card, boolean hovered) {
        Rect place = card.place();
        if (hovered) {
            canvas.sprite(Look.BUTTON_LIT, place);
        }
        cards.draw(canvas, card.entry(), place);
        if (starred.test(card.entry().id())) {
            STAR.draw(canvas, place.right() - Icon.SIZE, place.y());
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
        lines.add(Component.translatable(starred.test(entry.id()) ? "flatgen.picks.unstar" : "flatgen.picks.star"));
        return lines;
    }
}
