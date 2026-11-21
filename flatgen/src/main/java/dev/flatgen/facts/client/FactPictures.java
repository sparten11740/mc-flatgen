package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.facts.rules.Fact;
import java.util.List;
import net.minecraft.network.chat.Component;

/** How a fact's picture and words are drawn, alike in the editor's panel and on the biome picker's cards. */
public final class FactPictures {
    private FactPictures() {
    }

    /** A fact's picture, struck through when it says no. */
    public static Icon icon(Fact.Shown shown) {
        Icon icon = Icon.of(shown.icon());
        return shown.no() ? Icon.struck(icon) : icon;
    }

    /** What a fact shows, as a badge draws it: its picture and its words. */
    public static Badge.Look look(Fact.Shown shown) {
        return new Badge.Look(icon(shown), words(shown));
    }

    /** A fact's words, a line each, in the game's language. */
    public static List<Component> words(Fact.Shown shown) {
        return shown.words().stream()
                .<Component>map(words -> Component.translatable(words.key(), words.args().toArray()))
                .toList();
    }
}
