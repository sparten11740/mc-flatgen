package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Which options a preset has on, and options that cannot be on together. */
class OptionChoicesTest {
    private static final WorldOption ALWAYS_DAY = option("flatgen:always_day", false,
            new GameRule("doDaylightCycle", "false"), new FirstStartTime(6000));
    private static final WorldOption ALWAYS_NIGHT = option("flatgen:always_night", false,
            new GameRule("doDaylightCycle", "false"), new FirstStartTime(18000));
    private static final WorldOption FROZEN_CLOCK = option("pack:frozen_clock", false,
            new GameRule("doDaylightCycle", "false"));
    private static final WorldOption AIRPORT = option("skyliners:airport", true,
            new AddStructures(List.of("skyliners:airports")));
    private static final List<WorldOption> INSTALLED = List.of(ALWAYS_DAY, ALWAYS_NIGHT, FROZEN_CLOCK, AIRPORT);

    @Test
    void namesOnlyWhatDiffersFromTheDefaults() {
        Map<String, Boolean> chosen = OptionChoices.switched(Map.of(), ALWAYS_DAY, true, INSTALLED);
        chosen = OptionChoices.switched(chosen, AIRPORT, false, INSTALLED);
        assertEquals(Map.of("flatgen:always_day", true, "skyliners:airport", false), chosen);
        assertEquals(Map.of("skyliners:airport", false), OptionChoices.switched(chosen, ALWAYS_DAY, false, INSTALLED));
        assertEquals(List.of(ALWAYS_DAY), OptionChoices.on(chosen, INSTALLED));
    }

    @Test
    void turnsOffWhatClashesAndKeepsWhatAgrees() {
        assertTrue(ALWAYS_DAY.clashesWith(ALWAYS_NIGHT));
        assertFalse(ALWAYS_DAY.clashesWith(FROZEN_CLOCK));
        Map<String, Boolean> chosen = Map.of("flatgen:always_day", true, "pack:frozen_clock", true);
        assertEquals(Map.of("flatgen:always_night", true, "pack:frozen_clock", true),
                OptionChoices.switched(chosen, ALWAYS_NIGHT, true, INSTALLED));
    }

    @Test
    void keepsAndReportsOptionsNoLongerInstalled() {
        Map<String, Boolean> chosen = Map.of("gone:rockets", true, "flatgen:always_day", true);
        assertEquals(List.of("gone:rockets"), OptionChoices.missing(chosen, INSTALLED));
        assertEquals(Map.of("gone:rockets", true), OptionChoices.switched(chosen, ALWAYS_DAY, false, INSTALLED));
    }

    @Test
    void gathersTheGameRulesAndStructureSetsOfTheOptionsOn() {
        OptionChoices.Applied applied = OptionChoices.applied(List.of(ALWAYS_DAY, AIRPORT, FROZEN_CLOCK));
        assertEquals(Map.of("doDaylightCycle", "false"), applied.gameRules());
        assertEquals(Set.of("skyliners:airports"), applied.structureSets());
    }

    @Test
    void showsOptionsAsTheGameRulesAre() {
        Map<String, Boolean> chosen = Map.of("flatgen:always_day", true, "skyliners:airport", false);
        Map<String, String> daylightOn = Map.of("doDaylightCycle", "true", "doWeatherCycle", "true");
        assertEquals(Map.of("skyliners:airport", false),
                OptionChoices.followingGameRules(chosen, INSTALLED, daylightOn));
        assertEquals(Map.of("flatgen:always_day", true, "pack:frozen_clock", true, "skyliners:airport", false),
                OptionChoices.followingGameRules(chosen, INSTALLED, Map.of("doDaylightCycle", "false")));
    }

    @Test
    void turnsOnFromTheGameRulesOnlyOptionsThatDoNothingElse() {
        assertEquals(Map.of("pack:frozen_clock", true), OptionChoices.followingGameRules(Map.of(), INSTALLED,
                Map.of("doDaylightCycle", "false")));
    }

    private static WorldOption option(String id, boolean defaultOn, Effect... effects) {
        return new WorldOption(id, id, id, new WorldOption.Icon("minecraft:clock", false), "flatgen", defaultOn,
                List.of(), List.of(effects));
    }
}
