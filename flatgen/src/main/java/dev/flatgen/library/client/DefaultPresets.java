package dev.flatgen.library.client;

import dev.flatgen.Flatgen;
import dev.flatgen.editor.client.PresetEditors;
import dev.flatgen.library.ChoicesFile;
import dev.flatgen.library.PresetStore;
import dev.flatgen.library.rules.Choices;
import dev.flatgen.library.rules.MissingParts;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.options.InstalledOptions;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;

/**
 * The default preset (F12): when Superflat is chosen on a Create World screen and its settings are still the game's own
 * default flat world, the player's default preset is applied as Done would apply it, world options included; with
 * "start on Superflat" on, the screen opens with Superflat chosen. It only listens to the screen, through the public
 * methods of its state, and never changes the screen itself.
 */
public final class DefaultPresets {
    /** The Create World screens already listened to, so each is listened to once; a screen closed is forgotten. */
    private static final Set<CreateWorldScreen> WATCHED = Collections.newSetFromMap(new WeakHashMap<>());
    /** Whether the default is being applied now, as applying it changes the screen it listens to. */
    private static boolean applying;

    private DefaultPresets() {
    }

    /** A screen has opened, told by the loader's platform half: a Create World screen is listened to, once. */
    public static void screenOpened(Screen screen) {
        if (!(screen instanceof CreateWorldScreen createWorld) || !WATCHED.add(createWorld)) {
            return;
        }
        WorldCreation.listen(createWorld, context -> worldChanged(createWorld));
        if (ChoicesFile.read().startOnSuperflat()) {
            PresetEditors.EDITED.stream().findFirst().ifPresent(type -> WorldCreation.chooseWorldType(createWorld,
                    type));
        }
        worldChanged(createWorld);
    }

    /** The world on the screen changed: applies the default when Superflat is chosen with the game's own settings. */
    private static void worldChanged(CreateWorldScreen screen) {
        boolean superflat = PresetEditors.EDITED.stream().anyMatch(type -> WorldCreation.hasWorldType(screen, type));
        if (applying || !superflat) {
            return;
        }
        Optional<Preset> chosen = ChoicesFile.read().defaultFile().flatMap(PresetStore.player()::read);
        RegistryAccess registries = screen.getUiState().getSettings().worldgenLoadContext();
        Preset world = FlatSettings.toPreset(WorldCreation.flatSettings(screen.getUiState().getSettings()),
                registries, "", "").preset();
        Preset gameDefault = FlatSettings.toPreset(WorldCreation.defaultFlatSettings(registries), registries, "", "")
                .preset();
        if (chosen.isPresent() && Choices.mayApplyDefault(world, gameDefault)) {
            apply(screen, chosen.get(), registries);
        }
    }

    private static void apply(CreateWorldScreen screen, Preset preset, RegistryAccess registries) {
        MissingIds missing = FlatSettings.missingIds(preset, registries);
        if (!missing.isEmpty()) {
            Flatgen.LOGGER.warn("Flatgen's default preset names what this game lacks, used without it: {}", missing);
        }
        applying = true;
        try {
            OptionsAtDone.apply(screen, MissingParts.replaced(preset, missing), registries, InstalledOptions.load());
        } finally {
            applying = false;
        }
    }
}
