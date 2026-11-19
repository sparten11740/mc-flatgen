package dev.flatgen.editor.client;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

/** What the editor checks of a pasted superflat code beyond reading it, and how it says what is wrong. */
final class CodeChecks {
    /** Each problem's words, by their translation keys. */
    private static final Map<VanillaCode.Kind, String> PROBLEMS = Map.of(
            VanillaCode.Kind.UNREADABLE_LAYER, "flatgen.code.unreadable_layer",
            VanillaCode.Kind.NO_LAYERS, "flatgen.code.no_layers",
            VanillaCode.Kind.UNKNOWN_BLOCK, "flatgen.code.unknown_block",
            VanillaCode.Kind.UNKNOWN_BIOME, "flatgen.code.unknown_biome",
            VanillaCode.Kind.TOO_TALL, "flatgen.code.too_tall");

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
        String key = PROBLEMS.get(problem.kind());
        Component said = problem.kind() == VanillaCode.Kind.TOO_TALL ? Component.translatable(key, worldHeight)
                : Component.translatable(key, problem.text(), problem.start() + 1, problem.end());
        return more > 0 ? Component.translatable("flatgen.code.more", said, more) : said;
    }
}
