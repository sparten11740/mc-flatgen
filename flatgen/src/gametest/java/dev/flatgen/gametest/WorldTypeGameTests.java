package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.mc.FlatWorldTypes;
import dev.flatgen.mc.WorldTypes;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetReading;
import dev.flatgen.preset.rules.Stack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * The world types Flatgen edits besides Superflat (F18, F25): those it can edit whole, told from the game's and from
 * the fixture's, one with a flat overworld only, one with every dimension flat, one that changes the End some other way
 * and one with a flat Nether but such an End; and those flat beyond the overworld, which the library offers as presets.
 */
final class WorldTypeGameTests {
    private WorldTypeGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("world types Flatgen can edit whole are told from the others", WorldTypeGameTests::editableWhole);
        tests.put("world types flat beyond the overworld are presets with their Nether and End",
                WorldTypeGameTests::presets);
        return tests;
    }

    private static void editableWhole(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        expect(registries, "minecraft:flat", true);
        expect(registries, "flatgen_fixture:airship_ready", true);
        expect(registries, "flatgen_fixture:all_flat", true);
        expect(registries, "minecraft:normal", false);
        expect(registries, "minecraft:large_biomes", false);
        expect(registries, "flatgen_fixture:end_sea", false);
        expect(registries, "flatgen_fixture:flat_nether_end_sea", false);
        //? if >=26.2 {
        /*expect(registries, "minecraft:flat_all_dimensions", true);
        *///?}
    }

    private static void presets(GameTestHelper helper) {
        Map<String, PresetReading> presets = FlatWorldTypes.presets(helper.getLevel().registryAccess());
        check(!presets.containsKey("minecraft:flat") && !presets.containsKey("flatgen_fixture:airship_ready"),
                "a world type flat in the overworld only is offered as a preset");
        Preset allFlat = Optional.ofNullable(presets.get("flatgen_fixture:all_flat")).map(PresetReading::preset)
                .orElseThrow(() -> new IllegalStateException("the fixture's all flat world type is not offered"));
        check(allFlat.icon().equals("minecraft:grass_block"), "the preset's icon is not its top block");
        check(topBlock(allFlat, Dimension.NETHER).equals("minecraft:netherrack")
                && topBlock(allFlat, Dimension.END).equals("minecraft:end_stone"),
                "the preset does not carry the world type's Nether and End");
        //? if >=26.2 {
        /*check(presets.containsKey("minecraft:flat_all_dimensions"), "the game's Flat All Dimensions is not offered");
        *///?}
    }

    private static String topBlock(Preset preset, Dimension dimension) {
        List<Layer> layers = preset.stack(dimension).map(Stack::layers).orElse(List.of());
        return layers.isEmpty() ? "" : layers.get(layers.size() - 1).block();
    }

    private static void expect(HolderLookup.Provider registries, String type, boolean whole) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.parse(type));
        boolean found = WorldTypes.editableWhole(registries.lookupOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(key), registries);
        check(found == whole, type + (whole ? " is not told as" : " is told as") + " one Flatgen can edit whole");
    }
}
