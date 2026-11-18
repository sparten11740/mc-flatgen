package dev.flatgen.config;

import dev.flatgen.Flatgen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Reading and writing the files Flatgen keeps for the player in config/flatgen, each a feature's own: a missing file is
 * written with the defaults, so players find it, and an unreadable one is logged and read as the defaults.
 */
public final class ConfigFiles {
    private ConfigFiles() {
    }

    /** A file of config/flatgen by its name. */
    public static Path file(String name) {
        return ConfigFolder.path().resolve(name);
    }

    /**
     * What a file holds, read by {@code parse}, which throws {@link IllegalArgumentException} for text it cannot read;
     * a missing file is written with the defaults, by {@code write}.
     */
    public static <T> T read(Path file, Function<String, T> parse, T defaults, Function<T, String> write) {
        if (!Files.exists(file)) {
            write(file, write.apply(defaults));
            return defaults;
        }
        try {
            return parse.apply(Files.readString(file));
        } catch (IOException | IllegalArgumentException exception) {
            Flatgen.LOGGER.warn("Could not read {}, using the defaults", file, exception);
            return defaults;
        }
    }

    /** Writes a file, making config/flatgen first if needed; a failure is logged. */
    public static void write(Path file, String text) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, text);
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not write {}", file, exception);
        }
    }
}
