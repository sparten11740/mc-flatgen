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
}
