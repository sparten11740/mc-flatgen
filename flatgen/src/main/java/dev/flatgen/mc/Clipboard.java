package dev.flatgen.mc;

import net.minecraft.client.Minecraft;

/** The system clipboard, through the game, for copying and pasting superflat codes. */
public final class Clipboard {
    private Clipboard() {
    }

    /** The text on the clipboard; empty when it holds none. */
    public static String get() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    /** Puts text on the clipboard. */
    public static void set(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
