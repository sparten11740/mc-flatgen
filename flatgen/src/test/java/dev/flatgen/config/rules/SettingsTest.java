package dev.flatgen.config.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Reading and writing the player's settings, as config/flatgen/flatgen.json holds them. */
class SettingsTest {
    @Test
    void readsWhatItWrites() {
        Settings off = new Settings(false, false);
        assertEquals(off, Settings.fromJson(off.toJson()));
        assertEquals(Settings.DEFAULTS, Settings.fromJson(Settings.DEFAULTS.toJson()));
    }

    @Test
    void readsTheEnabledSwitch() {
        assertEquals(new Settings(false, false), Settings.fromJson("{\"enabled\": false}"));
    }

    @Test
    void readsTheAdvancedSwitch() {
        assertEquals(new Settings(true, true), Settings.fromJson("{\"advanced\": true}"));
        assertEquals(new Settings(true, true), Settings.DEFAULTS.withAdvanced(true));
    }

    @Test
    void keepsDefaultsForMissingKeys() {
        assertEquals(Settings.DEFAULTS, Settings.fromJson("{}"));
    }

    @Test
    void keepsDefaultsForValuesOfTheWrongType() {
        assertEquals(Settings.DEFAULTS, Settings.fromJson("{\"enabled\": \"no\"}"));
    }

    @Test
    void ignoresKeysItDoesNotKnow() {
        assertEquals(new Settings(false, false), Settings.fromJson("{\"enabled\": false, \"fromALaterVersion\": 3}"));
    }

    @Test
    void refusesTextThatIsNotAJsonObject() {
        assertThrows(IllegalArgumentException.class, () -> Settings.fromJson("enabled = false"));
        assertThrows(IllegalArgumentException.class, () -> Settings.fromJson("[true]"));
    }
}
