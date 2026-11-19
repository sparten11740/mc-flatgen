package dev.flatgen.mc;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.flatgen.preset.rules.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** The world types the Create World screen offers, as Flatgen tells which of them it can edit. */
public final class WorldTypes {
    private WorldTypes() {
    }

    /**
     * Whether a world type is superflat underneath and nothing more: its overworld is flat and its other dimensions are
     * the Default world type's, so its overworld's layers are all there is to edit, as a mod's "airship ready" type,
     * and unlike one that changes the End as well.
     */
    public static boolean onlyFlatOverworld(Holder<WorldPreset> type, HolderLookup.Provider registries) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = type.value().createWorldDimensions().dimensions();
        boolean flat = Optional.ofNullable(dimensions.get(LevelStem.OVERWORLD))
                .filter(overworld -> overworld.generator() instanceof FlatLevelSource).isPresent();
        Optional<Map<ResourceKey<LevelStem>, LevelStem>> normal = defaultDimensions(registries);
        return flat && normal.isPresent() && others(dimensions, registries).equals(others(normal.get(), registries));
    }

    /** The dimensions of the Default world type, by their keys; none when the registries lack it. */
    public static Optional<Map<ResourceKey<LevelStem>, LevelStem>> defaultDimensions(
            HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.WORLD_PRESET).get(WorldPresets.NORMAL)
                .map(preset -> preset.value().createWorldDimensions().dimensions());
    }

    /** Every dimension but the overworld, as the game writes it, so two world types' can be compared. */
    private static Map<String, Optional<JsonElement>> others(Map<ResourceKey<LevelStem>, LevelStem> dimensions,
            HolderLookup.Provider registries) {
        DynamicOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        Map<String, Optional<JsonElement>> written = new TreeMap<>();
        dimensions.forEach((key, stem) -> written.put(key.location().toString(),
                LevelStem.CODEC.encodeStart(ops, stem).result()));
        written.remove(LevelStem.OVERWORLD.location().toString());
        return written;
    }

    /**
     * Dimensions with the Nether's and the End's generators as the settings say: superflat ones, or for a dimension
     * with none, its generator among {@code own}.
     */
    public static Map<ResourceKey<LevelStem>, LevelStem> withOtherDimensions(
            Map<ResourceKey<LevelStem>, LevelStem> dimensions, Map<ResourceKey<LevelStem>, LevelStem> own,
            Map<Dimension, Optional<FlatLevelGeneratorSettings>> settings) {
        Map<ResourceKey<LevelStem>, LevelStem> changed = new LinkedHashMap<>(dimensions);
        settings.forEach((dimension, flat) -> changed.computeIfPresent(stemKey(dimension),
                (key, stem) -> flat.map(chosen -> new LevelStem(stem.type(), new FlatLevelSource(chosen)))
                        .orElse(own.getOrDefault(key, stem))));
        return changed;
    }

    /** The key of a dimension's stem among a world's dimensions. */
    public static ResourceKey<LevelStem> stemKey(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case END -> LevelStem.END;
        };
    }
}
