package dev.flatgen.blocks;

import dev.flatgen.config.ConfigFiles;
import dev.flatgen.search.rules.Picks;

/** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
public final class BlockPicksFile {
    private static final String FILE_NAME = "blocks.json";

    private BlockPicksFile() {
    }

    /** The picks as the file has them now. */
    public static Picks read() {
        return ConfigFiles.read(ConfigFiles.file(FILE_NAME), Picks::fromJson, Picks.EMPTY, Picks::toJson);
    }

    /** Keeps the picks in the file. */
    public static void save(Picks picks) {
        ConfigFiles.write(ConfigFiles.file(FILE_NAME), picks.toJson());
    }
}
