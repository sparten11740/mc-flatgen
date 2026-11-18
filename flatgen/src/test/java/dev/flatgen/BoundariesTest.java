package dev.flatgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The package rules of docs/plan.md 3.1 and 3.3, read from the sources as checked in: each test is one rule and lists
 * every reference that breaks it. A reference is an import or a fully qualified name in the code outside comments and
 * text.
 */
class BoundariesTest {
    /** The packages of each loader's API, by the platform half allowed to name them. */
    private static final Map<String, List<String>> LOADER_APIS = Map.of(
            "neoforge", List.of("net.neoforged."),
            "fabric", List.of("net.fabricmc."));
    /** What rules may use besides other rules: the JDK and Gson. */
    private static final List<String> RULES_LIBRARIES = List.of("java.", "com.google.gson.");
    private static final String ROOT = "dev.flatgen";
    private static final String PLATFORM = ROOT + ".platform";
    private static final String VERSION_ADAPTERS = ROOT + ".mc";
    private static final String GUI_TOOLKIT = ROOT + ".client.gui";
    private static final String COMPAT = ROOT + ".compat";
    /** The game's drawing and its own widgets, which only the toolkit and the version adapters use (plan 3.3). */
    private static final List<String> GAME_GUI = List.of(
            "net.minecraft.client.gui.GuiGraphics", "net.minecraft.client.gui.components.");

    private static final List<SourceFile> SOURCES = SourceFile.readAll(
            Path.of(System.getProperty("flatgen.project"), "src"));

    @Test
    void readsTheSources() {
        assertTrue(SOURCES.stream().anyMatch(file -> file.packageName().equals(ROOT)), "found no sources in " + ROOT);
    }

    @Test
    void rulesUseOnlyTheJdkGsonAndOtherRules() {
        assertNone(SourceFile::isRules, (file, reference) -> !isRulesOrLibrary(reference));
    }

    @Test
    void onlyALoadersOwnPlatformHalfNamesItsApi() {
        assertNone(file -> true, (file, reference) -> isLoaderApi(reference) && !isAllowedApi(file, reference));
    }

    @Test
    void nothingOutsideAPlatformHalfDependsOnIt() {
        assertNone(file -> true, BoundariesTest::reachesAnotherPlatformHalf);
    }

    @Test
    void versionAdaptersDependOnlyOnRulesTheRootAndEachOther() {
        assertNone(file -> isUnder(file.packageName(), VERSION_ADAPTERS),
                (file, reference) -> isOwn(reference) && !isRulesOrRoot(reference)
                        && !isUnder(packageOf(reference), VERSION_ADAPTERS));
    }

    @Test
    void theGuiToolkitDependsOnlyOnItselfVersionAdaptersRulesAndTheRoot() {
        assertNone(file -> isUnder(file.packageName(), GUI_TOOLKIT),
                (file, reference) -> isOwn(reference) && !isRulesOrRoot(reference)
                        && !isUnder(packageOf(reference), GUI_TOOLKIT)
                        && !isUnder(packageOf(reference), VERSION_ADAPTERS));
    }

    @Test
    void onlyTheGuiToolkitAndVersionAdaptersDrawWithTheGame() {
        assertNone(file -> !isUnder(file.packageName(), GUI_TOOLKIT)
                        && !isUnder(file.packageName(), VERSION_ADAPTERS),
                (file, reference) -> startsWithAny(reference, GAME_GUI));
    }

    @Test
    void theRootDependsOnNothingOfFlatgen() {
        assertNone(file -> file.packageName().equals(ROOT),
                (file, reference) -> isOwn(reference) && !packageOf(reference).equals(ROOT));
    }

    @Test
    void onlyPlatformHalvesDependOnIntegrations() {
        assertNone(file -> file.platformHalf().isEmpty() && !isUnder(file.packageName(), COMPAT),
                (file, reference) -> isUnder(packageOf(reference), COMPAT));
    }

    @Test
    void onlyClientCodeIntegrationsAndPlatformHalvesDependOnClientCode() {
        assertNone(file -> !file.isClient() && file.platformHalf().isEmpty() && !isUnder(file.packageName(), COMPAT),
                (file, reference) -> isOwn(reference) && hasSegment(packageOf(reference), "client"));
    }

