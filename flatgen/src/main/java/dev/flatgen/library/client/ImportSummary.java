package dev.flatgen.library.client;

import dev.flatgen.library.PresetImports;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/** What an import of presets did, in a line for the player: the presets added, and each left out with why. */
final class ImportSummary {
    private ImportSummary() {
    }

    /** The line for an import's outcome. */
    static Component of(PresetImports.Outcome outcome) {
        List<String> parts = new ArrayList<>();
        if (!outcome.imported().isEmpty()) {
            parts.add(Component.translatable("flatgen.library.imported", String.join(", ", outcome.imported()))
                    .getString());
        }
        outcome.skipped().forEach(skipped -> parts.add(Component.translatable("flatgen.library.skipped", skipped)
                .getString()));
        return Component.literal(String.join(" · ", parts));
    }
}
