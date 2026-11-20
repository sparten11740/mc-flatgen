package dev.flatgen.client.gui;

import java.util.List;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
//? if >=26.3 {
/*import net.minecraft.client.gui.narration.NarrationTrigger;
*///?}

/**
 * What a screen hands the game's narrator, kept as plain words instead of said aloud, so tools that drive the screen
 * can check every control narrates its name and state without anyone listening.
 */
final class NarrationWords implements NarrationElementOutput {
    private final List<String> words;

    /** Words given to the narrator are added to this list, in the order given. */
    NarrationWords(List<String> words) {
        this.words = words;
    }

    @Override
    public void add(NarratedElementType type, NarrationThunk<?> said) {
        said.getText(words::add);
    }

    @Override
    public NarrationElementOutput nest() {
        return this;
    }

    //? if >=26.3 {
    /*@Override
    public NarrationTrigger narrationTrigger() {
        return NarrationTrigger.KEYBOARD;
    }
    *///?}
}
