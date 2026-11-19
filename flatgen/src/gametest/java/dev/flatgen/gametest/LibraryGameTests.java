package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.FlatPresets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;

/** The game's flat world presets as the library's built-ins (F13). */
final class LibraryGameTests {
    private LibraryGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("every flat preset the game shows converts without problems", LibraryGameTests::gamePresets);
        return tests;
    }

    private static void gamePresets(GameTestHelper helper) {
        List<PresetLibrary.Entry> presets = FlatPresets.visible(helper.getLevel().registryAccess());
        check(presets.stream().anyMatch(entry -> entry.key().equals("game:minecraft:classic_flat")),
                "Classic Flat is missing from " + presets.stream().map(PresetLibrary.Entry::key).toList());
        presets.forEach(entry -> check(entry.problems().isEmpty() && entry.preset().isPresent(),
                entry.key() + ": " + entry.problems()));
    }
}
