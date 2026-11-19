package dev.flatgen.library.client;

import dev.flatgen.Flatgen;
import dev.flatgen.editor.client.PresetEditors;
import dev.flatgen.library.ChoicesFile;
import dev.flatgen.library.LibrarySources;
import dev.flatgen.library.rules.Choices;
import dev.flatgen.library.rules.MissingParts;
import dev.flatgen.mc.ClientTasks;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.options.InstalledOptions;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;

/**
 * The default preset (F12): on a new world's Create World screen, when Superflat is chosen while its settings are still
 * the game's own default flat world, the player's default preset is applied as Done would apply it, world options
 * included; with "start on Superflat" on, such a screen opens with Superflat chosen. A world being re-created is left
 * as it is. It only listens to the screen, through the public methods of its state, and never changes the screen.
 */
public final class DefaultPresets {
    /**
     * Each Create World screen listened to, with whether Superflat was chosen when it last changed, so the default is
     * applied when Superflat is chosen, not again when applying it changes the screen; a screen closed is forgotten.
     */
    private static final Map<CreateWorldScreen, Boolean> SUPERFLAT = new WeakHashMap<>();

    private DefaultPresets() {
    }

    /** A screen has opened, told by the loader's platform half: a Create World screen is listened to, once. */
    public static void screenOpened(Screen screen) {
        if (!(screen instanceof CreateWorldScreen createWorld) || SUPERFLAT.containsKey(createWorld)) {
            return;
        }
        boolean fresh = WorldCreation.isFresh(createWorld);
        SUPERFLAT.put(createWorld, !fresh && isSuperflat(createWorld));
        WorldCreation.listen(createWorld, context -> ClientTasks.later(() -> worldChanged(createWorld)));
        if (fresh && ChoicesFile.read().startOnSuperflat()) {
            PresetEditors.EDITED.stream().findFirst().ifPresent(type -> WorldCreation.chooseWorldType(createWorld,
                    type));
        }
        ClientTasks.later(() -> worldChanged(createWorld));
    }

    /**
     * The world on the screen changed: applies the default when Superflat has just been chosen. It runs after the
     * screen's listeners have been told, as applying the default changes the screen and may add one.
     */
    private static void worldChanged(CreateWorldScreen screen) {
        boolean superflat = isSuperflat(screen);
        boolean was = Boolean.TRUE.equals(SUPERFLAT.put(screen, superflat));
        if (superflat && !was) {
            chosenSuperflat(screen);
        }
    }

    /** Superflat was just chosen: applies the default while the settings are still the game's default flat world. */
    private static void chosenSuperflat(CreateWorldScreen screen) {
        RegistryAccess registries = WorldCreation.context(screen).worldgenLoadContext();
        Preset world = FlatSettings.toPreset(WorldCreation.flatSettings(WorldCreation.context(screen)), registries,
                "", "").preset();
        Preset gameDefault = FlatSettings.toPreset(WorldCreation.defaultFlatSettings(registries), registries, "", "")
                .preset();
        if (Choices.mayApplyDefault(world, gameDefault)) {
            ChoicesFile.read().defaultFile().flatMap(key -> LibrarySources.find(key, registries))
                    .ifPresent(preset -> apply(screen, preset, registries));
        }
    }

    private static boolean isSuperflat(CreateWorldScreen screen) {
        return PresetEditors.EDITED.stream().anyMatch(type -> WorldCreation.hasWorldType(screen, type));
    }

    private static void apply(CreateWorldScreen screen, Preset preset, RegistryAccess registries) {
        MissingIds missing = FlatSettings.missingIds(preset, registries);
        if (!missing.isEmpty()) {
            Flatgen.LOGGER.warn("Flatgen's default preset names what this game lacks, used without it: {}", missing);
        }
        OptionsAtDone.apply(screen, MissingParts.replaced(preset, missing), registries, InstalledOptions.load());
    }
}
