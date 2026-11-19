package dev.flatgen.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The player's presets kept as files, one each, written whole. */
class PresetStoreTest {
    private static final Preset TALL = TestPresets.TALL_FLAT.withName("Tall Flat");

    @TempDir
    Path folder;

    @Test
    void keepsAPresetInAFileNamedAfterItAndReadsItBack() {
        PresetStore store = new PresetStore(folder.resolve("presets"));
        assertEquals(Optional.of("tall_flat.json"), store.saveNew(TALL));
        assertEquals(List.of(new PresetLibrary.Entry("tall_flat.json", PresetLibrary.Origin.PLAYER, Optional.of(TALL),
                List.of())), store.load());
    }

    @Test
    void renamesDuplicatesOverwritesAndDeletes() {
        PresetStore store = new PresetStore(folder);
        String file = store.saveNew(TALL).orElseThrow();
        assertEquals(Optional.of("tall_flat_2.json"), store.duplicate(TALL));
        assertEquals("Tall Flat 2", store.load().get(1).name());
        assertEquals(Optional.of("towering.json"), store.rename(file, TALL, "Towering"));
        assertEquals(List.of("tall_flat_2.json", "towering.json"), files(store));
        assertTrue(store.overwrite("towering.json", TALL.withName("Towering").withBiome("minecraft:desert")));
        assertEquals("minecraft:desert", store.load().get(1).preset().orElseThrow().biome());
        assertTrue(store.delete("towering.json"));
        assertEquals(List.of("tall_flat_2.json"), files(store));
    }

    @Test
    void readsABrokenFileAsAnEntryWithItsProblem() throws IOException {
        Files.writeString(folder.resolve("broken.json"), "{ not json");
        PresetLibrary.Entry broken = new PresetStore(folder).load().get(0);
        assertEquals(Optional.empty(), broken.preset());
        assertEquals(1, broken.problems().size());
    }

    @Test
    void leavesNoHalfWrittenFileBehind() throws IOException {
        PresetStore store = new PresetStore(folder);
        store.saveNew(TALL);
        store.overwrite("tall_flat.json", TALL.withBiome("minecraft:desert"));
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of("tall_flat.json"), files.map(path -> path.getFileName().toString()).toList());
        }
    }

    private static List<String> files(PresetStore store) {
        return store.load().stream().map(PresetLibrary.Entry::key).toList();
    }
}
