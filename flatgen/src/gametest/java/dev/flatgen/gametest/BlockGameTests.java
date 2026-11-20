package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.search.rules.Search;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.flag.FeatureFlags;

/** The block picker's blocks and what each is like as a layer (F05), as the game's own blocks are. */
final class BlockGameTests {
    private BlockGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("the picker lists the game's blocks but air", BlockGameTests::catalog);
        tests.put("blocks are what they are like as layers", BlockGameTests::traits);
        return tests;
    }

    private static void catalog(GameTestHelper helper) {
        List<Search.Entry> blocks = BlockCatalog.blocks(FeatureFlags.DEFAULT_FLAGS);
        check(blocks.stream().anyMatch(block -> block.id().equals("minecraft:stone")
                && block.name().equals("Stone") && block.namespace().equals("minecraft")), "stone is not listed");
        check(blocks.stream().noneMatch(block -> block.id().equals("minecraft:air")), "air is listed");
    }

    private static void traits(GameTestHelper helper) {
        traitsAre("minecraft:sand", new LayerHints.Traits(true, true, false, true));
        traitsAre("minecraft:suspicious_gravel", new LayerHints.Traits(true, true, true, true));
        traitsAre("minecraft:stone", new LayerHints.Traits(false, true, false, true));
        traitsAre("minecraft:water", new LayerHints.Traits(false, false, false, false));
        traitsAre("minecraft:torch", new LayerHints.Traits(false, false, false, true));
        traitsAre("minecraft:chest", new LayerHints.Traits(false, false, true, true));
        traitsAre("minecraft:short_grass", new LayerHints.Traits(false, false, false, false));
    }

    private static void traitsAre(String block, LayerHints.Traits expected) {
        LayerHints.Traits traits = BlockCatalog.traits(block);
        check(traits.equals(expected), block + " is " + traits + ", not " + expected);
    }
}
