package dev.flatgen.library;

import dev.flatgen.config.ConfigFiles;
import dev.flatgen.library.rules.Choices;

/**
 * config/flatgen/library.json, the library's own file: the favourite presets, those used lately and the default.
 * Written only when the player changes one, so a pack that ships the file later still has it read
 * (docs/pack-authors.md).
 */
public final class ChoicesFile {
    private static final String FILE_NAME = "library.json";

    private ChoicesFile() {
    }

    /** The choices as the file has them now. */
    public static Choices read() {
        return ConfigFiles.readIfThere(ConfigFiles.file(FILE_NAME), Choices::fromJson, Choices.NONE);
    }

    /** Keeps the choices in the file. */
    public static void save(Choices choices) {
        LibraryChanges.changed();
        ConfigFiles.write(ConfigFiles.file(FILE_NAME), choices.toJson());
    }
}
