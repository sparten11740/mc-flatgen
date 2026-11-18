package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * The game's superflat code (F06): Flatgen writes each of the game's presets as the game writes it, and reads it back
 * to the same settings. The game's own parser is a client class, not on the GameTest server; its writer joins its
 * layers' own text, which this compares against.
 */
final class CodeGameTests {
    private CodeGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("every game preset's code is written as the game writes it and read back", CodeGameTests::codes);
        return tests;
    }

    private static void codes(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        registries.lookupOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).listElements()
                .forEach(preset -> writesAndReads(preset, registries));
    }

    private static void writesAndReads(Holder.Reference<FlatLevelGeneratorPreset> preset,
            HolderLookup.Provider registries) {
        String id = preset.key().location().toString();
        FlatLevelGeneratorSettings settings = preset.value().settings();
        Preset flatgen = FlatSettings.toPreset(settings, registries, id, "minecraft:stone").preset();
        String code = VanillaCode.write(flatgen);
        String gameCode = settings.getLayersInfo().stream().map(Object::toString).collect(Collectors.joining(","))
                + ";" + settings.getBiome().unwrapKey().orElseThrow().location();
        check(code.equals(gameCode), id + " is written " + code + " where the game writes " + gameCode);
        VanillaCode.Reading reading = VanillaCode.read(code);
        check(reading.problems().isEmpty(), id + "'s code reads with problems " + reading.problems());
        check(reading.layers().equals(flatgen.layers()) && reading.biome().equals(flatgen.biome()),
                id + "'s code reads back as other layers or another biome");
    }
}
