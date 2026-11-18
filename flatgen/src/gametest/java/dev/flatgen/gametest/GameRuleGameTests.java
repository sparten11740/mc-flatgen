package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.mc.GameRuleAccess;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameRules;

/** World options' game rules (F10) as the game takes them: by the format's names, a value a rule cannot take left. */
final class GameRuleGameTests {
    private GameRuleGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("always day, always clear and no mobs set the game's rules", GameRuleGameTests::flatgensOwn);
        tests.put("a rule the game lacks or a value it cannot take is left", GameRuleGameTests::leftAsTheyWere);
        tests.put("rules turned off go back to the game's defaults", GameRuleGameTests::defaults);
        return tests;
    }

    private static void flatgensOwn(GameTestHelper helper) {
        GameRuleAccess.Changed changed = GameRuleAccess.changed(new GameRules(), Map.of("doDaylightCycle", "false",
                "doWeatherCycle", "false", "doMobSpawning", "false", "randomTickSpeed", "7"));
        GameRules rules = changed.rules();
        check(!rules.getBoolean(GameRules.RULE_DAYLIGHT), "the daylight cycle is still on");
        check(!rules.getBoolean(GameRules.RULE_WEATHER_CYCLE), "the weather cycle is still on");
        check(!rules.getBoolean(GameRules.RULE_DOMOBSPAWNING), "mob spawning is still on");
        check(rules.getInt(GameRules.RULE_RANDOMTICKING) == 7, "the random tick speed is not 7");
        check(changed.left().isEmpty(), "left " + changed.left());
    }

    private static void leftAsTheyWere(GameTestHelper helper) {
        GameRuleAccess.Changed changed = GameRuleAccess.changed(new GameRules(), Map.of("doWeatherCycle", "maybe",
                "randomTickSpeed", "fast", "flatgenNoSuchRule", "true"));
        check(changed.rules().getBoolean(GameRules.RULE_WEATHER_CYCLE), "the weather cycle took \"maybe\"");
        check(changed.rules().getInt(GameRules.RULE_RANDOMTICKING) == GameRules.DEFAULT_RANDOM_TICK_SPEED,
                "the random tick speed took \"fast\"");
        check(changed.left().stream().sorted().toList().equals(List.of("doWeatherCycle", "flatgenNoSuchRule",
                "randomTickSpeed")), "left " + changed.left());
    }

    private static void defaults(GameTestHelper helper) {
        Map<String, String> defaults = GameRuleAccess.defaults(List.of("doDaylightCycle", "flatgenNoSuchRule"));
        check(defaults.equals(Map.of("doDaylightCycle", "true")), "the defaults are " + defaults);
    }
}
