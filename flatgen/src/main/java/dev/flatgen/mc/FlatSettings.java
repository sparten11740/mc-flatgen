package dev.flatgen.mc;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetReading;
import dev.flatgen.preset.rules.VanillaSettings;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Presets as the game's superflat settings and back, through the settings' own codec and the JSON of
 * {@link VanillaSettings}, so ids resolve against the registries of the world they are for, modded ones included.
 */
public final class FlatSettings {
    private FlatSettings() {
    }

    /** The preset of these settings, under this name and icon. */
    public static PresetReading toPreset(FlatLevelGeneratorSettings settings, HolderLookup.Provider registries,
            String name, String icon) {
        JsonElement json = FlatLevelGeneratorSettings.CODEC.encodeStart(ops(registries), settings).getOrThrow();
        return VanillaSettings.fromJson(json.getAsJsonObject(), name, icon);
    }

    /**
     * The settings of a preset whose ids all resolve ({@link #missingIds} is empty).
     *
     * @throws IllegalStateException when the game refuses the settings, as it does an unknown block
     */
    public static FlatLevelGeneratorSettings toSettings(Preset preset, HolderLookup.Provider registries) {
        return FlatLevelGeneratorSettings.CODEC.parse(ops(registries), VanillaSettings.toJson(preset)).getOrThrow();
    }

    /**
     * The ids of a preset the registries lack. The game would refuse an unknown block, but quietly put plains in place
     * of an unknown biome and every structure set in place of an unknown one, so these are found first.
     */
    public static MissingIds missingIds(Preset preset, HolderLookup.Provider registries) {
        return new MissingIds(
                missing(preset.layers().stream().map(Layer::block), id -> has(registries, Registries.BLOCK, id)),
                missing(Stream.of(preset.biome()), id -> has(registries, Registries.BIOME, id)),
                missing(preset.structureSets().orElse(List.of()).stream(),
                        id -> hasSetOrTag(registries, Registries.STRUCTURE_SET, id)));
    }

    /** The game's ops for JSON that names registry entries of these registries. */
    public static RegistryOps<JsonElement> ops(HolderLookup.Provider registries) {
        return RegistryOps.create(JsonOps.INSTANCE, registries);
    }

    private static List<String> missing(Stream<String> ids, Predicate<String> isKnown) {
        return ids.distinct().filter(isKnown.negate()).collect(Collectors.toList());
    }

    private static <T> boolean has(HolderLookup.Provider registries, ResourceKey<? extends Registry<T>> registry,
            String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null
                && registries.lookupOrThrow(registry).get(ResourceKey.create(registry, location)).isPresent();
    }

    private static <T> boolean hasSetOrTag(HolderLookup.Provider registries,
            ResourceKey<? extends Registry<T>> registry, String id) {
        if (!id.startsWith(VanillaSettings.TAG_MARK)) {
            return has(registries, registry, id);
        }
        ResourceLocation location = ResourceLocation.tryParse(id.substring(VanillaSettings.TAG_MARK.length()));
        return location != null
                && registries.lookupOrThrow(registry).get(TagKey.create(registry, location)).isPresent();
    }
}
