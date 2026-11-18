package dev.flatgen.config;

import dev.flatgen.Flatgen;
import dev.flatgen.config.rules.Settings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * config/flatgen/flatgen.json, read each time Flatgen needs the player's settings, so a change to the file counts
 * without restarting the game. A missing file is written with the defaults, so players can find it.
 */
public final class SettingsFile {
    private static final String FILE_NAME = "flatgen.json";

    private SettingsFile() {
    }

    /** The player's settings as the file has them now; the defaults when it is missing or cannot be read. */
    public static Settings read() {
        return read(ConfigFolder.path().resolve(FILE_NAME));
    }

    /** The settings in this file, written with the defaults when it is missing; the defaults when it is unreadable. */
    static Settings read(Path file) {
        if (!Files.exists(file)) {
            write(file, Settings.DEFAULTS);
            return Settings.DEFAULTS;
        }
        try {
            return Settings.fromJson(Files.readString(file));
        } catch (IOException | IllegalArgumentException exception) {
            Flatgen.LOGGER.warn("Could not read {}, using the default settings", file, exception);
            return Settings.DEFAULTS;
        }
    }

    private static void write(Path file, Settings settings) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, settings.toJson());
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not write {}", file, exception);
        }
    }
}
