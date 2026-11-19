package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.mc.WorldTypes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * The world types Flatgen edits besides Superflat (F18, F25): those it can edit whole, told from the game's and from
 * the fixture's, one with a flat overworld only, one with every dimension flat and one that changes the End some other
 * way.
 */
final class WorldTypeGameTests {
    private WorldTypeGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("world types Flatgen can edit whole are told from the others", WorldTypeGameTests::flatUnderneath);
        return tests;
    }

    private static void flatUnderneath(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        expect(registries, "minecraft:flat", true);
        expect(registries, "flatgen_fixture:airship_ready", true);
        expect(registries, "flatgen_fixture:all_flat", true);
        expect(registries, "minecraft:normal", false);
        expect(registries, "minecraft:large_biomes", false);
        expect(registries, "flatgen_fixture:end_sea", false);
    }

    private static void expect(HolderLookup.Provider registries, String type, boolean whole) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.parse(type));
        boolean found = WorldTypes.flatUnderneath(registries.lookupOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(key), registries);
        check(found == whole, type + (whole ? " is not told as" : " is told as") + " one Flatgen can edit whole");
    }
}
