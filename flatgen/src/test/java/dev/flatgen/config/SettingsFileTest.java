package dev.flatgen.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.config.rules.Settings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The settings file on disk: written with defaults when missing, read as written, ignored when unreadable. */
class SettingsFileTest {
    @TempDir
    Path folder;

    @Test
    void writesTheDefaultsWhenTheFileIsMissing() throws IOException {
        Path file = folder.resolve("config").resolve("flatgen").resolve("flatgen.json");
        assertEquals(Settings.DEFAULTS, SettingsFile.read(file));
        assertEquals(Settings.DEFAULTS, Settings.fromJson(Files.readString(file)));
    }

    @Test
    void readsWhatThePlayerWrote() throws IOException {
        Path file = folder.resolve("flatgen.json");
        Files.writeString(file, "{\"enabled\": false}");
        assertEquals(new Settings(false, false), SettingsFile.read(file));
    }

    @Test
    void readsWhatItWrote() {
        Path file = folder.resolve("flatgen.json");
        Settings advanced = Settings.DEFAULTS.withAdvanced(true);
        assertTrue(SettingsFile.write(file, advanced));
        assertEquals(advanced, SettingsFile.read(file));
    }

    @Test
    void usesTheDefaultsAndKeepsAnUnreadableFile() throws IOException {
        Path file = folder.resolve("flatgen.json");
        Files.writeString(file, "enabled = false");
        assertEquals(Settings.DEFAULTS, SettingsFile.read(file));
        assertEquals("enabled = false", Files.readString(file));
    }
}
