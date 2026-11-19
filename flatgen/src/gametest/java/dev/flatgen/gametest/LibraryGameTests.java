package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.preset.rules.PresetReading;
import dev.flatgen.mc.FlatPresets;
import java.util.LinkedHashMap;
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
        Map<String, PresetReading> presets = FlatPresets.visible(helper.getLevel().registryAccess());
        check(presets.containsKey("minecraft:classic_flat"), "Classic Flat is missing from " + presets.keySet());
        presets.forEach((id, reading) -> check(reading.problems().isEmpty(), id + ": " + reading.problems()));
    }
}
