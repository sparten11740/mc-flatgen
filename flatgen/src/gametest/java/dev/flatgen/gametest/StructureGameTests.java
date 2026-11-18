package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.Structures;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.structures.rules.StructureSetInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/** Structures (F09) held to the game's own flat generator: which structure sets it keeps for a preset. */
final class StructureGameTests {
    private static final String PLAINS = "minecraft:plains";
    private static final long SEED = 42L;

    private StructureGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("the biome's structures are those the game keeps for the biome",
                StructureGameTests::theBiomesAreTheGames);
        tests.put("a chosen list keeps what can appear in the biome, and none keeps none",
                StructureGameTests::chosenAndNone);
        return tests;
    }

    /** Every set Flatgen says can appear on plains, and no other, is one the game keeps for a plains world. */
    private static void theBiomesAreTheGames(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Set<String> appearing = Structures.all(registries).stream().filter(set -> set.appearsIn(PLAINS))
                .map(StructureSetInfo::id).collect(Collectors.toSet());
        Set<String> kept = kept(registries, Optional.empty());
        check(kept.equals(appearing), "the game keeps " + kept + ", Flatgen says " + appearing);
    }

    private static void chosenAndNone(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Set<String> kept = kept(registries, Optional.of(List.of("minecraft:villages", "minecraft:desert_pyramids")));
        check(kept.equals(Set.of("minecraft:villages")), "a chosen list on plains keeps " + kept);
        check(kept(registries, Optional.of(List.of())).isEmpty(), "none keeps structures");
    }

    /** The structure sets the game's flat generator keeps for a plains preset with this choice. */
    private static Set<String> kept(RegistryAccess registries, Optional<List<String>> sets) {
        Preset preset = new Preset("", "", List.of(new Layer("minecraft:stone", 1)), PLAINS, sets, false, false,
                Map.of());
        FlatLevelSource generator = new FlatLevelSource(FlatSettings.toSettings(preset, registries));
        RandomState random = RandomState.create(NoiseGeneratorSettings.dummy(),
                registries.lookupOrThrow(Registries.NOISE), SEED);
        return generator.createState(registries.lookupOrThrow(Registries.STRUCTURE_SET), random, SEED)
                .possibleStructureSets().stream()
                .flatMap(set -> set.unwrapKey().stream())
                .map(key -> key.location().toString())
                .collect(Collectors.toSet());
    }
}
