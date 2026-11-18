package dev.flatgen.editor.client;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

/** What the editor checks of a pasted superflat code beyond reading it, and how it says what is wrong. */
final class CodeChecks {
    private CodeChecks() {
    }

    /** The blocks and biome of a code read that the game lacks, each where the code first names it. */
    static List<VanillaCode.Problem> unknownIds(VanillaCode.Reading reading, Preset current,
            HolderLookup.Provider registries) {
        Preset pasted = current.withLayers(reading.layers()).withBiome(reading.biome());
        return VanillaCode.unknownIds(reading, FlatSettings.missingIds(pasted, registries));
    }

    /** A problem in the player's words, saying how many more there are. */
    static Component describe(VanillaCode.Problem problem, int more, int worldHeight) {
        String key = "flatgen.code." + problem.kind().name().toLowerCase(Locale.ROOT);
        Component said = problem.kind() == VanillaCode.Kind.TOO_TALL ? Component.translatable(key, worldHeight)
                : Component.translatable(key, problem.text(), problem.start() + 1, problem.end());
        return more > 0 ? Component.translatable("flatgen.code.more", said, more) : said;
    }
}
