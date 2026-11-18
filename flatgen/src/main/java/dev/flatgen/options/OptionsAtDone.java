package dev.flatgen.options;

import dev.flatgen.Flatgen;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.GameRuleAccess;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.options.rules.AddStructures;
import dev.flatgen.options.rules.Effect;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Preset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Applies a preset to the Create World screen when the editor's Done is pressed: its superflat settings, what its world
 * options do there, and the first-start effects expected for the world (F27). An option turned off since the last Done
 * puts its game rules back to the game's defaults.
 */
public final class OptionsAtDone {
    private OptionsAtDone() {
    }

    /** Applies the preset, with the options it has on, to the world on the screen. */
    public static void apply(CreateWorldScreen screen, Preset preset, HolderLookup.Provider registries) {
        List<WorldOption> installed = InstalledOptions.load();
        List<WorldOption> on = OptionChoices.on(preset.options(), installed);
        OptionChoices.Applied turnedOff = OptionChoices.applied(OptionChoices.on(OptionSessions.applied(screen),
                installed).stream().filter(option -> !on.contains(option)).toList());
        OptionChoices.Applied applied = OptionChoices.applied(on);
        ChunkGenerator generator = WorldCreation.applyFlatSettings(screen,
                FlatSettings.toSettings(AddStructures.addedTo(preset, applied.structureSets()), registries));
        FirstStarts.expect(generator, WorldCreation.describe(generator, registries), on.stream()
                .flatMap(option -> option.effects().stream()).filter(Effect::actsAtFirstStart).toList());
        if (!OptionSessions.has(screen)) {
            WorldCreation.listen(screen, OptionsAtDone::follow);
        }
        setGameRules(screen, applied, turnedOff);
        warnIfAny("Flatgen keeps these world options in the preset, which are not installed: {}",
                OptionChoices.missing(preset.options(), installed));
        OptionSessions.remember(screen, preset.options());
    }

    /** Sets the game rules of the options on, and puts those of options turned off back to the game's defaults. */
    private static void setGameRules(CreateWorldScreen screen, OptionChoices.Applied applied,
            OptionChoices.Applied turnedOff) {
        Map<String, String> rules = new LinkedHashMap<>(GameRuleAccess.defaults(turnedOff.gameRules().keySet()));
        rules.putAll(applied.gameRules());
        warnIfAny("Flatgen left these game rules as they were, which this game lacks or which cannot take the value"
                + " world options give them: {}", GameRuleAccess.set(screen, rules));
    }

    private static void warnIfAny(String message, List<String> names) {
        if (!names.isEmpty()) {
            Flatgen.LOGGER.warn(message, String.join(", ", names));
        }
    }

    /** Follows the world expected when the screen rebuilds its overworld generator alike, as data pack changes do. */
    private static void follow(WorldCreationContext context) {
        ChunkGenerator generator = WorldCreation.overworldGenerator(context);
        if (FirstStarts.expectsAnother(generator)) {
            FirstStarts.follow(generator, WorldCreation.describe(generator, context.worldgenLoadContext()));
        }
    }
}
