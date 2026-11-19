package dev.flatgen.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** The screen the game shows, and showing another: the game itself holds it up to 26.1, its GUI from 26.2 on. */
public final class Screens {
    private Screens() {
    }

    /** The screen shown now, if any. */
    public static Screen current() {
        //? if >=26.2 {
        /*return Minecraft.getInstance().gui.screen();
        *///?} else {
        return Minecraft.getInstance().screen;
        //?}
    }

    /** Shows a screen in place of the one shown now. */
    public static void show(Screen screen) {
        //? if >=26.2 {
        /*Minecraft.getInstance().gui.setScreen(screen);
        *///?} else {
        Minecraft.getInstance().setScreen(screen);
        //?}
    }
}
