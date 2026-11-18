package dev.flatgen.config;

import dev.flatgen.config.rules.Settings;
import java.nio.file.Path;

/**
 * config/flatgen/flatgen.json, read each time Flatgen needs the player's settings, so a change to the file counts
 * without restarting the game.
 */
public final class SettingsFile {
    private static final String FILE_NAME = "flatgen.json";

    private SettingsFile() {
    }

    /** The player's settings as the file has them now; the defaults when it is missing or cannot be read. */
    public static Settings read() {
        return read(ConfigFiles.file(FILE_NAME));
    }

    /** The settings in this file, written with the defaults when it is missing; the defaults when it is unreadable. */
    static Settings read(Path file) {
        return ConfigFiles.read(file, Settings::fromJson, Settings.DEFAULTS, Settings::toJson);
    }
}
