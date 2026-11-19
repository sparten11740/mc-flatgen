package dev.flatgen.exports.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** A datapack's files: the world preset under Flatgen's namespace, listed among the World Types, and its name. */
class DatapackFilesTest {
    private static final DatapackFiles.Pack PACK = new DatapackFiles.Pack(
            new DatapackFiles.PackFormats(34, 48, 0), "tall_flat", "Tall flat", "{\"dimensions\":{}}");

    @Test
    void holdsThePackItsPresetTheTagAndTheName() {
        Map<String, String> files = DatapackFiles.of(PACK);
        assertEquals(List.of("pack.mcmeta", "data/flatgen/worldgen/world_preset/tall_flat.json",
                "data/minecraft/tags/worldgen/world_preset/normal.json", "assets/flatgen/lang/en_us.json"),
                List.copyOf(files.keySet()));
        assertEquals("{\"dimensions\":{}}", files.get("data/flatgen/worldgen/world_preset/tall_flat.json"));
        assertEquals("[\"flatgen:tall_flat\"]", json(files, "data/minecraft/tags/worldgen/world_preset/normal.json")
                .get("values").toString());
        assertEquals("Tall flat", json(files, "assets/flatgen/lang/en_us.json")
                .get("generator.flatgen.tall_flat").getAsString());
    }

    @Test
    void saysItsDataFormatAndTakesTheResourceOneToo() {
        JsonObject about = json(DatapackFiles.of(PACK), "pack.mcmeta").getAsJsonObject("pack");
        assertEquals(48, about.get("pack_format").getAsInt());
        assertEquals("[34,48]", about.get("supported_formats").toString());
        assertEquals("Tall flat", about.get("description").getAsString());
    }

    @Test
    void namesItsFormatsAsARangeForAVersionThatReadsOne() {
        DatapackFiles.Pack ranged = new DatapackFiles.Pack(new DatapackFiles.PackFormats(84, 101, 1),
                PACK.id(), PACK.name(), PACK.preset());
        JsonObject about = json(DatapackFiles.of(ranged), "pack.mcmeta").getAsJsonObject("pack");
        assertEquals(84, about.get("min_format").getAsInt());
        assertEquals("[101,1]", about.get("max_format").toString());
        assertEquals(null, about.get("pack_format"));
    }

    private static JsonObject json(Map<String, String> files, String path) {
        return JsonParser.parseString(files.get(path)).getAsJsonObject();
    }
}
