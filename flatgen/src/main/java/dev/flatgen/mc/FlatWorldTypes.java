package dev.flatgen.mc;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetReading;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * The world types of the game and its data packs that make more than the overworld flat, the game's Flat All
 * Dimensions from 26.2 on, read as presets with a stack for each flat dimension.
 */
public final class FlatWorldTypes {
    private FlatWorldTypes() {
    }

    /**
     * Every world type of these registries Flatgen can edit whole that makes the Nether or the End flat as well, by id
     * in id order, with its name in the game's language and its top block as icon, converted as Flatgen reads the
     * game's settings.
     */
    public static Map<String, PresetReading> presets(HolderLookup.Provider registries) {
        Map<String, PresetReading> presets = new LinkedHashMap<>();
        registries.lookupOrThrow(Registries.WORLD_PRESET).listElements()
                .filter(type -> flatBeyondOverworld(type) && WorldTypes.editableWhole(type, registries))
                .sorted(Comparator.comparing((Holder.Reference<WorldPreset> type) -> type.key().location()))
                .forEach(type -> presets.put(type.key().location().toString(), reading(type, registries)));
        return presets;
    }

    private static boolean flatBeyondOverworld(Holder<WorldPreset> type) {
        return WorldTypes.flatDimensions(type).stream().anyMatch(Dimension.OTHERS::contains);
    }

    private static PresetReading reading(Holder.Reference<WorldPreset> type, HolderLookup.Provider registries) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = type.value().createWorldDimensions().dimensions();
        String name = Component.translatable(type.key().location().toLanguageKey("generator")).getString();
        PresetReading overworld = FlatSettings.toPreset(WorldTypes.flatSettings(dimensions, Dimension.OVERWORLD)
                .orElseThrow(), registries, name, "");
        Preset preset = overworld.preset().withTopBlockIcon().withOtherStacks(dimension -> WorldTypes
                .flatSettings(dimensions, dimension).map(settings -> FlatSettings.toStack(settings, registries)));
        return new PresetReading(preset, overworld.problems());
    }
}
