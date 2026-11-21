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
import dev.flatgen.options.PresetAtDone;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;

/**
 * The default preset (F12): on a new world's Create World screen, when Superflat is chosen while its settings are still
 * the game's own default flat world, the player's default preset is applied as Done would apply it, world options
 * included; with "start on Superflat" on, such a screen opens with Superflat chosen. When a world type Flatgen edits
 * is left for one it does not, the game rules Flatgen's options set go back to their defaults. A world being
 * re-created is left as it is. It only listens to the screen, through the public methods of its state, and never
 * changes the screen itself.
 */
public final class DefaultPresets {
    /**
     * Each Create World screen listened to, with its world type as it was when it last changed, so the default is
     * applied when Superflat is chosen, not again when applying it changes the screen, and options are taken back when
     * a type Flatgen edits is left; a screen closed is forgotten.
     */
    private static final Map<CreateWorldScreen, Chosen> CHOSEN = new WeakHashMap<>();

    /**
     * What a screen's world type is, as the default and the options need it.
     *
     * @param superflat whether it is Superflat, which the default is for
     * @param edited    whether Flatgen's editor can have set its settings, Superflat or a type superflat underneath
     */
    private record Chosen(boolean superflat, boolean edited) {
        static final Chosen NOTHING = new Chosen(false, false);
    }

    private DefaultPresets() {
    }

    /** A screen has opened, told by the loader's platform half: a Create World screen is listened to, once. */
    public static void screenOpened(Screen screen) {
        if (!(screen instanceof CreateWorldScreen createWorld) || CHOSEN.containsKey(createWorld)) {
            return;
        }
        boolean fresh = WorldCreation.isFresh(createWorld);
        CHOSEN.put(createWorld, fresh ? Chosen.NOTHING : chosen(createWorld));
        WorldCreation.listen(createWorld, context -> ClientTasks.later(() -> worldChanged(createWorld)));
        if (fresh && ChoicesFile.read().startOnSuperflat()) {
            PresetEditors.EDITED.stream().findFirst().ifPresent(type -> WorldCreation.chooseWorldType(createWorld,
                    type));
        }
        ClientTasks.later(() -> worldChanged(createWorld));
    }

    /**
     * The world on the screen changed: applies the default when Superflat has just been chosen, and takes Flatgen's
     * world options back when a type Flatgen edits has just been left. It runs after the screen's listeners have been
     * told, as either changes the screen and may add a listener.
     */
    private static void worldChanged(CreateWorldScreen screen) {
        Chosen now = chosen(screen);
        Chosen was = Optional.ofNullable(CHOSEN.put(screen, now)).orElse(Chosen.NOTHING);
        if (now.superflat() && !was.superflat()) {
            chosenSuperflat(screen);
        }
        if (!now.edited() && was.edited()) {
            PresetAtDone.withdraw(screen, InstalledOptions.load());
        }
    }

    /** What the screen's world type is now. */
    private static Chosen chosen(CreateWorldScreen screen) {
        RegistryAccess registries = WorldCreation.context(screen).worldgenLoadContext();
        boolean edited = WorldCreation.worldType(screen)
                .filter(type -> PresetEditors.mayHaveEdited(type, registries)).isPresent();
        return new Chosen(isSuperflat(screen), edited);
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
        PresetAtDone.apply(screen, MissingParts.replaced(preset, missing), registries, InstalledOptions.load());
    }
}
