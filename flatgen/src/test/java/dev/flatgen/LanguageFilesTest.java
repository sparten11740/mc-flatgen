package dev.flatgen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Flatgen's language files and its code agree (F19): every key the code or Flatgen's data files name is in each
 * language file, and each file has only keys something names. The code names a key whole, or by a beginning it ends
 * with a name of its own ({@code "flatgen.facts.weather." + weather}), or by a key with {@code _tip} after it for a
 * button's tooltip; Flatgen's options are named by their files, as {@code option.flatgen.always_day}.
 */
class LanguageFilesTest {
    private static final Path SRC = Path.of(System.getProperty("flatgen.project"), "src", "main");
    private static final Path LANG = SRC.resolve("resources/assets/flatgen/lang");
    private static final Path DATA = SRC.resolve("resources/assets/flatgen/flatgen");
    private static final List<String> LANGUAGES = List.of("en_us", "de_de");
    /** A key as Flatgen's code and data write them, in quotes; one ending in a dot is the beginning of keys. */
    private static final Pattern KEY = Pattern.compile("\"((?:flatgen|option|option_group|preset)\\.[a-z0-9_.]+)\"");
    /** What a tooltip's key adds to its button's. */
    private static final String TIP = "_tip";
    private static final Set<String> NAMED = named();

    @Test
    void everyKeyNamedIsInEachLanguage() {
        for (String language : LANGUAGES) {
            Set<String> keys = keys(language);
            Set<String> missing = NAMED.stream().filter(key -> !key.endsWith("."))
                    .filter(key -> !keys.contains(key)).collect(Collectors.toCollection(TreeSet::new));
            assertEquals(Set.of(), missing, language + " lacks keys");
        }
    }

    @Test
    void eachLanguageHasOnlyKeysSomethingNames() {
        for (String language : LANGUAGES) {
            Set<String> unused = keys(language).stream().filter(key -> !isNamed(key))
                    .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(Set.of(), unused, language + " has keys nothing names");
        }
    }

    @Test
    void theLanguagesHaveTheSameKeys() {
        assertEquals(keys("en_us"), keys("de_de"));
    }

    private static boolean isNamed(String key) {
        String stem = key.endsWith(TIP) ? key.substring(0, key.length() - TIP.length()) : key;
        return NAMED.contains(key) || NAMED.contains(stem)
                || NAMED.stream().anyMatch(named -> named.endsWith(".") && key.startsWith(named));
    }

    /** Every key or beginning of keys the Java code and Flatgen's data files name, and the keys of its options. */
    private static Set<String> named() {
        Set<String> found = new TreeSet<>();
        Stream.of(SRC.resolve("java"), DATA).flatMap(folder -> walk(folder).stream())
                .forEach(file -> found.addAll(keysIn(file)));
        walk(DATA.resolve("options")).forEach(file -> found.addAll(optionKeys(file)));
        return found;
    }

    /** The keys an option of Flatgen's is named by when its file names none: its name, its description, its group. */
    private static List<String> optionKeys(Path file) {
        String name = file.getFileName().toString().replace(".json", "");
        JsonObject option = JsonParser.parseString(read(file)).getAsJsonObject();
        String key = "option.flatgen." + name;
        List<String> keys = new ArrayList<>(List.of(key, key + ".description"));
        if (option.has("group") && option.get("group").getAsString().contains(":")) {
            keys.add("option_group." + option.get("group").getAsString().replace(':', '.'));
        }
        return keys;
    }

    /** The keys a file names; a file's name, as {@code flatgen.json}, is none. */
    private static List<String> keysIn(Path file) {
        Matcher matcher = KEY.matcher(read(file));
        return matcher.results().map(found -> found.group(1)).filter(key -> !key.endsWith(".json")).toList();
    }

    private static Set<String> keys(String language) {
        return JsonParser.parseString(read(LANG.resolve(language + ".json"))).getAsJsonObject().keySet();
    }

    private static List<Path> walk(Path folder) {
        try (Stream<Path> files = Files.walk(folder)) {
            return files.filter(Files::isRegularFile).toList();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
