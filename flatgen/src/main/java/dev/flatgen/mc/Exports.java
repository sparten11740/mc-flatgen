package dev.flatgen.mc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.flatgen.exports.rules.DatapackFiles;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * What exports (F21) write in this version's own formats: superflat settings as a server's generator settings, a world
 * preset of them, and the pack formats a datapack of this version says it is.
 */
public final class Exports {
    /** This version's resource and data pack formats, 1.21.1's. */
    public static final DatapackFiles.PackFormats PACK_FORMATS = new DatapackFiles.PackFormats(34, 48);
    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Exports() {
    }

    /** Superflat settings as one line of JSON, as a server's {@code generator-settings} reads them. */
    public static String generatorSettings(FlatLevelGeneratorSettings settings, HolderLookup.Provider registries) {
        return COMPACT.toJson(FlatLevelGeneratorSettings.CODEC.encodeStart(FlatSettings.ops(registries), settings)
                .getOrThrow());
    }

    /**
     * A world preset of these superflat settings: the Default world type's dimensions, its overworld superflat, as a
     * datapack's {@code worldgen/world_preset} file holds it.
     *
     * @throws IllegalStateException when the registries lack the Default world type
     */
    public static String worldPreset(FlatLevelGeneratorSettings settings, HolderLookup.Provider registries) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = new LinkedHashMap<>(WorldTypes.defaultDimensions(registries)
                .orElseThrow(() -> new IllegalStateException("the game has no Default world type")));
        LevelStem overworld = dimensions.get(LevelStem.OVERWORLD);
        dimensions.put(LevelStem.OVERWORLD, new LevelStem(overworld.type(), new FlatLevelSource(settings)));
        return PRETTY.toJson(WorldPreset.DIRECT_CODEC.encodeStart(FlatSettings.ops(registries),
                new WorldPreset(dimensions)).getOrThrow());
    }
}
