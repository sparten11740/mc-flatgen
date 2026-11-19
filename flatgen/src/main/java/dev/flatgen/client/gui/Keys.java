package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;

/** The keys Flatgen's controls answer alike, by the game's key codes. */
final class Keys {
    private Keys() {
    }

    /** Whether a key uses the control that has the focus, as a click would: Enter, the keypad's Enter or Space. */
    static boolean uses(int keyCode) {
        return keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER
                || keyCode == InputConstants.KEY_SPACE;
    }

    /** Whether a key is an arrow, which moves the focus within an area when the control does not take it. */
    static boolean isArrow(int keyCode) {
        return keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_DOWN
                || keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_RIGHT;
    }

    /** Whether an arrow moves forward, down or right, rather than back. */
    static boolean forward(int keyCode) {
        return keyCode == InputConstants.KEY_DOWN || keyCode == InputConstants.KEY_RIGHT;
    }

    /** Which way an arrow moves across: 1 for Right, -1 for Left, 0 for any other key. */
    static int across(int keyCode) {
        return keyCode == InputConstants.KEY_RIGHT ? 1 : keyCode == InputConstants.KEY_LEFT ? -1 : 0;
    }

    /** Which way an arrow moves up or down: 1 for Down, -1 for Up, 0 for any other key. */
    static int down(int keyCode) {
        return keyCode == InputConstants.KEY_DOWN ? 1 : keyCode == InputConstants.KEY_UP ? -1 : 0;
    }
}
