package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;
//? if >=1.21.11 {
/*import static net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME;
import static net.minecraft.world.level.gamerules.GameRules.ADVANCE_WEATHER;
import static net.minecraft.world.level.gamerules.GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER;
import static net.minecraft.world.level.gamerules.GameRules.RAIDS;
import static net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS;
*///?}

import dev.flatgen.mc.GameRuleAccess;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;

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
        GameRuleAccess.Changed changed = GameRuleAccess.changed(GameRuleAccess.defaultRules(), Map.of(
                "doDaylightCycle", "false", "doWeatherCycle", "false", "doMobSpawning", "false", "randomTickSpeed", "7",
                "disableRaids", "true", "doFireTick", "false"));
        Map<String, String> rules = GameRuleAccess.values(changed.rules());
        check("false".equals(rules.get("doDaylightCycle")), "the daylight cycle is still on");
        check("false".equals(rules.get("doWeatherCycle")), "the weather cycle is still on");
        check("false".equals(rules.get("doMobSpawning")), "mob spawning is still on");
        check("7".equals(rules.get("randomTickSpeed")), "the random tick speed is not 7");
        check("true".equals(rules.get("disableRaids")), "raids are still on");
        check("false".equals(rules.get("doFireTick")), "fire still spreads");
        check(changed.left().isEmpty(), "left " + changed.left());
        //? if >=1.21.11 {
        /*checkTheGamesOwn(changed);
        *///?}
    }

    //? if >=1.21.11 {
    /*/^*
     * The game's own rules after the option format's names were set, so a mistake the names would read back the same
     * shows: they were renamed, raids' switch turned round and fire spread became a radius, and a rule goes by its id.
     ^/
    private static void checkTheGamesOwn(GameRuleAccess.Changed changed) {
        var rules = changed.rules();
        check(!rules.get(ADVANCE_TIME), "the game's time still advances");
        check(!rules.get(ADVANCE_WEATHER), "the game's weather still advances");
        check(!rules.get(SPAWN_MOBS), "the game still spawns mobs");
        check(!rules.get(RAIDS), "the game still has raids");
        check(rules.get(FIRE_SPREAD_RADIUS_AROUND_PLAYER) == 0, "fire still spreads in the game's rules");
        Map<String, String> byId = GameRuleAccess.values(rules);
        check("false".equals(byId.get("minecraft:advance_time")) && "false".equals(byId.get("advance_time")),
                "the time's rule does not go by its ids");
    }
    *///?}

    private static void leftAsTheyWere(GameTestHelper helper) {
        Map<String, String> before = GameRuleAccess.values(GameRuleAccess.defaultRules());
        GameRuleAccess.Changed changed = GameRuleAccess.changed(GameRuleAccess.defaultRules(), Map.of(
                "doWeatherCycle", "maybe", "randomTickSpeed", "fast", "flatgenNoSuchRule", "true"));
        Map<String, String> rules = GameRuleAccess.values(changed.rules());
        check("true".equals(rules.get("doWeatherCycle")), "the weather cycle took \"maybe\"");
        check(before.get("randomTickSpeed").equals(rules.get("randomTickSpeed")),
                "the random tick speed took \"fast\"");
        check(changed.left().stream().sorted().toList().equals(List.of("doWeatherCycle", "flatgenNoSuchRule",
                "randomTickSpeed")), "left " + changed.left());
    }

    private static void defaults(GameTestHelper helper) {
        Map<String, String> defaults = GameRuleAccess.defaults(List.of("doDaylightCycle", "flatgenNoSuchRule"));
        check(defaults.equals(Map.of("doDaylightCycle", "true")), "the defaults are " + defaults);
    }
}
