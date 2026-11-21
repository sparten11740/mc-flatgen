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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Flatgen's language files and its code agree (F19): every key the code or Flatgen's data files name is in each
 * language file, each file has only keys something names, and each translation keeps the English text's placeholders. The code names its keys whole, never joining one from
 * parts, so every one is checked; Flatgen's options are named by their files, as {@code option.flatgen.always_day},
 * the one key the code joins being the options' and their groups', which mods' files name.
 */
class LanguageFilesTest {
    private static final Path SRC = Path.of(System.getProperty("flatgen.project"), "src", "main");
    private static final Path LANG = SRC.resolve("resources/assets/flatgen/lang");
    private static final Path DATA = SRC.resolve("resources/assets/flatgen/flatgen");
    /** Every language Flatgen has a file for, by its name, as {@code en_us}. */
    private static final List<String> LANGUAGES = languages();
    /** A placeholder the game fills in a text, as {@code %s}, {@code %d} or {@code %2$s}. */
    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[sd]");
    /** A key as Flatgen's code and data write them, in quotes; one ending in a dot begins an option's or a group's. */
    private static final Pattern KEY = Pattern.compile("\"((?:flatgen|option|option_group|preset)\\.[a-z0-9_.]+)\"");
    private static final Set<String> NAMED = named();

    @Test
    void everyKeyNamedIsInEachLanguage() {
        for (String language : LANGUAGES) {
            Set<String> keys = keys(language);
            Set<String> missing = NAMED.stream().filter(key -> !keys.contains(key))
                    .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(Set.of(), missing, language + " lacks keys");
        }
    }

    @Test
    void eachLanguageHasOnlyKeysSomethingNames() {
        for (String language : LANGUAGES) {
            Set<String> unused = keys(language).stream().filter(key -> !NAMED.contains(key))
                    .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(Set.of(), unused, language + " has keys nothing names");
        }
    }

    @Test
    void theLanguagesHaveTheSameKeys() {
        for (String language : LANGUAGES) {
            assertEquals(keys("en_us"), keys(language), language + " has other keys than en_us");
        }
    }

    @Test
    void eachTranslationKeepsTheEnglishPlaceholders() {
        JsonObject english = texts("en_us");
        for (String language : LANGUAGES) {
            JsonObject texts = texts(language);
            List<String> changed = english.keySet().stream().filter(texts::has)
                    .filter(key -> !placeholders(english, key).equals(placeholders(texts, key))).toList();
            assertEquals(List.of(), changed, language + " changes the placeholders of these texts");
        }
    }

    /** The languages of the files in the language folder. */
    private static List<String> languages() {
        return walk(LANG).stream().map(file -> file.getFileName().toString())
                .filter(name -> name.endsWith(".json")).map(name -> name.replace(".json", "")).sorted().toList();
    }

    /** A text's placeholders, sorted, as the game fills them whatever order a language puts them in. */
    private static List<String> placeholders(JsonObject texts, String key) {
        return PLACEHOLDER.matcher(texts.get(key).getAsString()).results().map(MatchResult::group)
                .map(found -> found.replaceAll("^%\\d+\\$", "%")).sorted().toList();
    }

    /** Every key the Java code and Flatgen's data files name whole, and the keys of its options. */
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

    /** The keys a file names whole; a file's name, as {@code flatgen.json}, is none, nor is a key's beginning. */
    private static List<String> keysIn(Path file) {
        Matcher matcher = KEY.matcher(read(file));
        return matcher.results().map(found -> found.group(1))
                .filter(key -> !key.endsWith(".json") && !key.endsWith(".")).toList();
    }

    private static Set<String> keys(String language) {
        return texts(language).keySet();
    }

    private static JsonObject texts(String language) {
        return JsonParser.parseString(read(LANG.resolve(language + ".json"))).getAsJsonObject();
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
