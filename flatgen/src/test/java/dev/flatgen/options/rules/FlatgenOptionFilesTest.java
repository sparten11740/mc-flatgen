package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.pictures.rules.Picture;
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

    /** The time and weather group's pictures are Flatgen's own, and its heading has one of its own (F16). */
    @Test
    void timeAndWeatherShowFlatgensOwnPictures() throws IOException {
        WorldOption clear = read("always_clear");
        WorldOption day = read("always_day");
        List<Picture> pictures = List.of(clear.icon(), day.icon(), clear.groupIcon().orElseThrow());
        pictures.forEach(picture -> assertTrue(picture.texture() && Files.exists(texture(picture.id())),
                picture.id() + " is not a texture Flatgen ships"));
    }

    /** Where a texture is among Flatgen's resources, by its location. */
    private static Path texture(String location) {
        int colon = location.indexOf(':');
        return OPTIONS.resolve("../../../" + location.substring(0, colon) + "/" + location.substring(colon + 1))
                .normalize();
    }

    @Test
    void noMobsStopsMobSpawning() throws IOException {
        WorldOption option = read("no_mobs");
        assertEquals(List.of(new GameRule("doMobSpawning", "false")), option.effects());
        assertEquals("option.flatgen.no_mobs", option.name());
    }

    /**
     * No mobs is shown the other way round, a switch of a zombie on while mobs spawn, so a player who cannot read sees
     * mobs on as the world has them; its group's heading shows the zombie's head (F16).
     */
    @Test
    void noMobsShowsMobsSpawningTheOtherWayRound() throws IOException {
        WorldOption option = read("no_mobs");
        assertTrue(option.inverted() && !option.defaultOn());
        assertEquals(Picture.item("minecraft:zombie_spawn_egg"), option.icon());
        assertEquals(Picture.item("minecraft:zombie_head"), option.groupIcon().orElseThrow());
    }

    private static WorldOption read(String name) throws IOException {
        OptionReader.Reading reading = OptionReader.read("flatgen:" + name,
                Files.readString(OPTIONS.resolve(name + ".json")));
        assertEquals(List.of(), reading.problems());
        return reading.option().orElseThrow();
    }
}
