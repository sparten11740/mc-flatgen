package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import com.google.gson.JsonElement;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetReading;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** Presets as the game's superflat settings (F02): every game preset converts and back, and the layers generate. */
final class PresetGameTests {
    /** The stack most of the test instance's superflat worlds use: bedrock 1, stone 100, dirt 2, grass 1. */
    private static final Preset TALL_FLAT = new Preset("Tall flat", "minecraft:grass_block", List.of(
            new Layer("minecraft:bedrock", 1),
            new Layer("minecraft:stone", 100),
            new Layer("minecraft:dirt", 2),
            new Layer("minecraft:grass_block", 1)),
            Preset.DEFAULT_BIOME, Optional.of(List.of("minecraft:villages")), false, false, Map.of());

    private PresetGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("every game preset converts to a preset and back unchanged", PresetGameTests::everyGamePreset);
        tests.put("a preset's layers generate bottom up from the world's floor", PresetGameTests::layersGenerate);
        tests.put("ids the game lacks are found before the game swaps them", PresetGameTests::missingIdsAreFound);
        return tests;
    }

    private static void everyGamePreset(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        List<Holder.Reference<FlatLevelGeneratorPreset>> presets =
                registries.lookupOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).listElements().toList();
        check(!presets.isEmpty(), "the game has no superflat presets to convert");
        presets.forEach(preset -> convertsAndBack(preset, registries));
    }

    private static void convertsAndBack(Holder.Reference<FlatLevelGeneratorPreset> preset,
            HolderLookup.Provider registries) {
        String id = preset.key().location().toString();
        FlatLevelGeneratorSettings settings = preset.value().settings();
        PresetReading reading = FlatSettings.toPreset(settings, registries, id, "minecraft:grass_block");
        check(reading.problems().isEmpty(), id + " read with problems " + reading.problems());
        check(FlatSettings.missingIds(reading.preset(), registries).isEmpty(), id + " names ids the game lacks");
        FlatLevelGeneratorSettings back = FlatSettings.toSettings(reading.preset(), registries);
        check(json(settings, registries).equals(json(back, registries)),
                id + " came back as " + json(back, registries) + " rather than " + json(settings, registries));
    }

    private static void layersGenerate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FlatLevelGeneratorSettings settings = FlatSettings.toSettings(TALL_FLAT, level.registryAccess());
        NoiseColumn column = new FlatLevelSource(settings)
                .getBaseColumn(0, 0, level, level.getChunkSource().randomState());
        int y = level.getMinBuildHeight();
        for (Layer layer : TALL_FLAT.layers()) {
            for (int depth = 0; depth < layer.height(); depth++, y++) {
                String block = blockId(column.getBlock(y));
                check(block.equals(layer.block()), "at y=" + y + " the column has " + block + ", not " + layer.block());
            }
        }
        check(column.getBlock(y).isAir(), "above the top layer, at y=" + y + ", the column is not air");
    }

    private static void missingIdsAreFound(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        Preset fromMissingMods = new Preset("", "minecraft:stone",
                List.of(new Layer("minecraft:stone", 1), new Layer("nomod:glow_rock", 2)),
                "nomod:candy_land", Optional.of(List.of("minecraft:villages", "nomod:castles", "#nomod:towns")),
                false, false, Map.of());
        MissingIds missing = FlatSettings.missingIds(fromMissingMods, registries);
        check(missing.blocks().equals(List.of("nomod:glow_rock")), "missing blocks were " + missing.blocks());
        check(missing.biomes().equals(List.of("nomod:candy_land")), "missing biomes were " + missing.biomes());
        check(missing.structureSets().equals(List.of("nomod:castles", "#nomod:towns")),
                "missing structure sets were " + missing.structureSets());
        check(FlatSettings.missingIds(TALL_FLAT, registries).isEmpty(), "the tall flat preset names missing ids");
    }

    private static JsonElement json(FlatLevelGeneratorSettings settings, HolderLookup.Provider registries) {
        return FlatSettings.orThrow(
                FlatLevelGeneratorSettings.CODEC.encodeStart(FlatSettings.ops(registries), settings));
    }

    private static String blockId(BlockState state) {
        return state.getBlockHolder().unwrapKey().map(key -> key.location().toString()).orElse("?");
    }
}
