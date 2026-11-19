package dev.flatgen.mc;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
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
        Optional<Map<ResourceKey<LevelStem>, LevelStem>> normal = registries.lookupOrThrow(Registries.WORLD_PRESET)
                .get(WorldPresets.NORMAL).map(preset -> preset.value().createWorldDimensions().dimensions());
        return flat && normal.isPresent() && others(dimensions, registries).equals(others(normal.get(), registries));
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
}
