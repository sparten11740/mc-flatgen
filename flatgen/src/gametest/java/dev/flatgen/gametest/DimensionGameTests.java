package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import com.google.gson.JsonParser;
import dev.flatgen.mc.Exports;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldTypes;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/** The Nether's and the End's own stacks (F22): their settings generate their layers, and a world preset keeps them. */
final class DimensionGameTests {
    private static final Stack NETHER = new Stack(List.of(new Layer("minecraft:bedrock", 1),
            new Layer("minecraft:netherrack", 3), new Layer("minecraft:soul_sand", 1)), "minecraft:soul_sand_valley");
    private static final Stack END = new Stack(List.of(new Layer("minecraft:end_stone", 2)), "minecraft:the_end");
    private static final Preset BOTH = new Preset("Three flats", "minecraft:grass_block",
            List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:grass_block", 1)), "minecraft:plains",
            Optional.of(List.of()), false, false, Map.of())
            .withStack(Dimension.NETHER, Optional.of(NETHER)).withStack(Dimension.END, Optional.of(END));

    private DimensionGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("a Nether stack generates its layers and biome", DimensionGameTests::netherGenerates);
        tests.put("a world preset gives the Nether and the End their stacks and keeps their types",
                DimensionGameTests::worldPresetKeepsStacks);
        tests.put("a world preset without stacks leaves the Nether and the End as the game makes them",
                DimensionGameTests::worldPresetWithoutStacks);
        tests.put("giving a flat Nether back puts its world type's generator back",
                DimensionGameTests::givingBackAfterDone);
        tests.put("parts the Nether and the End lack in this game are found", DimensionGameTests::missingIdsInStacks);
        return tests;
    }

    private static void netherGenerates(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FlatLevelSource generator = new FlatLevelSource(FlatSettings.toSettings(NETHER, level.registryAccess()));
        NoiseColumn column = generator.getBaseColumn(0, 0, level, level.getChunkSource().randomState());
        int y = level.getMinBuildHeight();
        for (Layer layer : NETHER.layers()) {
            for (int depth = 0; depth < layer.height(); depth++, y++) {
                String block = BuiltInRegistries.BLOCK.getKey(column.getBlock(y).getBlock()).toString();
                check(block.equals(layer.block()), "at y=" + y + " the column has " + block + ", not " + layer.block());
            }
        }
        String biome = generator.settings().getBiome().unwrapKey().map(key -> key.location().toString()).orElse("");
        check(biome.equals(NETHER.biome()), "the Nether's biome is " + biome);
    }

    private static void worldPresetKeepsStacks(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = dimensions(BOTH, registries);
        checkFlat(dimensions.get(LevelStem.NETHER), NETHER, registries, "the Nether");
        checkFlat(dimensions.get(LevelStem.END), END, registries, "the End");
        check(dimensions.get(LevelStem.NETHER).type().is(BuiltinDimensionTypes.NETHER), "the Nether's type changed");
        check(dimensions.get(LevelStem.END).type().is(BuiltinDimensionTypes.END), "the End's type changed");
    }

    private static void worldPresetWithoutStacks(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = dimensions(
                BOTH.withStack(Dimension.NETHER, Optional.empty()).withStack(Dimension.END, Optional.empty()),
                registries);
        check(!(dimensions.get(LevelStem.NETHER).generator() instanceof FlatLevelSource), "the Nether is superflat");
        check(!(dimensions.get(LevelStem.END).generator() instanceof FlatLevelSource), "the End is superflat");
    }

    private static void givingBackAfterDone(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Map<ResourceKey<LevelStem>, LevelStem> normal = WorldTypes.defaultDimensions(registries).orElseThrow();
        Map<ResourceKey<LevelStem>, LevelStem> flat = WorldTypes.withOtherDimensions(normal, normal,
                FlatSettings.otherDimensions(BOTH, registries));
        check(flat.get(LevelStem.NETHER).generator() instanceof FlatLevelSource, "Done left the Nether as it was");
        Map<ResourceKey<LevelStem>, LevelStem> givenBack = WorldTypes.withOtherDimensions(flat, normal,
                Map.of(Dimension.NETHER, Optional.empty()));
        check(givenBack.get(LevelStem.NETHER) == normal.get(LevelStem.NETHER),
                "the Nether given back is not its world type's");
        check(givenBack.get(LevelStem.END).generator() instanceof FlatLevelSource, "the End lost its stack");
    }

    private static void missingIdsInStacks(GameTestHelper helper) {
        Preset modded = BOTH.withStack(Dimension.NETHER, Optional.of(new Stack(List.of(new Layer("nomod:ash", 2)),
                "nomod:ash_wastes")));
        MissingIds missing = FlatSettings.missingIds(modded, helper.getLevel().registryAccess());
        check(missing.blocks().equals(List.of("nomod:ash")), "missing blocks were " + missing.blocks());
        check(missing.biomes().equals(List.of("nomod:ash_wastes")), "missing biomes were " + missing.biomes());
    }

    private static Map<ResourceKey<LevelStem>, LevelStem> dimensions(Preset preset,
            RegistryAccess registries) {
        String json = Exports.worldPreset(OptionsAtDone.settings(preset, registries, List.of()),
                FlatSettings.otherDimensions(preset, registries), registries);
        WorldPreset decoded = FlatSettings.orThrow(
                WorldPreset.DIRECT_CODEC.parse(FlatSettings.ops(registries), JsonParser.parseString(json)));
        return WorldTypes.byKey(decoded.createWorldDimensions());
    }

    private static void checkFlat(LevelStem stem, Stack stack, RegistryAccess registries, String name) {
        if (!(stem.generator() instanceof FlatLevelSource flat)) {
            throw FlatgenTests.failure(name + " is not superflat");
        }
        List<String> layers = flat.settings().getLayersInfo().stream().map(FlatLayerInfo::toString).toList();
        List<String> wanted = FlatSettings.toSettings(stack, registries).getLayersInfo().stream()
                .map(FlatLayerInfo::toString).toList();
        check(layers.equals(wanted), name + "'s layers are " + layers + ", not " + wanted);
    }
}
