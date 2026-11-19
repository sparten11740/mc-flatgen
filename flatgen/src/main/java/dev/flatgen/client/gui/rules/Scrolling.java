package dev.flatgen.client.gui.rules;

/** How far a widget that scrolls with the wheel is scrolled: from 0 at its top to as much as it hides. */
public final class Scrolling {
    /** How far one notch of the wheel scrolls, in GUI pixels: a block's height in the inventory. */
    public static final int WHEEL_STEP = 16;

    private Scrolling() {
    }

    /**
     * The scroll after the wheel turned by an amount, positive away from the player, which shows what is above; one
     * step a turn, whatever its size, kept between the top and as much as the widget hides.
     *
     * @param step   how far a turn scrolls, in GUI pixels
     * @param hidden how much of what the widget shows does not fit in it, in GUI pixels
     */
    public static int scrolled(int scroll, double amount, int step, int hidden) {
        return Math.max(0, Math.min(hidden, scroll - (int) Math.signum(amount) * step));
    }

    /**
     * The scroll that brings a part of what the widget shows into its window, moving as little as it can; the scroll
     * as it is when the part is in sight already.
     *
     * @param part   where the part is drawn at the scroll as it is
     * @param window the widget's window
     * @param hidden how much of what the widget shows does not fit in it, in GUI pixels
     */
    public static int showing(int scroll, Rect part, Rect window, int hidden) {
        int above = window.y() - part.y();
        int below = part.bottom() - window.bottom();
        int moved = above > 0 ? scroll - above : below > 0 ? scroll + below : scroll;
        return Math.max(0, Math.min(hidden, moved));
    }
}
