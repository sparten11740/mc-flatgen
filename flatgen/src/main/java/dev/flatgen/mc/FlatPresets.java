package dev.flatgen.mc;

import dev.flatgen.preset.rules.PresetReading;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

/** The flat world presets of the game and its data packs, as this version lists them, those it shows players. */
public final class FlatPresets {
    private FlatPresets() {
    }

    /**
     * Every flat world preset of these registries the game's own presets screen shows, by id in id order, with its name
     * in the game's language and its item as icon, converted as Flatgen reads the game's settings.
     */
    public static Map<String, PresetReading> visible(HolderLookup.Provider registries) {
        Map<String, PresetReading> presets = new LinkedHashMap<>();
        registries.lookupOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).get(FlatLevelGeneratorPresetTags.VISIBLE)
                .stream().flatMap(HolderSet.Named::stream)
                .flatMap(preset -> preset.unwrapKey().stream().map(key -> Map.entry(key, preset.value())))
                .sorted(Map.Entry.comparingByKey((first, second) -> first.location().compareTo(second.location())))
                .forEach(preset -> presets.put(preset.getKey().location().toString(),
                        reading(preset.getKey(), preset.getValue(), registries)));
        return presets;
    }

    private static PresetReading reading(ResourceKey<FlatLevelGeneratorPreset> key, FlatLevelGeneratorPreset preset,
            HolderLookup.Provider registries) {
        String name = Component.translatable(key.location().toLanguageKey("flat_world_preset")).getString();
        String icon = BuiltInRegistries.ITEM.getKey(preset.displayItem().value()).toString();
        return FlatSettings.toPreset(preset.settings(), registries, name, icon);
    }
}
