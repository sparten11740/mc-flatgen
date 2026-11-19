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
    private int scroll;
    private WidgetGroup widgets;
    /** How tall the widgets are laid out, from the first one's top to the last one's bottom. */
    private int contentHeight;

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

    /** A window onto widgets laid out from its top down. */
    public ScrollGroup(Rect window, Content content) {
        this.window = window;
        this.content = content;
        layOut();
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
        int scrolled = Scrolling.scrolled(scroll, amount, Scrolling.WHEEL_STEP, hidden());
        if (scrolled != scroll) {
            scroll = scrolled;
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

    /** Scrolls so that a part of the widgets is in the window, as far as it fits. */
    @Override
    public void reveal(Rect area) {
        int scrolled = Scrolling.showing(scroll, area, window, hidden());
        if (scrolled != scroll) {
            scroll = scrolled;
            layOut();
        }
    }

    @Override
    public List<WidgetDescription> describe() {
        return widgets.describe();
    }

    /** Places the widgets for the scroll, which moves them up. */
    private void layOut() {
        int top = window.y() - scroll;
        Laid laid = content.layOut(top);
        widgets = new WidgetGroup(laid.widgets());
        contentHeight = laid.bottom() - top;
    }

    /** How much of the widgets the window cannot show at once, in GUI pixels. */
    private int hidden() {
        return Math.max(0, contentHeight - window.height());
    }

    private void drawBar(Canvas canvas) {
        Rect track = new Rect(window.right() + BAR_GAP, window.y(), BAR_WIDTH, window.height());
        int handleHeight = Math.max(SHORTEST_HANDLE, window.height() * window.height() / contentHeight);
        int handleY = track.y() + scroll * (track.height() - handleHeight) / hidden();
        canvas.fill(track, BAR_TRACK);
        canvas.fill(new Rect(track.x(), handleY, BAR_WIDTH, handleHeight), BAR_HANDLE);
    }
}
