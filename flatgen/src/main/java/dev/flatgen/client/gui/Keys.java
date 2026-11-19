package dev.flatgen.client.gui;

import org.lwjgl.glfw.GLFW;

/** The keys Flatgen's controls answer alike, by the game's key codes. */
final class Keys {
    private Keys() {
    }

    /** Whether a key uses the control that has the focus, as a click would: Enter, the keypad's Enter or Space. */
    static boolean uses(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE;
    }

    /** Whether a key is an arrow, which moves the focus within an area when the control does not take it. */
    static boolean isArrow(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_LEFT
                || keyCode == GLFW.GLFW_KEY_RIGHT;
    }

    /** Whether an arrow moves forward, down or right, rather than back. */
    static boolean forward(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_RIGHT;
    }
}
