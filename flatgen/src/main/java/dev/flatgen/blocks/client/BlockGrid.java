package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.Scrolling;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * The block picker's grid: sections of block icons, each under its heading, scrolled with the wheel. A left click picks
 * a block, a right click stars or unstars it; hovering shows its name, id and mod.
 */
public final class BlockGrid implements Widget {
    /** The size of a block's slot, in GUI pixels: its icon and a pixel around it. */
    private static final int SLOT = 18;
    /** Space above a section's heading, in GUI pixels. */
    private static final int SECTION_GAP = 4;
    /** The height of a section's heading, in GUI pixels: a line of text and a little space under it. */
    private static final int HEADING_HEIGHT = 12;
    private static final int HEADING_COLOR = 0xFFA0A0A0;
    private static final int HOVER = 0x60FFFFFF;
    private static final int STAR = 0xFFFFD040;
    /** The size of the mark on a starred block's slot, in GUI pixels. */
    private static final int STAR_SIZE = 3;

    private final Rect bounds;
    private final Supplier<List<Section>> sections;
    private final Predicate<String> starred;
    private final Function<String, Component> modName;
    private final Consumer<String> onPick;
    private final Consumer<String> onStar;
    private int scroll;
    /** The sections {@link #placed} was laid out for, at the scroll it had, and that layout. */
    private List<Section> placedSections;
    private int placedScroll;
    private Placed placed;

    /**
     * A part of the grid under a heading.
     *
     * @param heading what it is called
     * @param blocks  its blocks, in order
     */
    public record Section(Component heading, List<Search.Entry> blocks) {
    }

    /**
     * A block's slot where the grid draws it now.
     *
     * @param block the block
     * @param place where its slot is, on the screen
     */
    public record Slot(Search.Entry block, Rect place) {
    }

    /**
     * A grid in this rectangle of the sections given, which may change as the player searches.
     *
     * @param starred whether a block is starred
     * @param modName a mod's name for players, by namespace
     * @param onPick  told the block a left click picks
     * @param onStar  told the block a right click stars or unstars
     */
    public BlockGrid(Rect bounds, Supplier<List<Section>> sections, Predicate<String> starred,
            Function<String, Component> modName, Consumer<String> onPick, Consumer<String> onStar) {
        this.bounds = bounds;
        this.sections = sections;
        this.starred = starred;
        this.modName = modName;
        this.onPick = onPick;
        this.onStar = onStar;
    }

    /** A section's heading where the grid draws it now. */
    private record Heading(Component text, int y) {
    }

    /** Where everything is drawn now: the headings and the slots, those scrolled out of sight included. */
    private record Placed(List<Heading> headings, List<Slot> slots) {
    }

    /** Where the grid is drawn: the part of it in sight. */
    public Rect bounds() {
        return bounds;
    }

    /** Where each block's slot is drawn now, those scrolled out of sight included. */
    public List<Slot> slots() {
        return placed().slots();
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
        List<Slot> slots = new ArrayList<>();
        int columns = Math.max(1, bounds.width() / SLOT);
        int y = bounds.y() - scroll;
        for (Section section : current) {
            headings.add(new Heading(section.heading(), y + SECTION_GAP));
            y += SECTION_GAP + HEADING_HEIGHT;
            for (int index = 0; index < section.blocks().size(); index++) {
                Rect place = new Rect(bounds.x() + index % columns * SLOT, y + index / columns * SLOT, SLOT, SLOT);
                slots.add(new Slot(section.blocks().get(index), place));
            }
            y += (section.blocks().size() + columns - 1) / columns * SLOT;
        }
        return new Placed(headings, slots);
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Placed placed = placed();
        canvas.clip(bounds);
        placed.headings().forEach(heading -> canvas.text(heading.text(), bounds.x(), heading.y(), HEADING_COLOR));
        for (Slot slot : placed.slots()) {
            if (slot.place().bottom() > bounds.y() && slot.place().y() < bounds.bottom()) {
                drawSlot(canvas, slot, slot.place().contains(mouseX, mouseY) && bounds.contains(mouseX, mouseY));
            }
        }
        canvas.unclip();
        slotAt(mouseX, mouseY).ifPresent(slot -> canvas.tooltip(describe(slot.block()), mouseX, mouseY));
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        Optional<Slot> slot = slotAt(x, y);
        if (slot.isEmpty() || (button != MouseButtons.LEFT && button != MouseButtons.RIGHT)) {
            return false;
        }
        (button == MouseButtons.LEFT ? onPick : onStar).accept(slot.get().block().id());
        return true;
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        if (!bounds.contains(x, y)) {
            return false;
        }
        int bottom = slots().stream().mapToInt(slot -> slot.place().bottom()).max().orElse(bounds.y()) + scroll;
        scroll = Scrolling.scrolled(scroll, amount, SLOT, bottom - bounds.bottom());
        return true;
    }

    private void drawSlot(Canvas canvas, Slot slot, boolean hovered) {
        Rect place = slot.place();
        if (hovered) {
            canvas.fill(place, HOVER);
        }
        canvas.blockIcon(slot.block().id(), place.x() + 1, place.y() + 1);
        if (starred.test(slot.block().id())) {
            canvas.fill(new Rect(place.right() - STAR_SIZE - 1, place.y() + 1, STAR_SIZE, STAR_SIZE), STAR);
        }
    }

    private Optional<Slot> slotAt(double x, double y) {
        if (!bounds.contains(x, y)) {
            return Optional.empty();
        }
        return slots().stream().filter(slot -> slot.place().contains(x, y)).findFirst();
    }

    private List<Component> describe(Search.Entry block) {
        String star = starred.test(block.id()) ? "flatgen.blocks.unstar" : "flatgen.blocks.star";
        return List.of(Component.literal(block.name()), Component.literal(block.id()), modName.apply(block.namespace()),
                Component.translatable(star));
    }
}
