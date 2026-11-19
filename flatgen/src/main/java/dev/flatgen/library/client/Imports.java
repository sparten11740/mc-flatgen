package dev.flatgen.library.client;

import dev.flatgen.library.PresetImports;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/** Importing presets as the player sees it: what an import did, and the name a preset without one gets. */
final class Imports {
    private Imports() {
    }

    /** The name an imported preset without one gets, as a pasted superflat code, in the player's language. */
    static String unnamed() {
        return Component.translatable("flatgen.library.pasted_name").getString();
    }

    /** What an import did, in a line: the presets added, and each left out with why. */
    static Component summary(PresetImports.Outcome outcome) {
        List<String> parts = new ArrayList<>();
        if (!outcome.imported().isEmpty()) {
            parts.add(Component.translatable("flatgen.library.imported", String.join(", ", outcome.imported()))
                    .getString());
        }
        outcome.skipped().forEach(skipped -> parts.add(skippedLine(skipped)));
        return Component.literal(String.join(" · ", parts));
    }

    /** What was left out and why, the pasted text named as such. */
    private static String skippedLine(PresetImports.Skipped skipped) {
        Component what = skipped.file().<Component>map(Component::literal)
                .orElse(Component.translatable("flatgen.library.pasted_text"));
        return Component.translatable("flatgen.library.skipped", what, Component.translatable(skipped.reason().key()))
                .getString();
    }
}
