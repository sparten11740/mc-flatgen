package dev.flatgen.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.TestPresets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Presets brought into the player's folder from text and from dropped files. */
class PresetImportsTest {
    @TempDir
    Path folder;

    @Test
    void importsTextUnderAFreeName() {
        PresetStore store = new PresetStore(folder.resolve("presets"));
        String json = PresetJson.write(TestPresets.TALL_FLAT.withName("Tall"));
        assertEquals(new PresetImports.Outcome(List.of("Tall"), List.of()), PresetImports.fromText(json, store));
        assertEquals(new PresetImports.Outcome(List.of("Tall 2"), List.of()), PresetImports.fromText(json, store));
        assertEquals(List.of("Pasted"), PresetImports.fromText("minecraft:stone;minecraft:plains", store).imported());
    }

    @Test
    void importsDroppedPresetFilesAndSaysWhyOthersWereLeftOut() throws IOException {
        PresetStore store = new PresetStore(folder.resolve("presets"));
        Path preset = Files.writeString(folder.resolve("tall.json"), PresetJson.write(TestPresets.TALL_FLAT
                .withName("Tall")));
        Path picture = Files.writeString(folder.resolve("tall.png"), "not a preset");
        Path broken = Files.writeString(folder.resolve("broken.json"), "{ not json");
        PresetImports.Outcome outcome = PresetImports.fromFiles(List.of(preset, picture, broken), store);
        assertEquals(List.of("Tall"), outcome.imported());
        assertEquals(List.of("tall.png: not a preset file", "broken.json: the text is not JSON"), outcome.skipped());
    }
}
