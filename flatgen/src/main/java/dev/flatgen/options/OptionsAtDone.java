package dev.flatgen.options;

import dev.flatgen.Flatgen;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.GameRuleAccess;
import dev.flatgen.mc.Structures;
import dev.flatgen.mc.WorldCreation;
import dev.flatgen.options.rules.AddStructures;
import dev.flatgen.options.rules.Effect;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.structures.rules.GenerateStructures;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Applies a preset to the Create World screen when the editor's Done is pressed: its superflat settings, what its world
 * options do there, the first-start effects expected for the world (F27), and the game's Generate Structures switch
 * for a world with no structures. An option turned off since the last Done puts its game rules back to the game's
 * defaults.
 */
public final class OptionsAtDone {
    private OptionsAtDone() {
    }

    /**
     * The superflat settings a preset makes, the structure sets its world options add included, as Done applies them
     * and as exports write them (F21).
     */
    public static FlatLevelGeneratorSettings settings(Preset preset, HolderLookup.Provider registries,
            List<WorldOption> installed) {
        OptionChoices.Applied applied = OptionChoices.applied(OptionChoices.on(preset.options(), installed));
        Preset withStructures = AddStructures.addedTo(preset, applied.structureSets(), Structures.ids(registries));
        return FlatSettings.toSettings(withStructures, registries);
    }

    /** Applies the preset, with the options it has on of those installed, to the world on the screen. */
    public static void apply(CreateWorldScreen screen, Preset preset, HolderLookup.Provider registries,
            List<WorldOption> installed) {
        List<WorldOption> on = OptionChoices.on(preset.options(), installed);
        OptionChoices.Applied turnedOff = OptionChoices.applied(OptionChoices.on(OptionSessions.applied(screen),
                installed).stream().filter(option -> !on.contains(option)).toList());
        OptionChoices.Applied applied = OptionChoices.applied(on);
        Set<String> knownSets = Structures.ids(registries);
        warnIfAny("Flatgen left out these structure sets that world options add, which this game lacks: {}",
                applied.structureSets().stream().filter(set -> !knownSets.contains(set)).sorted().toList());
        ChunkGenerator generator = WorldCreation.applyFlatSettings(screen, settings(preset, registries, installed));
        WorldCreation.applyOtherDimensions(screen, FlatSettings.otherDimensions(preset, registries));
        FirstStarts.expect(generator, WorldCreation.describe(generator, registries), on.stream()
                .flatMap(option -> option.effects().stream()).filter(Effect::actsAtFirstStart).toList());
        if (!OptionSessions.has(screen)) {
            WorldCreation.listen(screen, OptionsAtDone::follow);
        }
        setGameRules(screen, applied, turnedOff);
        GenerateStructures structures = generateStructures(screen)
                .after(AddStructures.addedTo(preset, applied.structureSets(), knownSets).structureSets());
        WorldCreation.generateStructures(screen, structures.on());
        warnIfAny("Flatgen keeps these world options in the preset, which are not installed: {}",
                OptionChoices.missing(preset.options(), installed));
        OptionSessions.remember(screen, preset.options(),
                AddStructures.newlyAdded(preset, applied.structureSets(), knownSets), structures.byFlatgen());
    }

    /**
     * The screen left a world type Flatgen edits for one it does not: the game rules Flatgen's options set go back to
     * the game's defaults, Generate Structures goes on again if Flatgen turned it off, and no world waits for
     * first-start effects, so a world of another type gets nothing of them.
     */
    public static void withdraw(CreateWorldScreen screen, List<WorldOption> installed) {
        if (!OptionSessions.has(screen)) {
            return;
        }
        OptionChoices.Applied applied = OptionChoices.applied(OptionChoices.on(OptionSessions.applied(screen),
                installed));
        warnIfAny("Flatgen could not put these game rules back: {}",
                GameRuleAccess.set(screen, GameRuleAccess.defaults(applied.gameRules().keySet())));
        WorldCreation.generateStructures(screen, generateStructures(screen).withdrawn().on());
        FirstStarts.forget();
        OptionSessions.forget(screen);
    }

    /** Sets the game rules of the options on, and puts those of options turned off back to the game's defaults. */
    private static void setGameRules(CreateWorldScreen screen, OptionChoices.Applied applied,
            OptionChoices.Applied turnedOff) {
        Map<String, String> rules = new LinkedHashMap<>(GameRuleAccess.defaults(turnedOff.gameRules().keySet()));
        rules.putAll(applied.gameRules());
        warnIfAny("Flatgen left these game rules as they were, which this game lacks or which cannot take the value"
                + " world options give them: {}", GameRuleAccess.set(screen, rules));
    }

    /** The game's Generate Structures switch on the screen as it is now, and whether Flatgen turned it off. */
    private static GenerateStructures generateStructures(CreateWorldScreen screen) {
        return new GenerateStructures(WorldCreation.generatesStructures(screen),
                OptionSessions.turnedStructuresOff(screen));
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
