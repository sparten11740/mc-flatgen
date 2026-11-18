package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Flatgen's own world options (F10), read from its resources as the game reads them. */
class FlatgenOptionFilesTest {
    private static final Path OPTIONS = Path.of(System.getProperty("flatgen.project"),
            "src/main/resources/assets/flatgen/flatgen/options");

    @Test
    void alwaysDayStopsTheClockAtMidday() throws IOException {
        WorldOption option = read("always_day");
        assertEquals(List.of(new GameRule("doDaylightCycle", "false"), new FirstStartTime(6000)), option.effects());
        assertEquals("flatgen:time_and_weather", option.group());
    }

    @Test
    void alwaysClearStopsTheWeather() throws IOException {
        assertEquals(List.of(new GameRule("doWeatherCycle", "false")), read("always_clear").effects());
    }

    @Test
    void noMobsStopsMobSpawning() throws IOException {
        WorldOption option = read("no_mobs");
        assertEquals(List.of(new GameRule("doMobSpawning", "false")), option.effects());
        assertEquals("option.flatgen.no_mobs", option.name());
    }

    private static WorldOption read(String name) throws IOException {
        OptionReader.Reading reading = OptionReader.read("flatgen:" + name,
                Files.readString(OPTIONS.resolve(name + ".json")));
        assertEquals(List.of(), reading.problems());
        return reading.option().orElseThrow();
    }
}
