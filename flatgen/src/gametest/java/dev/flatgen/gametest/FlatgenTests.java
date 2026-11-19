package dev.flatgen.gametest;

import dev.flatgen.Flatgen;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Flatgen's GameTests, the same on every node. Each loader's glue ({@code FlatgenGameTests} in
 * {@code dev.flatgen.platform.<loader>}, in this source set too) hands the game one GameTest, {@link #all}, which plays
 * every test of {@link #TESTS} and fails naming each that failed, so a test added here runs on every node with no glue
 * changed. They run on a headless GameTest server, which also shows Flatgen loads on a dedicated server. This source
 * set never reaches the jar.
 */
public final class FlatgenTests {
    /** The site the GameTest stands in: data/flatgen/structure/empty.nbt, one block of air, as no test builds. */
    public static final String SITE = "empty";
    /** How long the GameTest may run before it fails, in ticks. */
    public static final int MAX_TICKS = 100;
    /** The system property naming the file the tests write once they have played, which the build checks for. */
    private static final String PLAYED = "flatgen.played";
    /**
     * The most characters of a failure the GameTest says: the game writes it on a book by the test, whose page holds
     * 1024, and a longer one fails saving the book instead of the test. Every failure is logged in full.
     */
    private static final int SAID_AT_MOST = 900;
    /** Every test, by name, each failing by throwing. */
    private static final Map<String, Consumer<GameTestHelper>> TESTS = tests();

    private FlatgenTests() {
    }

    private static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.putAll(PresetGameTests.tests());
        tests.putAll(BlockGameTests.tests());
        tests.putAll(CodeGameTests.tests());
        tests.putAll(OptionGameTests.tests());
        tests.putAll(FactGameTests.tests());
        tests.putAll(StructureGameTests.tests());
        tests.putAll(GameRuleGameTests.tests());
        tests.putAll(LibraryGameTests.tests());
        tests.putAll(WorldTypeGameTests.tests());
        return tests;
    }

    /** Plays every test and fails naming each that failed. */
    public static void all(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        TESTS.forEach((name, test) -> play(name, test, helper, failures));
        leaveWordThatTheyPlayed();
        if (!failures.isEmpty()) {
            String said = String.join("; ", failures);
            throw new GameTestAssertException(said.length() > SAID_AT_MOST ? said.substring(0, SAID_AT_MOST) : said);
        }
        helper.succeed();
    }

    /** Fails the test with this message unless the condition holds. */
    static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static void play(String name, Consumer<GameTestHelper> test, GameTestHelper helper, List<String> failures) {
        try {
            test.accept(helper);
            Flatgen.LOGGER.info("GameTest passed: {}", name);
        } catch (RuntimeException | AssertionError failure) {
            Flatgen.LOGGER.error("GameTest failed: {}", name, failure);
            failures.add(name + ": " + failure.getMessage());
        }
    }

    private static void leaveWordThatTheyPlayed() {
        String file = System.getProperty(PLAYED);
        if (file == null) {
            return;
        }
        try {
            Files.createDirectories(Path.of(file).getParent());
            Files.writeString(Path.of(file), "played");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
