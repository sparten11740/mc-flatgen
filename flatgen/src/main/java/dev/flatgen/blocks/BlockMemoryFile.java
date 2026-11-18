package dev.flatgen.blocks;

import dev.flatgen.blocks.rules.BlockMemory;
import dev.flatgen.config.ConfigFiles;

/** config/flatgen/blocks.json, the blocks feature's own file: the blocks picked lately and starred. */
public final class BlockMemoryFile {
    private static final String FILE_NAME = "blocks.json";

    private BlockMemoryFile() {
    }

    /** The memory as the file has it now. */
    public static BlockMemory read() {
        return ConfigFiles.read(ConfigFiles.file(FILE_NAME), BlockMemory::fromJson, BlockMemory.EMPTY,
                BlockMemory::toJson);
    }

    /** Keeps the memory in the file. */
    public static void save(BlockMemory memory) {
        ConfigFiles.write(ConfigFiles.file(FILE_NAME), memory.toJson());
    }
}
