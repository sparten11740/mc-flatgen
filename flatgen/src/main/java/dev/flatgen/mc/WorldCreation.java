package dev.flatgen.mc;

import com.google.gson.JsonElement;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

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

    /**
     * Gives the world these superflat settings, as the game's own editor does when its Done is pressed.
     *
     * @return the overworld's generator put on the screen, the very object the world will be created with
     */
    public static ChunkGenerator applyFlatSettings(CreateWorldScreen screen, FlatLevelGeneratorSettings settings) {
        FlatLevelSource generator = new FlatLevelSource(settings);
        screen.getUiState().updateDimensions((registries, dimensions) ->
                dimensions.replaceOverworldGenerator(registries, generator));
        return generator;
    }

    /** The world being created, but with these superflat settings; the Create World screen stays as it is. */
    public static WorldCreationContext withFlatSettings(WorldCreationContext context,
            FlatLevelGeneratorSettings settings) {
        return context.withDimensions(withFlatOverworld(settings));
    }

    private static WorldCreationContext.DimensionsUpdater withFlatOverworld(FlatLevelGeneratorSettings settings) {
        return (registries, dimensions) -> dimensions.replaceOverworldGenerator(registries,
                new FlatLevelSource(settings));
    }

    /** The world's overworld generator, as it is on the screen now. */
    public static ChunkGenerator overworldGenerator(WorldCreationContext context) {
        return context.selectedDimensions().overworld();
    }

    /**
     * A generator's settings as JSON text, naming registry entries by id, to tell whether two generators of different
     * registries generate alike; empty when it cannot be written.
     */
    public static String describe(ChunkGenerator generator, HolderLookup.Provider registries) {
        return ChunkGenerator.CODEC.encodeStart(FlatSettings.ops(registries), generator).result()
                .map(JsonElement::toString).orElse("");
    }

    /** Tells the listener each change of the world on the screen, as the screen's own tabs are told. */
    public static void listen(CreateWorldScreen screen, Consumer<WorldCreationContext> listener) {
        screen.getUiState().addListener(state -> listener.accept(state.getSettings()));
    }

    /** The world's overworld type, which says where its floor is and how tall it is. */
    public static DimensionType overworldType(WorldCreationContext context) {
        return context.selectedDimensions().dimensions().get(LevelStem.OVERWORLD).type().value();
    }

    /**
     * Opens the Create World screen as Singleplayer's Create New World does, with this world type chosen.
     *
     * @throws IllegalStateException when the screen does not open or has no such world type
     */
    public static CreateWorldScreen openWithWorldType(Minecraft minecraft, Screen lastScreen,
            ResourceKey<WorldPreset> worldType) {
        CreateWorldScreen.openFresh(minecraft, lastScreen);
        if (!(minecraft.screen instanceof CreateWorldScreen screen)) {
            throw new IllegalStateException("the Create World screen did not open");
        }
        WorldCreationUiState state = screen.getUiState();
        WorldCreationUiState.WorldTypeEntry entry = state.getNormalPresetList().stream()
                .filter(type -> type.preset() != null && type.preset().is(worldType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Create World offers no " + worldType.location()));
        state.setWorldType(entry);
        return screen;
    }
}
