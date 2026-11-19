package dev.flatgen.mc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.flatgen.exports.rules.DatapackFiles;
import dev.flatgen.json.rules.JsonFields;
import dev.flatgen.preset.rules.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
//? if >=1.21.11 {
/*import net.minecraft.server.packs.metadata.pack.PackFormat;
*///?}
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * What exports (F21) write in this version's own formats: superflat settings as a server's generator settings, a world
 * preset of them, and the pack formats a datapack of this version says it is.
 */
public final class Exports {
    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();

    private Exports() {
    }

    /** This version's resource and data pack formats, as the game itself gives them. */
    public static DatapackFiles.PackFormats packFormats() {
        WorldVersion version = SharedConstants.getCurrentVersion();
        //? if >=1.21.11 {
        /*PackFormat data = version.packVersion(PackType.SERVER_DATA);
        return new DatapackFiles.PackFormats(version.packVersion(PackType.CLIENT_RESOURCES).major(), data.major(),
                data.minor());
        *///?} else {
        return new DatapackFiles.PackFormats(version.getPackVersion(PackType.CLIENT_RESOURCES),
                version.getPackVersion(PackType.SERVER_DATA), 0);
        //?}
    }

    /** Superflat settings as one line of JSON, as a server's {@code generator-settings} reads them. */
    public static String generatorSettings(FlatLevelGeneratorSettings settings, HolderLookup.Provider registries) {
        return COMPACT.toJson(FlatLevelGeneratorSettings.CODEC.encodeStart(FlatSettings.ops(registries), settings)
                .getOrThrow());
    }

    /**
     * A world preset of these superflat settings: the Default world type's dimensions, its overworld superflat and its
     * Nether and End too where {@code others} gives them settings, as a datapack's {@code worldgen/world_preset} file
     * holds it.
     *
     * @throws IllegalStateException when the registries lack the Default world type
     */
    public static String worldPreset(FlatLevelGeneratorSettings settings,
            Map<Dimension, Optional<FlatLevelGeneratorSettings>> others, HolderLookup.Provider registries) {
        Map<ResourceKey<LevelStem>, LevelStem> normal = WorldTypes.defaultDimensions(registries)
                .orElseThrow(() -> new IllegalStateException("the game has no Default world type"));
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = new LinkedHashMap<>(
                WorldTypes.withOtherDimensions(normal, normal, others));
        LevelStem overworld = dimensions.get(LevelStem.OVERWORLD);
        dimensions.put(LevelStem.OVERWORLD, new LevelStem(overworld.type(), new FlatLevelSource(settings)));
        return JsonFields.PRETTY.toJson(WorldPreset.DIRECT_CODEC.encodeStart(FlatSettings.ops(registries),
                new WorldPreset(dimensions)).getOrThrow());
    }
}
