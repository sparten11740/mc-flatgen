package dev.flatgen.library;

import dev.flatgen.Flatgen;
import dev.flatgen.config.ConfigFiles;
import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.PresetReading;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The player's saved presets (F11): one file each in a folder, config/flatgen/presets, as F02's format writes them,
 * so they can be copied between instances; each written whole or not at all.
 */
public final class PresetStore implements PresetSource {
    private static final String FOLDER = "presets";
    private static final String JSON = ".json";

    private final Path folder;

    /** A store over this folder. */
    public PresetStore(Path folder) {
        this.folder = folder;
    }

    /** The folder the presets are kept in. */
    public Path folder() {
        return folder;
    }

    /** The player's own store, config/flatgen/presets. */
    public static PresetStore player() {
        return new PresetStore(ConfigFiles.file(FOLDER));
    }

    /** Every preset file of the folder, in name order; one that cannot be read is an entry with its problem, logged. */
    public List<PresetLibrary.Entry> load() {
        return paths().stream().map(this::read).toList();
    }

    @Override
    public List<PresetLibrary.Entry> presets() {
        return load();
    }

    /** Keeps a preset in a new file named after it; the file's name, none when it could not be written. */
    public Optional<String> saveNew(Preset preset) {
        String file = PresetLibrary.fileName(preset.name(), files());
        return write(file, preset) ? Optional.of(file) : Optional.empty();
    }

    /** Keeps a preset in place of what a file held; whether it was written. */
    public boolean overwrite(String file, Preset preset) {
        return write(file, preset);
    }

    /**
     * Gives a file's preset another name, in a file named after it, or in its own when the new file's name differs only
     * in case; the file's name, none when it could not be written, in which case the old file stays.
     */
    public Optional<String> rename(String file, Preset preset, String name) {
        List<String> others = files().stream().filter(other -> !other.equals(file)).toList();
        String named = PresetLibrary.fileName(name, others);
        String renamed = named.equalsIgnoreCase(file) ? file : named;
        if (!write(renamed, preset.withName(name))) {
            return Optional.empty();
        }
        if (!renamed.equals(file)) {
            delete(file);
        }
        return Optional.of(renamed);
    }

    /** Keeps a copy of a preset under the first free name after its own, as "Tall Flat 2"; the copy's file. */
    public Optional<String> duplicate(Preset preset) {
        List<String> names = load().stream().map(PresetLibrary.Entry::name).toList();
        return saveNew(preset.withName(PresetLibrary.newName(preset.name(), names)));
    }

    /** Deletes a preset's file; whether it is gone. */
    public boolean delete(String file) {
        try {
            Files.deleteIfExists(folder.resolve(file));
            LibraryChanges.changed();
            return true;
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not delete {}", folder.resolve(file), exception);
            return false;
        }
    }

    private boolean write(String file, Preset preset) {
        LibraryChanges.changed();
        return ConfigFiles.write(folder.resolve(file), PresetJson.write(preset));
    }

    private PresetLibrary.Entry read(Path path) {
        String file = path.getFileName().toString();
        try {
            PresetReading reading = PresetJson.read(Files.readString(path));
            reading.problems().forEach(problem -> Flatgen.LOGGER.warn("Preset {}: {}", path, problem));
            return new PresetLibrary.Entry(file, PresetLibrary.Origin.PLAYER, Optional.of(reading.preset()),
                    reading.problems());
        } catch (IOException | IllegalArgumentException exception) {
            Flatgen.LOGGER.warn("Could not read the preset {}", path, exception);
            return new PresetLibrary.Entry(file, PresetLibrary.Origin.PLAYER, Optional.empty(),
                    List.of(String.valueOf(exception.getMessage())));
        }
    }

    /** The names of the folder's preset files. */
    private List<String> files() {
        return paths().stream().map(path -> path.getFileName().toString()).toList();
    }

    /** The folder's preset files, in name order; none when it does not exist yet. */
    private List<Path> paths() {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> file.getFileName().toString().endsWith(JSON)).sorted().toList();
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not list {}", folder, exception);
            return List.of();
        }
    }
}
