package dev.flatgen.json.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Reading Flatgen's JSON files leniently, as all of them are read: a missing key keeps its default, and a key of the
 * wrong kind keeps it too and is noted in a list of problems for the log or the player.
 */
public final class JsonFields {
    /** Writes JSON as Flatgen's files keep it: one key a line, indented, characters such as {@code <} as they are. */
    public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonFields() {
    }

    /**
     * The JSON object the text holds.
     *
     * @throws IllegalArgumentException when the text is not a JSON object
     */
    public static JsonObject parseObject(String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("not JSON", exception);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("not a JSON object");
        }
        return root.getAsJsonObject();
    }

    /** The key's true or false, or the fallback when it is missing or not one. */
    public static boolean bool(JsonObject object, String key, boolean fallback, List<String> problems) {
        return element(object, key, JsonFields::isBoolean, "true or false", problems)
                .map(JsonElement::getAsBoolean).orElse(fallback);
    }

    /** The key's text, or the fallback when it is missing or not text. */
    public static String text(JsonObject object, String key, String fallback, List<String> problems) {
        return element(object, key, JsonFields::isText, "text", problems)
                .map(JsonElement::getAsString).orElse(fallback);
    }

    /** The key's whole number, or the fallback when it is missing or not one. */
    public static int integer(JsonObject object, String key, int fallback, List<String> problems) {
        return element(object, key, JsonFields::isInteger, "a whole number", problems)
                .map(JsonElement::getAsInt).orElse(fallback);
    }

    /** The key's list, if it has one. */
    public static Optional<JsonArray> array(JsonObject object, String key, List<String> problems) {
        return element(object, key, JsonElement::isJsonArray, "a list", problems).map(JsonElement::getAsJsonArray);
    }

    /** Each text of the key's list, empty when it has none; anything else in the list is left out and noted. */
    public static List<String> texts(JsonObject object, String key, List<String> problems) {
        return array(object, key, problems).map(array -> texts(array, key, problems)).orElse(List.of());
    }

    /** Each text of a list; anything else in it is left out and noted under the list's key. */
    public static List<String> texts(JsonArray array, String key, List<String> problems) {
        List<String> texts = new ArrayList<>();
        for (JsonElement element : array) {
            if (isText(element)) {
                texts.add(element.getAsString());
            } else {
                problems.add(key + " should hold only text, so " + element + " was left out");
            }
        }
        return texts;
    }

    /** A list of texts as JSON. */
    public static JsonArray textArray(List<String> texts) {
        JsonArray array = new JsonArray();
        texts.forEach(array::add);
        return array;
    }

    /** The key's object, if it has one. */
    public static Optional<JsonObject> object(JsonObject object, String key, List<String> problems) {
        return element(object, key, JsonElement::isJsonObject, "an object", problems).map(JsonElement::getAsJsonObject);
    }

    /** Whether a value is true or false. */
    public static boolean isBoolean(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
    }

    /** Whether a value is text. */
    public static boolean isText(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

    /** Whether a value is a whole number that fits an int. */
    public static boolean isInteger(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                && value.getAsDouble() == value.getAsInt();
    }

    private static Optional<JsonElement> element(JsonObject object, String key, Predicate<JsonElement> isExpected,
            String expected, List<String> problems) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return Optional.empty();
        }
        if (!isExpected.test(value)) {
            problems.add(key + " should be " + expected);
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
