package dev.flatgen.options;

import dev.flatgen.Flatgen;
import dev.flatgen.mc.Resources;
import dev.flatgen.options.rules.OptionReader;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.platform.ClientPlatform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The world options installed: every file {@code assets/<namespace>/flatgen/options/<name>.json} of the game's
 * resources, from mods and resource packs, that reads and whose required mods are loaded.
 */
public final class InstalledOptions {
    /** The folder of {@code assets/<namespace>/} option files are in. */
    private static final String FOLDER = "flatgen/options";

    private InstalledOptions() {
    }

    /** Reads every option file now, logging each one left out and why; in group and id order. */
    public static List<WorldOption> load() {
        List<WorldOption> options = new ArrayList<>();
        for (Map.Entry<String, String> file : Resources.jsonFiles(FOLDER).entrySet()) {
            OptionReader.Reading reading = OptionReader.read(file.getKey(), file.getValue());
            reading.option().filter(InstalledOptions::hasRequiredMods).ifPresent(options::add);
            if (!reading.problems().isEmpty()) {
                Flatgen.LOGGER.warn("Flatgen's world option {} ({}): {}", file.getKey(), path(file.getKey()),
                        String.join("; ", reading.problems()));
            }
        }
        options.sort(Comparator.comparing(WorldOption::group).thenComparing(WorldOption::id));
        return options;
    }

    private static boolean hasRequiredMods(WorldOption option) {
        List<String> missing = option.requires().stream()
                .filter(mod -> !ClientPlatform.get().isModLoaded(mod)).toList();
        if (!missing.isEmpty()) {
            Flatgen.LOGGER.info("Flatgen hides the world option {}, which needs {}", option.id(),
                    String.join(", ", missing));
        }
        return missing.isEmpty();
    }

    /** Where an option's file is, for the log. */
    private static String path(String id) {
        int colon = id.indexOf(':');
        return "assets/" + id.substring(0, colon) + "/" + FOLDER + "/" + id.substring(colon + 1) + ".json";
    }
}
