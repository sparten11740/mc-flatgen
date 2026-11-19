package dev.flatgen.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.TestPresets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Presets brought into the player's folder from text and from dropped files. */
class PresetImportsTest {
    /** The name a preset without one gets, as the client gives it in English. */
    private static final String UNNAMED = "Pasted";

    @TempDir
    Path folder;

    @Test
    void importsTextUnderAFreeName() {
        PresetStore store = new PresetStore(folder.resolve("presets"));
        String json = PresetJson.write(TestPresets.TALL_FLAT.withName("Tall"));
        assertEquals(new PresetImports.Outcome(List.of("Tall"), List.of()),
                PresetImports.fromText(json, store, UNNAMED));
        assertEquals(new PresetImports.Outcome(List.of("Tall 2"), List.of()),
                PresetImports.fromText(json, store, UNNAMED));
        assertEquals(List.of("Pasted"),
                PresetImports.fromText("minecraft:stone;minecraft:plains", store, UNNAMED).imported());
        assertEquals(List.of(new PresetImports.Skipped(Optional.empty(), PresetImports.Reason.NOT_A_PRESET)),
                PresetImports.fromText("hello, world!", store, UNNAMED).skipped());
    }

    @Test
    void importsDroppedPresetFilesAndSaysWhyOthersWereLeftOut() throws IOException {
        PresetStore store = new PresetStore(folder.resolve("presets"));
        Path preset = Files.writeString(folder.resolve("tall.json"), PresetJson.write(TestPresets.TALL_FLAT
                .withName("Tall")));
        Path picture = Files.writeString(folder.resolve("tall.png"), "not a preset");
        Path broken = Files.writeString(folder.resolve("broken.json"), "{ not json");
        PresetImports.Outcome outcome = PresetImports.fromFiles(List.of(preset, picture, broken), store, UNNAMED);
        assertEquals(List.of("Tall"), outcome.imported());
        assertEquals(List.of(new PresetImports.Skipped(Optional.of("tall.png"), PresetImports.Reason.NOT_A_PRESET_FILE),
                new PresetImports.Skipped(Optional.of("broken.json"), PresetImports.Reason.NOT_A_PRESET)),
                outcome.skipped());
    }
}
