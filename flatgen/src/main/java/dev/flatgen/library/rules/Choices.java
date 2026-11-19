package dev.flatgen.library.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.search.rules.Picks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The player's choices among their presets (F12), kept in config/flatgen/library.json by preset file name, so preset
 * files stay free of them and can be shared as they are: the favourites and the presets used lately, as the pickers
 * keep theirs, the default, applied when Superflat is chosen, and whether Create World starts on Superflat.
 *
 * @param picks            the favourites (starred) and the presets used lately, by file
 * @param defaultFile      the default preset's file; none when there is no default
 * @param startOnSuperflat whether Create World opens with Superflat chosen, so the default is ready
 * @param builtIns         which built-in presets the library shows (F13)
 */
public record Choices(Picks picks, Optional<String> defaultFile, boolean startOnSuperflat,
        PresetLibrary.BuiltIns builtIns) {
    /** The choices of a player who has made none. */
    public static final Choices NONE = new Choices(Picks.EMPTY, Optional.empty(), false,
            PresetLibrary.BuiltIns.SHOWN);

    private static final String DEFAULT = "default";
    private static final String START_ON_SUPERFLAT = "start_on_superflat";
    private static final String BUILT_INS = "built_ins";

    /** Whether a preset is a favourite. */
    public boolean isFavourite(String file) {
        return picks.starred().contains(file);
    }

    /** The choices with a preset made a favourite, or no longer one. */
    public Choices toggleFavourite(String file) {
        return new Choices(picks.toggleStar(file), defaultFile, startOnSuperflat, builtIns);
    }

    /** The choices once a preset is used: it becomes the latest. */
    public Choices used(String file) {
        return new Choices(picks.used(file), defaultFile, startOnSuperflat, builtIns);
    }

    /** The choices with this preset the default, in place of any other, or no longer the default when it was. */
    public Choices toggleDefault(String file) {
        return new Choices(picks, defaultFile.filter(file::equals).isPresent() ? Optional.empty() : Optional.of(file),
                startOnSuperflat, builtIns);
    }

    /** The choices with Create World starting on Superflat, or no longer. */
    public Choices toggleStartOnSuperflat() {
        return new Choices(picks, defaultFile, !startOnSuperflat, builtIns);
    }

    /** The choices with the next way of showing built-in presets: all, then not the game's, then none. */
    public Choices nextBuiltIns() {
        PresetLibrary.BuiltIns[] all = PresetLibrary.BuiltIns.values();
        return new Choices(picks, defaultFile, startOnSuperflat, all[(builtIns.ordinal() + 1) % all.length]);
    }

    /** The choices once a preset's file is renamed: they follow it. */
    public Choices renamed(String file, String newFile) {
        return new Choices(new Picks(replaced(picks.recent(), file, newFile), replaced(picks.starred(), file, newFile)),
                defaultFile.map(chosen -> chosen.equals(file) ? newFile : chosen), startOnSuperflat, builtIns);
    }

    /** The choices once a preset's file is deleted: it is no longer a favourite, recent or the default. */
    public Choices deleted(String file) {
        return new Choices(new Picks(without(picks.recent(), file), without(picks.starred(), file)),
                defaultFile.filter(chosen -> !chosen.equals(file)), startOnSuperflat, builtIns);
    }

    /**
     * Reads the choices from library.json; a missing key is no choice, and anything in a list but text is left out.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static Choices fromJson(String json) {
        JsonObject root = JsonFields.parseObject(json);
        List<String> ignored = new ArrayList<>();
        String chosen = JsonFields.text(root, DEFAULT, "", ignored);
        String shown = JsonFields.text(root, BUILT_INS, NONE.builtIns().name(), ignored).toUpperCase(Locale.ROOT);
        PresetLibrary.BuiltIns builtIns = Arrays.stream(PresetLibrary.BuiltIns.values())
                .filter(kind -> kind.name().equals(shown)).findFirst().orElse(NONE.builtIns());
        return new Choices(Picks.fromJson(json), chosen.isEmpty() ? Optional.empty() : Optional.of(chosen),
                JsonFields.bool(root, START_ON_SUPERFLAT, false, ignored), builtIns);
    }

    /** The choices as library.json keeps them. */
    public String toJson() {
        JsonObject root = JsonFields.parseObject(picks.toJson());
        defaultFile.ifPresent(file -> root.addProperty(DEFAULT, file));
        root.addProperty(START_ON_SUPERFLAT, startOnSuperflat);
        root.addProperty(BUILT_INS, builtIns.name().toLowerCase(Locale.ROOT));
        return JsonFields.PRETTY.toJson(root);
    }

    /**
     * Whether the default may be put in place of a world's superflat settings: only while they are still the game's own
     * default flat world, so settings the player has changed are never replaced.
     *
     * @param world        the world's settings now
     * @param gameDefault  the game's default flat world, as a preset
     */
    public static boolean mayApplyDefault(Preset world, Preset gameDefault) {
        return world.layers().equals(gameDefault.layers()) && world.biome().equals(gameDefault.biome())
                && world.structureSets().equals(gameDefault.structureSets())
                && world.decorations() == gameDefault.decorations() && world.lakes() == gameDefault.lakes();
    }

    private static List<String> replaced(List<String> files, String file, String newFile) {
        return files.stream().map(found -> found.equals(file) ? newFile : found).toList();
    }

    private static List<String> without(List<String> files, String file) {
        return files.stream().filter(found -> !found.equals(file)).toList();
    }
}
