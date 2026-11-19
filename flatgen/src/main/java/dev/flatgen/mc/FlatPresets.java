package dev.flatgen.mc;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.preset.rules.PresetReading;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

/** The flat world presets of the game and its data packs (F13), as this version lists them, those it shows players. */
public final class FlatPresets {
    /** How a game preset's key begins in the library, before its id. */
    public static final String KEY_PREFIX = "game:";

    private FlatPresets() {
    }

    /**
     * Every flat world preset of these registries the game's own presets screen shows, with its name in the game's
     * language and its item as icon, converted as Flatgen reads the game's settings.
     */
    public static List<PresetLibrary.Entry> visible(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET)
                .get(FlatLevelGeneratorPresetTags.VISIBLE).stream()
                .flatMap(HolderSet.Named::stream)
                .flatMap(preset -> preset.unwrapKey().map(key -> entry(key, preset.value(), registries)).stream())
                .sorted(Comparator.comparing(PresetLibrary.Entry::key))
                .toList();
    }

    private static PresetLibrary.Entry entry(ResourceKey<FlatLevelGeneratorPreset> key,
            FlatLevelGeneratorPreset preset, HolderLookup.Provider registries) {
        String name = Component.translatable(key.location().toLanguageKey("flat_world_preset")).getString();
        String icon = BuiltInRegistries.ITEM.getKey(preset.displayItem().value()).toString();
        PresetReading reading = FlatSettings.toPreset(preset.settings(), registries, name, icon);
        return new PresetLibrary.Entry(KEY_PREFIX + key.location(), PresetLibrary.Origin.GAME,
                Optional.of(reading.preset()), reading.problems());
    }
}
