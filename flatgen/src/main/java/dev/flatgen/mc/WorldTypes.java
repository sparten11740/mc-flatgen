package dev.flatgen.mc;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.flatgen.preset.rules.Dimension;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
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
     * Whether Flatgen can edit a world type whole: its overworld is flat, and each other dimension is the Default world
     * type's or, for the Nether and the End, which Flatgen edits too (F22), flat. So a mod's "airship ready" type is,
     * as is the game's Flat All Dimensions from 26.2 on, and a type that makes the End some other way is not.
     */
    public static boolean flatUnderneath(Holder<WorldPreset> type, HolderLookup.Provider registries) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = type.value().createWorldDimensions().dimensions();
        Set<Dimension> flat = flatDimensions(type);
        return flat.contains(Dimension.OVERWORLD) && defaultDimensions(registries)
                .filter(normal -> written(normal, flat, registries).equals(written(dimensions, flat, registries)))
                .isPresent();
    }

    /** The dimensions Flatgen edits that a world type makes flat. */
    public static Set<Dimension> flatDimensions(Holder<WorldPreset> type) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = type.value().createWorldDimensions().dimensions();
        return Arrays.stream(Dimension.values()).filter(dimension -> flatSettings(dimensions, dimension).isPresent())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Dimension.class)));
    }

    /** A dimension's superflat settings among a world's dimensions, when it has that dimension flat. */
    public static Optional<FlatLevelGeneratorSettings> flatSettings(Map<ResourceKey<LevelStem>, LevelStem> dimensions,
            Dimension dimension) {
        LevelStem stem = dimensions.get(stemKey(dimension));
        return stem != null && stem.generator() instanceof FlatLevelSource flat ? Optional.of(flat.settings())
                : Optional.empty();
    }

    /** The dimensions of the Default world type, by their keys; none when the registries lack it. */
    public static Optional<Map<ResourceKey<LevelStem>, LevelStem>> defaultDimensions(
            HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.WORLD_PRESET).get(WorldPresets.NORMAL)
                .map(preset -> preset.value().createWorldDimensions().dimensions());
    }

    /** The dimensions but those left out, as the game writes them, so two world types' can be compared. */
    private static Map<String, Optional<JsonElement>> written(Map<ResourceKey<LevelStem>, LevelStem> dimensions,
            Set<Dimension> leftOut, HolderLookup.Provider registries) {
        DynamicOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        Map<String, Optional<JsonElement>> written = new TreeMap<>();
        dimensions.forEach((key, stem) -> written.put(key.location().toString(),
                LevelStem.CODEC.encodeStart(ops, stem).result()));
        leftOut.forEach(dimension -> written.remove(stemKey(dimension).location().toString()));
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