    /** Fails listing every reference of the chosen files that breaks the rule. */
    private static void assertNone(Predicate<SourceFile> files, BiPredicate<SourceFile, String> breaksRule) {
        List<String> found = SOURCES.stream()
                .filter(files)
                .flatMap(file -> file.referencesMatching(breaksRule))
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of(), found, String.join("\n", found));
    }

    private static boolean isRulesOrLibrary(String reference) {
        boolean isRules = isOwn(reference) && hasSegment(packageOf(reference), "rules");
        return isRules || startsWithAny(reference, RULES_LIBRARIES);
    }

    private static boolean isRulesOrRoot(String reference) {
        return hasSegment(packageOf(reference), "rules") || packageOf(reference).equals(ROOT);
    }

    private static boolean isAllowedApi(SourceFile file, String reference) {
        return startsWithAny(reference, file.platformHalf().map(LOADER_APIS::get).orElse(List.of()));
    }

    private static boolean reachesAnotherPlatformHalf(SourceFile file, String reference) {
        Optional<String> half = platformHalfOf(packageOf(reference));
        return half.isPresent() && !half.equals(file.platformHalf());
    }

    private static boolean isOwn(String reference) {
        return isUnder(packageOf(reference), ROOT);
    }

    private static boolean isLoaderApi(String reference) {
        return LOADER_APIS.values().stream().anyMatch(apis -> startsWithAny(reference, apis));
    }

    private static boolean startsWithAny(String reference, List<String> prefixes) {
        return prefixes.stream().anyMatch(reference::startsWith);
    }

    private static boolean hasSegment(String name, String segment) {
        return Arrays.asList(name.split("\\.")).contains(segment);
    }

    private static boolean isUnder(String packageName, String parent) {
        return packageName.equals(parent) || packageName.startsWith(parent + ".");
    }

    /**
     * The package a reference is in: its leading segments up to the first class name, which Java's naming starts with a
     * capital, so nested classes, static members and wildcards all resolve to their package.
     */
    private static String packageOf(String reference) {
        List<String> segments = new ArrayList<>();
        for (String segment : reference.split("\\.")) {
            if (Character.isUpperCase(segment.charAt(0))) {
                break;
            }
            segments.add(segment);
        }
        return String.join(".", segments);
    }

    /** The loader half of dev.flatgen.platform a package is in, if it is in one. */
    private static Optional<String> platformHalfOf(String packageName) {
        if (!packageName.startsWith(PLATFORM + ".")) {
            return Optional.empty();
        }
        String half = packageName.substring(PLATFORM.length() + 1).split("\\.")[0];
        return LOADER_APIS.containsKey(half) ? Optional.of(half) : Optional.empty();
    }

    /** One Java source of the mod: where it is, its package and every name it references. */
    private record SourceFile(Path path, String packageName, Set<String> references) {
        private static final Pattern PACKAGE = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
        private static final Pattern IMPORT =
                Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+?)(?:\\.\\*)?;", Pattern.MULTILINE);
        /** A fully qualified class name in code: lower-case package segments, then a capitalised class. */
        private static final Pattern QUALIFIED_NAME = Pattern.compile("\\b(?:[a-z][a-z0-9_]*\\.)+[A-Z]\\w*");
        /** Comments, text in quotes and the package and import lines, which name nothing the code uses. */
        private static final Pattern NOT_CODE = Pattern.compile(
                "/\\*.*?\\*/|//[^\\n]*|\"(?:[^\"\\\\]|\\\\.)*\"|^(?:package|import)\\s[^;]*;",
                Pattern.DOTALL | Pattern.MULTILINE);

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
            Set<String> references = new LinkedHashSet<>(matches(IMPORT, text));
            references.addAll(matches(QUALIFIED_NAME, NOT_CODE.matcher(text).replaceAll(" ")));
            return new SourceFile(path, packageName, references);
        }

        private static List<String> matches(Pattern pattern, String text) {
            Matcher matcher = pattern.matcher(text);
            List<String> found = new ArrayList<>();
            while (matcher.find()) {
                found.add(matcher.groupCount() > 0 ? matcher.group(1) : matcher.group());
            }
            return found;
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

        /** Each reference that breaks the rule, as a line naming this file and the reference. */
        Stream<String> referencesMatching(BiPredicate<SourceFile, String> breaksRule) {
            return references.stream()
                    .filter(reference -> breaksRule.test(this, reference))
                    .map(reference -> path + " references " + reference);
        }
    }
}
