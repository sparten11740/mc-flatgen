package dev.flatgen.exports;

import dev.flatgen.config.ConfigFiles;
import dev.flatgen.exports.rules.DatapackFiles;
import dev.flatgen.exports.rules.ServerProperties;
import dev.flatgen.mc.Exports;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Preset;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.core.HolderLookup;

/**
 * A preset shared for a world made without Flatgen (F21): the lines a dedicated server's {@code server.properties}
 * needs, or a datapack, kept in config/flatgen/exports, that adds it as a World Type. Both make the world Done would,
 * with the structure sets its world options add, of a preset this game can make (its missing parts replaced by the
 * caller, as when it is used); the options' game rules and first-start effects, which neither can carry, are left
 * out.
 */
public final class PresetExports {
    /** The folder of config/flatgen exports are written to. */
    private static final String FOLDER = "exports";
    private static final String ZIP = ".zip";

    private PresetExports() {
    }

    /** The folder exports are written to. */
    public static Path folder() {
        return ConfigFiles.file(FOLDER);
    }

    /** The {@code server.properties} lines for a preset. */
    public static String serverLines(Preset preset, HolderLookup.Provider registries, List<WorldOption> installed) {
        return ServerProperties.lines(Exports.generatorSettings(OptionsAtDone.settings(preset, registries, installed),
                registries));
    }

    /**
     * Writes a preset's datapack to the exports folder as {@code <id>.zip}, replacing an earlier export of that name;
     * its file, none when it could not be written.
     *
     * @param id the world preset's path and the zip's name: lower-case letters, digits and underscores
     */
    public static Optional<Path> datapack(Preset preset, String id, HolderLookup.Provider registries,
            List<WorldOption> installed) {
        String worldPreset = Exports.worldPreset(OptionsAtDone.settings(preset, registries, installed),
                FlatSettings.otherDimensions(preset, registries), registries);
        Map<String, String> files = DatapackFiles.of(new DatapackFiles.Pack(Exports.PACK_FORMATS, id, preset.name(),
                worldPreset));
        Path zip = folder().resolve(id + ZIP);
        return ConfigFiles.write(zip, zipped(files)) ? Optional.of(zip) : Optional.empty();
    }

    /** Files, by path, as a zip's bytes; made in memory, which never fails to be written to. */
    private static byte[] zipped(Map<String, String> files) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return bytes.toByteArray();
    }
}
