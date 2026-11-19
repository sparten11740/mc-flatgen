package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** The Blockpilot releases Flatgen's commands are registered with, and the one the build compiles against. */
class BlockpilotReleaseTest {
    @Test
    void takesTheFirstReleaseAndLaterOnes() {
        assertTrue(BlockpilotRelease.supported("1.4.0"));
        assertTrue(BlockpilotRelease.supported("1.4.0+1.21.1-neoforge"));
        assertTrue(BlockpilotRelease.supported("1.4.2+1.21.1-fabric"));
        assertTrue(BlockpilotRelease.supported("1.10.0"));
        assertTrue(BlockpilotRelease.supported("2.0.0-alpha.1"));
    }

    @Test
    void leavesOlderReleasesAndUnreadableVersionsAlone() {
        assertFalse(BlockpilotRelease.supported("1.1.1+1.21.1-neoforge"));
        assertFalse(BlockpilotRelease.supported("1.3.9"));
        assertFalse(BlockpilotRelease.supported("0.9"));
        assertFalse(BlockpilotRelease.supported("${version}"));
    }

    @Test
    void isTheReleaseTheBuildNames() throws IOException {
        Path properties = Path.of(System.getProperty("flatgen.project"), "gradle.properties");
        String release = Files.readAllLines(properties).stream().filter(line -> line.startsWith("blockpilot_release="))
                .map(line -> line.substring(line.indexOf('=') + 1).trim()).findFirst().orElseThrow();
        assertEquals(release, BlockpilotRelease.FIRST);
    }
}
