package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.Screens;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A menu over the screen it came from, opened from a button there: rows on a raised panel under the button, groups of
 * them parted by lines, scrolling when they are more than fit below the button. A row that does something closes the
 * menu first; one that stays, such as a switch, keeps it open to be turned over again. A click outside the panel and
 * Escape close it.
 */
public final class MenuScreen extends FlatgenScreen {
    /** The panel's width, a row's height and the space inside the panel's edges, in GUI pixels. */
    private static final int WIDTH = 160;
    private static final int ROW = 20;
    private static final int PADDING = 3;
    /** The height of the line parting two groups, with the space around it, in GUI pixels. */
    private static final int SEPARATOR = 7;
    /** The space between the button and the panel under it, in GUI pixels. */
    private static final int BELOW = 2;
    /** The room the rows leave at their right for the bar of a menu that scrolls, in GUI pixels. */
    private static final int SCROLL_BAR_ROOM = 4;

    private final Screen parent;
    /** Where the button that opened the menu is now, as the screen behind lays it out again when the window changes. */
    private final Supplier<Rect> button;
    private final List<List<Row>> groups;
    /** The panel as last laid out, outside which a click closes the menu. */
    private Rect panel = new Rect(0, 0, 0, 0);

    /** A row of a menu, made for its place in the panel, with what closes the menu. */
    @FunctionalInterface
    public interface Row {
        /** The row's widget in this place. */
        Widget at(Rect place, Runnable close);
    }

    /**
     * A menu titled so, the narrator's name for it, under {@code button} on {@code parent}, of these groups of rows.
     *
     * @param button where the button that opened it is, in GUI pixels, as {@code parent} last laid it out
     */
    public MenuScreen(Component title, Screen parent, Supplier<Rect> button, List<List<Row>> groups) {
        super(title);
        this.parent = parent;
        this.button = button;
        this.groups = List.copyOf(groups);
    }

    /**
     * A row with this picture and name that closes the menu and then does {@code action}.
     *
     * @param usable whether it can be used now
     * @param tip    what it does
     */
    public static Row action(Icon icon, Component label, Component tip, BooleanSupplier usable, Runnable action) {
        return (place, close) -> new MenuItem(icon, () -> label, place, () -> closeThen(close, action), usable,
                () -> List.of(label, tip));
    }

    /** A row that keeps the menu open, such as a switch: the widget made for its place. */
    public static Row staying(Function<Rect, Widget> widget) {
        return (place, close) -> widget.apply(place);
    }

    /**
     * The panel under the button, its right edge at the button's, kept on the screen: its rows as they are when they
     * fit below the button, else in a window that scrolls, the window's inset keeping the first row where it would be.
     */
    @Override
    protected List<Widget> layOut(int width, int height) {
        int rows = groups.stream().mapToInt(List::size).sum();
        int contentHeight = rows * ROW + (groups.size() - 1) * SEPARATOR;
        Rect under = button.get();
        int x = Math.max(PageFrame.MARGIN, Math.min(under.right(), width - PageFrame.MARGIN) - WIDTH);
        int top = under.bottom() + BELOW;
        boolean fits = 2 * PADDING + contentHeight <= height - PageFrame.MARGIN - top;
        panel = new Rect(x, top, WIDTH, fits ? 2 * PADDING + contentHeight : height - PageFrame.MARGIN - top);
        List<Widget> widgets = new ArrayList<>(List.of(new Surface(Look.TOOLTIP, panel)));
        if (fits) {
            widgets.addAll(rows(panel.y() + PADDING, WIDTH - 2 * PADDING).widgets());
        } else {
            int inset = PADDING - ScrollGroup.INSET;
            widgets.add(new ScrollGroup(new Rect(panel.x() + PADDING, panel.y() + inset,
                    WIDTH - 2 * PADDING - SCROLL_BAR_ROOM, panel.height() - 2 * inset),
                    rowsTop -> rows(rowsTop, WIDTH - 2 * PADDING - SCROLL_BAR_ROOM)));
        }
        return widgets;
    }

    /** The groups' rows this wide from a height down, lines parting the groups, and the height after them. */
    private ScrollGroup.Laid rows(int top, int rowWidth) {
        List<Widget> widgets = new ArrayList<>();
        int y = top;
        for (int index = 0; index < groups.size(); index++) {
            for (Row row : groups.get(index)) {
                widgets.add(row.at(new Rect(panel.x() + PADDING, y, rowWidth, ROW), this::onClose));
                y += ROW;
            }
            if (index < groups.size() - 1) {
                Rect line = new Rect(panel.x() + PADDING, y + SEPARATOR / 2, rowWidth, 1);
                widgets.add((canvas, mouseX, mouseY) -> canvas.fill(line, Look.DIVIDER));
                y += SEPARATOR;
            }
        }
        return new ScrollGroup.Laid(widgets, y);
    }

    /** A click outside the panel closes the menu. */
    @Override
    protected boolean click(double x, double y, int button) {
        if (!panel.contains(x, y)) {
            onClose();
            return true;
        }
        return super.click(x, y, button);
    }

    /** Nothing: the menu has no title on the screen. */
    @Override
    protected void drawBehind(Canvas canvas) {
    }

    /** The screen the menu came from, dimmed behind it. */
    @Override
    protected Optional<Screen> behind() {
        return Optional.of(parent);
    }

    @Override
    public void onClose() {
        Screens.show(parent);
    }

    private static void closeThen(Runnable close, Runnable action) {
        close.run();
        action.run();
    }
}
