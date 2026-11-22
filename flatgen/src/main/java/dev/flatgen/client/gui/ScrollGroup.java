package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.Scrolling;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;

/**
 * Widgets laid out down a window of the screen, perhaps taller than it: drawn and handed the mouse only within it, and
 * scrolled by the wheel no widget takes, with a bar beside the window showing how far while there is more to see.
 */
public final class ScrollGroup implements Widget {
    /** How far inside the window's top and bottom the content starts and ends, in GUI pixels. */
    public static final int INSET = 2;
    /** The bar's width and its distance from the window, in GUI pixels. */
    private static final int BAR_WIDTH = 2;
    private static final int BAR_GAP = 2;
    /** The shortest the bar's handle is drawn, in GUI pixels. */
    private static final int SHORTEST_HANDLE = 8;
    private static final int BAR_TRACK = 0x40FFFFFF;
    private static final int BAR_HANDLE = 0xC0FFFFFF;
    /** Where no mouse is, handed to the widgets while the mouse is outside the window, so none looks hovered. */
    private static final int NOWHERE = Integer.MIN_VALUE / 2;

    private final Rect window;
    private final Content content;
    /** How far it is scrolled, kept where its owner says, so a group laid out anew starts where the last one was. */
    private final Position position;
    private WidgetGroup widgets;
    /** How tall the widgets are laid out, from the first one's top to the last one's bottom. */
    private int contentHeight;

    /** How far a scrolling group is scrolled, in GUI pixels, kept by its owner across the groups it lays out. */
    public static final class Position {
        private int scroll;
    }

    /** Lays the widgets out from a top Y down. */
    public interface Content {
        /** The widgets placed from this Y down, and the Y just below the last. */
        Laid layOut(int top);
    }

    /**
     * Widgets laid out.
     *
     * @param widgets the widgets, placed
     * @param bottom  the Y just below the last
     */
    public record Laid(List<Widget> widgets, int bottom) {
    }

    /** A window onto widgets laid out from its top down, starting at its top. */
    public ScrollGroup(Rect window, Content content) {
        this(window, content, new Position());
    }

    /** A window onto widgets laid out from its top down, scrolled as far as this position, as far as they reach. */
    public ScrollGroup(Rect window, Content content, Position position) {
        this.window = window;
        this.content = content;
        this.position = position;
        layOutAgain();
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean inside = window.contains(mouseX, mouseY);
        canvas.clip(window);
        widgets.draw(canvas, inside ? mouseX : NOWHERE, inside ? mouseY : NOWHERE);
        canvas.unclip();
        if (hidden() > 0) {
            drawBar(canvas);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return window.contains(x, y) && widgets.click(x, y, button, modifiers);
    }

    @Override
    public void drag(double x, double y) {
        widgets.drag(x, y);
    }

    @Override
    public void release(double x, double y) {
        widgets.release(x, y);
    }

    /** The wheel over the window: a widget's if one takes it, or else it scrolls the window. */
    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        if (!window.contains(x, y)) {
            return false;
        }
        if (widgets.scroll(x, y, amount, modifiers)) {
            return true;
        }
        int scrolled = Scrolling.scrolled(position.scroll, amount, Scrolling.WHEEL_STEP, hidden());
        if (scrolled != position.scroll) {
            position.scroll = scrolled;
            widgets.keyboard(false);
            layOut();
        }
        return true;
    }

    @Override
    public boolean takesKeyboard() {
        return widgets.takesKeyboard();
    }

    @Override
    public void keyboard(boolean held) {
        widgets.keyboard(held);
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return widgets.key(keyCode, modifiers);
    }

    @Override
    public boolean type(char character) {
        return widgets.type(character);
    }

    @Override
    public List<Widget> focusables() {
        return widgets.focusables();
    }

    @Override
    public Optional<Narration> narrationAt(double x, double y) {
        return window.contains(x, y) ? widgets.narrationAt(x, y) : Optional.empty();
    }

    /** Scrolls so an area of the content, as it lies with the content scrolled to its top, is in sight. */
    public void revealContent(Rect area) {
        reveal(new Rect(area.x(), area.y() - position.scroll, area.width(), area.height()));
    }

    /** Scrolls so that a part of the widgets is in the window, as far as it fits. */
    @Override
    public void reveal(Rect area) {
        int scrolled = Scrolling.showing(position.scroll, area, window, hidden());
        if (scrolled != position.scroll) {
            position.scroll = scrolled;
            layOut();
        }
    }

    @Override
    public List<WidgetDescription> describe() {
        return widgets.describe();
    }

    /**
     * Lays the content out again where it is scrolled to, as when it comes to take more or less room, scrolling back
     * no further than the content now reaches.
     */
    public void layOutAgain() {
        layOut();
        if (position.scroll > hidden()) {
            position.scroll = hidden();
            layOut();
        }
    }

    /** Whether one of its widgets has the keyboard from a click, as a field being typed in. */
    public boolean holdsKeyboard() {
        return widgets.holdsKeyboard();
    }

    /** Lays the content out a little inside the window's top, so its first border never touches the clip. */
    private void layOut() {
        int top = window.y() - position.scroll + INSET;
        Laid laid = content.layOut(top);
        widgets = new WidgetGroup(laid.widgets());
        contentHeight = laid.bottom() - top + 2 * INSET;
    }

    /** How much of the widgets the window cannot show at once, in GUI pixels. */
    private int hidden() {
        return Math.max(0, contentHeight - window.height());
    }

    private void drawBar(Canvas canvas) {
        Rect track = new Rect(window.right() + BAR_GAP, window.y(), BAR_WIDTH, window.height());
        int handleHeight = Math.max(SHORTEST_HANDLE, window.height() * window.height() / contentHeight);
        int handleY = track.y() + position.scroll * (track.height() - handleHeight) / hidden();
        canvas.fill(track, BAR_TRACK);
        canvas.fill(new Rect(track.x(), handleY, BAR_WIDTH, handleHeight), BAR_HANDLE);
    }
}
