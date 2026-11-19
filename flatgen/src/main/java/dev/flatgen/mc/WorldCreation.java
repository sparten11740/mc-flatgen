package dev.flatgen.mc;

import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
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
        return defaultFlatSettings(context.worldgenLoadContext());
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
        if (!chooseWorldType(screen, worldType)) {
            throw new IllegalStateException("Create World offers no " + worldType.location());
        }
        return screen;
    }

    /** Chooses a world type on the Create World screen, as its World Type button does; whether it offers it. */
    public static boolean chooseWorldType(CreateWorldScreen screen, ResourceKey<WorldPreset> worldType) {
        WorldCreationUiState state = screen.getUiState();
        Optional<WorldCreationUiState.WorldTypeEntry> entry = state.getNormalPresetList().stream()
                .filter(type -> type.preset() != null && type.preset().is(worldType))
                .findFirst();
        entry.ifPresent(state::setWorldType);
        return entry.isPresent();
    }

    /** The world on the Create World screen as it is now. */
    public static WorldCreationContext context(CreateWorldScreen screen) {
        return screen.getUiState().getSettings();
    }

    /**
     * Whether the Create World screen is for a new world, as Singleplayer's Create New World opens it, rather than for
     * a world being re-created, which comes with its seed.
     */
    public static boolean isFresh(CreateWorldScreen screen) {
        return screen.getUiState().getSeed().isEmpty();
    }

    /** Whether the Create World screen has this world type chosen. */
    public static boolean hasWorldType(CreateWorldScreen screen, ResourceKey<WorldPreset> worldType) {
        Holder<WorldPreset> chosen = screen.getUiState().getWorldType().preset();
        return chosen != null && chosen.is(worldType);
    }

    /** The game's own default superflat settings, which a new Superflat world starts with. */
    public static FlatLevelGeneratorSettings defaultFlatSettings(RegistryAccess registries) {
        return FlatLevelGeneratorSettings.getDefault(
                registries.lookupOrThrow(Registries.BIOME),
                registries.lookupOrThrow(Registries.STRUCTURE_SET),
                registries.lookupOrThrow(Registries.PLACED_FEATURE));
    }
}
