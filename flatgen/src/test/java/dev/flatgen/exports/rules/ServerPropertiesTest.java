package dev.flatgen.exports.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/** The server.properties lines, read back as the game reads the file. */
class ServerPropertiesTest {
    private static final String SETTINGS = "{\"biome\":\"minecraft:plains\",\"layers\":[{\"block\":"
            + "\"minecraft:bedrock\",\"height\":1}],\"lakes\":false,\"features\":false}";

    @Test
    void readsBackAsTheFlatWorldTypeAndTheSettings() throws IOException {
        Properties read = new Properties();
        read.load(new StringReader(ServerProperties.lines(SETTINGS)));
        assertEquals("minecraft:flat", read.getProperty("level-type"));
        assertEquals(SETTINGS, read.getProperty("generator-settings"));
    }

    @Test
    void escapesBackslashesAndWhatIsNotPlainAscii() throws IOException {
        String odd = "{\"name\":\"Überwelt \\\\ ☃\"}";
        Properties read = new Properties();
        read.load(new StringReader(ServerProperties.lines(odd)));
        assertEquals(odd, read.getProperty("generator-settings"));
        assertEquals("\\u00dcberwelt \\\\\\\\ \\u2603", ServerProperties.escaped("Überwelt \\\\ ☃"));
    }
}
