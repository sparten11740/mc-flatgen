package dev.flatgen.editor.client;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

/** What the editor checks of a pasted superflat code beyond reading it, and how it says what is wrong. */
final class CodeChecks {
    private CodeChecks() {
    }

    /** The blocks and biome of a code read that the game lacks, each where the code names it. */
    static List<VanillaCode.Problem> unknownIds(String code, VanillaCode.Reading reading, Preset current,
            HolderLookup.Provider registries) {
        Preset pasted = current.withLayers(reading.layers()).withBiome(reading.biome());
        MissingIds missing = FlatSettings.missingIds(pasted, registries);
        List<VanillaCode.Problem> problems = new ArrayList<>();
        missing.blocks().forEach(id -> problems.add(at(VanillaCode.Kind.UNKNOWN_BLOCK, code, id)));
        missing.biomes().forEach(id -> problems.add(at(VanillaCode.Kind.UNKNOWN_BIOME, code, id)));
        return problems;
    }

    /** A problem in the player's words, saying how many more there are. */
    static Component describe(VanillaCode.Problem problem, int more, int worldHeight) {
        String key = "flatgen.code." + problem.kind().name().toLowerCase(Locale.ROOT);
        Component said = problem.kind() == VanillaCode.Kind.TOO_TALL ? Component.translatable(key, worldHeight)
                : Component.translatable(key, problem.text(), problem.start() + 1, problem.end());
        return more > 0 ? Component.translatable("flatgen.code.more", said, more) : said;
    }

    private static VanillaCode.Problem at(VanillaCode.Kind kind, String code, String id) {
        int start = Math.max(0, code.indexOf(id));
        return new VanillaCode.Problem(kind, start, start + id.length(), id);
    }
}
