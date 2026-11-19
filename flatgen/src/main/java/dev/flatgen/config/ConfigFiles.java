package dev.flatgen.config;

import dev.flatgen.Flatgen;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

/**
 * Reading and writing the files Flatgen keeps for the player in config/flatgen, each a feature's own: a missing file is
 * written with the defaults, so players find it, and an unreadable one is logged and read as the defaults.
 */
public final class ConfigFiles {
    /** The ending of a file being written, before it is moved over the real one. */
    private static final String TEMPORARY = ".tmp";

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

    /**
     * Writes a file whole or not at all: to a temporary file beside it, then moved over it, so a crash halfway never
     * leaves half a file; its folder is made first if needed, and a failure is logged.
     *
     * @return whether it was written
     */
    public static boolean write(Path file, String text) {
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), file.getFileName().toString(), TEMPORARY);
            Files.writeString(temporary, text);
            moveOver(temporary, file);
            return true;
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not write {}", file, exception);
            deleteQuietly(temporary);
            return false;
        }
    }

    /** Moves a file over another at once where the file system can, else replacing it. */
    private static void moveOver(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not delete {}", file, exception);
        }
    }
}
