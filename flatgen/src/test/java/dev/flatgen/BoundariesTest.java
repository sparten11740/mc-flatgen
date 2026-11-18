package dev.flatgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The package boundaries of docs/plan.md 3.1, read from the sources as checked in: each test is one rule and lists
 * every import that breaks it.
 */
class BoundariesTest {
    /** The packages of each loader's API, by the platform half allowed to name them. */
    private static final Map<String, List<String>> LOADER_APIS = Map.of(
            "neoforge", List.of("net.neoforged."),
            "fabric", List.of("net.fabricmc."));
    /** The game's own packages. */
    private static final List<String> GAME_APIS = List.of("net.minecraft.", "com.mojang.");
    private static final String ROOT_PACKAGE = "dev.flatgen";
    private static final String ROOT = ROOT_PACKAGE + ".";
    private static final String PLATFORM = ROOT + "platform.";
    private static final String VERSION_ADAPTERS = ROOT + "mc";

    private static final List<SourceFile> SOURCES = SourceFile.readAll(
            Path.of(System.getProperty("flatgen.project"), "src"));

    @Test
    void readsTheSources() {
        assertTrue(SOURCES.stream().anyMatch(file -> file.packageName().equals(ROOT_PACKAGE)),
                "found no sources in " + ROOT_PACKAGE);
    }

    @Test
    void rulesNameNeitherTheGameNorALoader() {
        List<String> apis = new ArrayList<>(GAME_APIS);
        LOADER_APIS.values().forEach(apis::addAll);
        assertNone(SOURCES.stream()
                .filter(SourceFile::isRules)
                .flatMap(file -> file.importsMatching(imported -> startsWithAny(imported, apis))));
    }

    @Test
    void rulesDependOnlyOnRules() {
        assertNone(SOURCES.stream()
                .filter(SourceFile::isRules)
                .flatMap(file -> file.importsMatching(imported -> isOwn(imported) && !hasSegment(imported, "rules"))));
    }

    @Test
    void onlyALoadersOwnPlatformHalfNamesItsApi() {
        assertNone(SOURCES.stream().flatMap(file -> {
            List<String> allowed = file.platformHalf().map(LOADER_APIS::get).orElse(List.of());
            return file.importsMatching(imported -> isLoaderApi(imported) && !startsWithAny(imported, allowed));
        }));
    }

    @Test
    void nothingOutsideAPlatformHalfDependsOnIt() {
        assertNone(SOURCES.stream().flatMap(file -> file.importsMatching(imported -> {
            Optional<String> half = platformHalfOf(imported);
            return half.isPresent() && !half.equals(file.platformHalf());
        })));
    }

    @Test
    void versionAdaptersDependOnlyOnRulesTheRootAndEachOther() {
        assertNone(SOURCES.stream()
                .filter(file -> isUnder(file.packageName(), VERSION_ADAPTERS))
                .flatMap(file -> file.importsMatching(imported -> isOwn(imported) && !hasSegment(imported, "rules")
                        && !isUnder(packageOf(imported), VERSION_ADAPTERS) && !isRoot(imported))));
    }

    @Test
    void onlyClientCodeAndPlatformHalvesDependOnClientCode() {
        assertNone(SOURCES.stream()
                .filter(file -> !file.isClient() && file.platformHalf().isEmpty())
                .flatMap(file -> file.importsMatching(imported -> isOwn(imported) && hasSegment(imported, "client"))));
    }

    private static void assertNone(Stream<String> violations) {
        List<String> found = violations.sorted().collect(Collectors.toList());
        assertEquals(List.of(), found, String.join("\n", found));
    }

    private static boolean isOwn(String imported) {
        return imported.startsWith(ROOT);
    }

    /** Whether an import is of a class in dev.flatgen itself, such as {@link Flatgen}. */
    private static boolean isRoot(String imported) {
        return packageOf(imported).equals(ROOT_PACKAGE);
    }

    private static boolean isLoaderApi(String imported) {
        return LOADER_APIS.values().stream().anyMatch(apis -> startsWithAny(imported, apis));
    }

    private static boolean startsWithAny(String imported, List<String> prefixes) {
        return prefixes.stream().anyMatch(imported::startsWith);
    }

    private static boolean hasSegment(String name, String segment) {
        return Arrays.asList(name.split("\\.")).contains(segment);
    }

    private static boolean isUnder(String packageName, String parent) {
        return packageName.equals(parent) || packageName.startsWith(parent + ".");
    }

    /** The package of an imported class: its name without the last segment. */
    private static String packageOf(String imported) {
        return imported.substring(0, Math.max(0, imported.lastIndexOf('.')));
    }

    /** The loader half of dev.flatgen.platform a name is in, if it is in one. */
    private static Optional<String> platformHalfOf(String name) {
        if (!name.startsWith(PLATFORM)) {
            return Optional.empty();
        }
        String half = name.substring(PLATFORM.length()).split("\\.")[0];
        return LOADER_APIS.containsKey(half) ? Optional.of(half) : Optional.empty();
    }

    /** One Java source of the mod: where it is, its package and what it imports. */
    private record SourceFile(Path path, String packageName, List<String> imports) {
        private static final Pattern PACKAGE = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
        private static final Pattern IMPORT =
                Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+?)(?:\\.\\*)?;", Pattern.MULTILINE);

        /** Every source of the mod's own trees under src/, leaving out the tests. */
        static List<SourceFile> readAll(Path src) {
            try (Stream<Path> files = Files.walk(src)) {
                return files
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !src.relativize(path).startsWith("test"))
                        .map(SourceFile::read)
                        .collect(Collectors.toList());
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        private static SourceFile read(Path path) {
            String text;
            try {
                text = Files.readString(path);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
            Matcher packageMatch = PACKAGE.matcher(text);
            String packageName = packageMatch.find() ? packageMatch.group(1) : "";
            List<String> imports = new ArrayList<>();
            Matcher importMatch = IMPORT.matcher(text);
            while (importMatch.find()) {
                imports.add(importMatch.group(1));
            }
            return new SourceFile(path, packageName, imports);
        }

        boolean isRules() {
            return hasSegment(packageName, "rules");
        }

        boolean isClient() {
            return hasSegment(packageName, "client");
        }

        Optional<String> platformHalf() {
            return platformHalfOf(packageName);
        }

        /** Each import that matches, as a line naming this file and the import. */
        Stream<String> importsMatching(Predicate<String> breaksRule) {
            return imports.stream().filter(breaksRule).map(imported -> path + " imports " + imported);
        }
    }
}
