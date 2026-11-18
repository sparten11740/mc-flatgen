package dev.flatgen.mc;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** The world on the Create World screen, as this version reads and changes it. */
public final class WorldCreation {
    private WorldCreation() {
    }

    /** The world's superflat settings, or the game's default ones when its overworld is not superflat. */
    public static FlatLevelGeneratorSettings flatSettings(WorldCreationContext context) {
        if (context.selectedDimensions().overworld() instanceof FlatLevelSource flat) {
            return flat.settings();
        }
        RegistryAccess registries = context.worldgenLoadContext();
        return FlatLevelGeneratorSettings.getDefault(
                registries.lookupOrThrow(Registries.BIOME),
                registries.lookupOrThrow(Registries.STRUCTURE_SET),
                registries.lookupOrThrow(Registries.PLACED_FEATURE));
    }

    /** Gives the world these superflat settings, as the game's own editor does when its Done is pressed. */
    public static void applyFlatSettings(CreateWorldScreen screen, FlatLevelGeneratorSettings settings) {
        screen.getUiState().updateDimensions((registries, dimensions) ->
                dimensions.replaceOverworldGenerator(registries, new FlatLevelSource(settings)));
    }

    /** The world's overworld type, which says where its floor is and how tall it is. */
    public static DimensionType overworldType(WorldCreationContext context) {
        return context.selectedDimensions().dimensions().get(LevelStem.OVERWORLD).type().value();
    }
}
