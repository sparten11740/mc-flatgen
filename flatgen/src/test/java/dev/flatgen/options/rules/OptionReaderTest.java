package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Reading world option files, as mods and packs write them. */
class OptionReaderTest {
    private static final String ALWAYS_DAY = """
            {
              "icon": "minecraft:clock",
              "group": "flatgen:time_and_weather",
              "effects": [
                { "type": "game_rule", "rule": "doDaylightCycle", "value": false },
                { "type": "first_start_time", "time": 6000 }
              ]
            }""";
    private static final String AIRPORT = """
            {
              "format": 1,
              "name": "Airport",
              "icon": { "texture": "skyliners:textures/gui/flatgen/airport.png" },
              "default": true,
              "requires": ["skyliners"],
              "effects": [
                { "type": "place_at_spawn", "template": "skyliners:airport", "offset": [0, -1, 0],
                  "rotation": "clockwise_90", "spawn": [20, 1, 8] },
                { "type": "game_rule", "rule": "spawnRadius", "value": 0.0 },
                { "type": "structures", "sets": ["skyliners:airports"] }
              ]
            }""";

    @Test
    void readsAnOptionAndGivesWhatIsLeftOutItsDefaults() {
        WorldOption option = read("flatgen:always_day", ALWAYS_DAY);
        assertEquals("option.flatgen.always_day", option.name());
        assertEquals("option.flatgen.always_day.description", option.description());
        assertEquals(new WorldOption.Icon("minecraft:clock", false), option.icon());
        assertEquals("flatgen:time_and_weather", option.group());
        assertEquals(false, option.defaultOn());
        assertEquals(List.of(new GameRule("doDaylightCycle", "false"), new FirstStartTime(6000)), option.effects());
        assertTrue(option.actsAtFirstStart());
    }

    @Test
    void readsEveryKindOfEffectATextureIconAndRequiredMods() {
        WorldOption option = read("skyliners:airport", AIRPORT);
        assertEquals("Airport", option.name());
        assertEquals("skyliners", option.group());
        assertEquals(new WorldOption.Icon("skyliners:textures/gui/flatgen/airport.png", true), option.icon());
        assertEquals(List.of("skyliners"), option.requires());
        assertEquals(List.of(
                new PlaceAtSpawn("skyliners:airport", true, new Position(0, -1, 0), Rotation.CLOCKWISE_90, 64,
                        Optional.of(new Position(20, 1, 8))),
                new GameRule("spawnRadius", "0"),
                new AddStructures(List.of("skyliners:airports"))), option.effects());
    }

    @Test
    void leavesOutAWholeOptionWithAPartItCannotRead() {
        assertLeftOut("{\"icon\": \"minecraft:clock\", \"effects\": [{\"type\": \"weather\"}]}", "no effect type");
        assertLeftOut("{\"icon\": \"minecraft:clock\", \"effects\": [{\"type\": \"game_rule\", \"rule\": \"x\"},"
                + " {\"type\": \"first_start_time\", \"time\": 6000}]}", "a game_rule needs");
        assertLeftOut("{\"effects\": [{\"type\": \"first_start_time\", \"time\": 6000}]}", "it needs an icon");
        assertLeftOut("{\"icon\": \"minecraft:clock\", \"effects\": []}", "at least one");
        assertLeftOut("{\"format\": 2, \"icon\": \"minecraft:clock\", \"effects\": [{\"type\": \"structures\","
                + " \"sets\": [\"a:b\"]}]}", "later Flatgen");
        assertLeftOut("[1, 2]", "not a JSON object");
        assertLeftOut("{\"icon\": \"minecraft:clock\", \"effects\": [{\"type\": \"place_at_spawn\","
                + " \"template\": \"a:b\", \"offset\": [1, 2]}]}", "a place_at_spawn needs");
        assertLeftOut("{\"icon\": \"minecraft:clock\", \"effects\": [{\"type\": \"first_start_time\","
                + " \"time\": 24000}]}", "a first_start_time needs");
    }

    private static WorldOption read(String id, String json) {
        OptionReader.Reading reading = OptionReader.read(id, json);
        assertEquals(List.of(), reading.problems());
        return reading.option().orElseThrow();
    }

    private static void assertLeftOut(String json, String problem) {
        OptionReader.Reading reading = OptionReader.read("test:option", json);
        assertEquals(Optional.empty(), reading.option());
        assertTrue(reading.problems().stream().anyMatch(found -> found.contains(problem)),
                problem + " not in " + reading.problems());
    }
}
