package dev.flatgen.options.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code game_rule}: sets a game rule on the Create World screen, as the game's own game rules screen does.
 *
 * @param rule  the rule's name as the option format names it, which is Minecraft 1.21.1's, such as
 *              {@code doWeatherCycle}; {@code mc/GameRuleAccess} turns it into a later version's name
 * @param value its value as the game writes it: {@code true}, {@code false} or a whole number
 */
public record GameRule(String rule, String value) implements Effect {
    /** The format's name of the rule that turns mobs spawning on their own on or off. */
    public static final String MOB_SPAWNING = "doMobSpawning";
    /** The prefix of the name under which a game rule is a setting that options may clash over. */
    private static final String SETTING = "game_rule ";

    /** Reads {@code {"rule": "doWeatherCycle", "value": false}}. */
    static Optional<Effect> read(JsonObject effect, List<String> problems) {
        String rule = JsonFields.text(effect, "rule", "", problems);
        JsonElement value = effect.get("value");
        boolean readable = value != null && (JsonFields.isBoolean(value) || JsonFields.isInteger(value));
        if (rule.isEmpty() || !readable) {
            problems.add("a game_rule needs a rule's name and a value of true, false or a whole number");
            return Optional.empty();
        }
        return Optional.of(new GameRule(rule,
                JsonFields.isBoolean(value) ? value.getAsString() : Integer.toString(value.getAsInt())));
    }

    @Override
    public void atDone(WorldSetup setup) {
        setup.setGameRule(rule, value);
    }

    @Override
    public Map<String, String> settings() {
        return Map.of(SETTING + rule, value);
    }
}
