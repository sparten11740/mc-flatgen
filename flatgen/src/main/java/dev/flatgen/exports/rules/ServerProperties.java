package dev.flatgen.exports.rules;

import java.util.Locale;

/**
 * The lines a dedicated server's {@code server.properties} needs to make a superflat world of a preset (F21): its
 * world type and its generator settings, written as the file reads them.
 */
public final class ServerProperties {
    /** The line choosing the superflat world type, its colon escaped as the game writes it. */
    private static final String LEVEL_TYPE = "level-type=minecraft\\:flat";
    private static final String GENERATOR_SETTINGS = "generator-settings=";
    /** The highest character a properties file holds as it is; the rest are written as escapes. */
    private static final char LAST_PLAIN = '~';

    private ServerProperties() {
    }

    /** Both lines, for generator settings as one line of JSON, as the game writes the flat settings. */
    public static String lines(String generatorSettings) {
        return LEVEL_TYPE + "\n" + GENERATOR_SETTINGS + escaped(generatorSettings) + "\n";
    }

    /** A value as a properties file reads it back: backslashes doubled, other than plain ASCII as escapes. */
    static String escaped(String value) {
        StringBuilder written = new StringBuilder();
        for (char character : value.toCharArray()) {
            if (character == '\\') {
                written.append("\\\\");
            } else if (character < ' ' || character > LAST_PLAIN) {
                written.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
            } else {
                written.append(character);
            }
        }
        return written.toString();
    }
}
