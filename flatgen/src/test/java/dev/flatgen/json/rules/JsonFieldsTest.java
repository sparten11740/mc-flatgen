package dev.flatgen.json.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The lenient reading every Flatgen file goes through: defaults for what is missing, notes for what is wrong. */
class JsonFieldsTest {
    private static final JsonObject OBJECT = JsonFields.parseObject(
            "{\"on\": true, \"name\": \"Tall\", \"depth\": 3, \"half\": 1.5, \"none\": null, \"list\": [1]}");

    @Test
    void readsValuesOfTheRightKindWithoutProblems() {
        List<String> problems = new ArrayList<>();
        assertEquals(true, JsonFields.bool(OBJECT, "on", false, problems));
        assertEquals("Tall", JsonFields.text(OBJECT, "name", "", problems));
        assertEquals(3, JsonFields.integer(OBJECT, "depth", 0, problems));
        assertEquals(1, JsonFields.array(OBJECT, "list", problems).orElseThrow().size());
        assertEquals(List.of(), problems);
    }

    @Test
    void treatsMissingAndNullKeysAsMissing() {
        List<String> problems = new ArrayList<>();
        assertEquals(7, JsonFields.integer(OBJECT, "absent", 7, problems));
        assertEquals("fallback", JsonFields.text(OBJECT, "none", "fallback", problems));
        assertEquals(Optional.empty(), JsonFields.object(OBJECT, "none", problems));
        assertEquals(List.of(), problems);
    }

    @Test
    void keepsTheFallbackForValuesOfTheWrongKindAndNotesEach() {
        List<String> problems = new ArrayList<>();
        assertEquals(false, JsonFields.bool(OBJECT, "name", false, problems));
        assertEquals("", JsonFields.text(OBJECT, "depth", "", problems));
        assertEquals(0, JsonFields.integer(OBJECT, "half", 0, problems));
        assertEquals(Optional.empty(), JsonFields.object(OBJECT, "list", problems));
        assertEquals(List.of("name should be true or false", "depth should be text", "half should be a whole number",
                "list should be an object"), problems);
    }

    @Test
    void refusesTextThatIsNotAJsonObject() {
        assertThrows(IllegalArgumentException.class, () -> JsonFields.parseObject("{"));
        assertThrows(IllegalArgumentException.class, () -> JsonFields.parseObject("\"text\""));
    }
}
