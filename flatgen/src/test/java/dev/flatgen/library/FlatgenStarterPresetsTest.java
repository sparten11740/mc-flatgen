package dev.flatgen.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.PresetReading;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Flatgen's own starter presets (F13), read from its resources as the library reads them. */
class FlatgenStarterPresetsTest {
    private static final Path PRESETS = Path.of(System.getProperty("flatgen.project"),
            "src/main/resources/assets/flatgen/flatgen/presets");
    /** The world's floor and the Y below which slimes spawn in slime chunks, 1.21.1's. */
    private static final int FLOOR = -64;
    private static final int SLIME_CHUNK_MAX_Y = 40;

    @Test
    void everyStarterReadsWithoutProblems() throws IOException {
        for (Path file : files()) {
            PresetReading reading = PresetJson.read(Files.readString(file));
            assertEquals(List.of(), reading.problems(), file.toString());
            assertTrue(reading.preset().name().startsWith("preset.flatgen."), file.toString());
        }
        assertEquals(4, files().size());
    }

    @Test
    void theTallFlatHasNoSlimes() throws IOException {
        PresetReading tall = PresetJson.read(Files.readString(PRESETS.resolve("tall_flat_no_slimes.json")));
        assertEquals(SLIME_CHUNK_MAX_Y, FLOOR + tall.preset().height());
    }

    private static List<Path> files() throws IOException {
        try (Stream<Path> files = Files.list(PRESETS)) {
            return files.sorted().toList();
        }
    }
}
