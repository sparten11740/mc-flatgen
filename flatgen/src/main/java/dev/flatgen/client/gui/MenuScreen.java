package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.Screens;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A menu over the screen it came from, opened from a button there: rows on a raised panel under the button, groups of
 * them parted by lines. A row that does something closes the menu first; one that stays, such as a switch, keeps it
 * open to be turned over again. A click outside the panel and Escape close it.
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

    private final Screen parent;
    private final Rect button;
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
     * @param button where the button that opened it is, in GUI pixels
     */
    public MenuScreen(Component title, Screen parent, Rect button, List<List<Row>> groups) {
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

    /** The panel under the button, its right edge at the button's, kept on the screen. */
    @Override
    protected List<Widget> layOut(int width, int height) {
        int rows = groups.stream().mapToInt(List::size).sum();
        int panelHeight = 2 * PADDING + rows * ROW + (groups.size() - 1) * SEPARATOR;
        int x = Math.max(PageFrame.MARGIN, Math.min(button.right(), width - PageFrame.MARGIN) - WIDTH);
        panel = new Rect(x, button.bottom() + BELOW, WIDTH, panelHeight);
        List<Widget> widgets = new ArrayList<>(List.of(new Surface(Look.TOOLTIP, panel)));
        int y = panel.y() + PADDING;
        for (int index = 0; index < groups.size(); index++) {
            y = addGroup(widgets, groups.get(index), y);
            if (index < groups.size() - 1) {
                Rect line = new Rect(panel.x() + PADDING, y + SEPARATOR / 2, WIDTH - 2 * PADDING, 1);
                widgets.add((canvas, mouseX, mouseY) -> canvas.fill(line, Look.DIVIDER));
                y += SEPARATOR;
            }
        }
        return widgets;
    }

    /** Adds a group's rows from this height; the height after them. */
    private int addGroup(List<Widget> widgets, List<Row> group, int top) {
        int y = top;
        for (Row row : group) {
            widgets.add(row.at(new Rect(panel.x() + PADDING, y, WIDTH - 2 * PADDING, ROW), this::onClose));
            y += ROW;
        }
        return y;
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
