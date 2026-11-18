package dev.flatgen.search;

import dev.flatgen.config.ConfigFiles;
import dev.flatgen.search.rules.Picks;

/**
 * A picker's own file in config/flatgen: what it picked lately and starred. Each feature with a picker owns one, named
 * after it, as blocks.json and biomes.json.
 *
 * @param fileName the file's name within config/flatgen
 */
public record PicksFile(String fileName) {
    /** The picks as the file has them now; none when it is missing or cannot be read. */
    public Picks read() {
        return ConfigFiles.read(ConfigFiles.file(fileName), Picks::fromJson, Picks.EMPTY, Picks::toJson);
    }

    /** Keeps the picks in the file. */
    public void save(Picks picks) {
        ConfigFiles.write(ConfigFiles.file(fileName), picks.toJson());
    }
}
