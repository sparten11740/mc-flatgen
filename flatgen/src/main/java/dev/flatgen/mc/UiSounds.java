package dev.flatgen.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/** The game's interface sounds, which Flatgen's widgets play as the game's own do. */
public final class UiSounds {
    private UiSounds() {
    }

    /** The click of a button. */
    public static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }
}
