package dev.flatgen.mc;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.GameRules;

/**
 * The game rules of the world on the Create World screen, by the option format's names, which are Minecraft 1.21.1's;
 * {@link GameRuleValues}, one per version tree, reads and sets them in this version's game rules.
 */
public final class GameRuleAccess {
    /** A whole number as a game rule value is written. */
    private static final String WHOLE_NUMBER = "-?\\d{1,9}";

    private GameRuleAccess() {
    }

    /** The world's game rules now, by name, their values as the game writes them. */
    public static Map<String, String> values(CreateWorldScreen screen) {
        return values(screen.getUiState().getGameRules());
    }

    /** These game rules, by name, their values as the game writes them. */
    public static Map<String, String> values(GameRules rules) {
        return GameRuleValues.of(rules);
    }

    /** The game's own default values of these game rules, by name, of those this version has. */
    public static Map<String, String> defaults(Collection<String> names) {
        Map<String, String> all = values(defaultRules());
        Map<String, String> values = new LinkedHashMap<>();
        names.stream().filter(all::containsKey).forEach(name -> values.put(name, all.get(name)));
        return values;
    }

    /** The game rules a new world starts with. */
    public static GameRules defaultRules() {
        return GameRuleValues.defaults();
    }

    /**
     * Gives the world these game rules, by name with their values as the game writes them ({@code false}, {@code 3}),
     * as the Create World screen's own game rules screen does.
     *
     * @return the names left as they were: rules this version lacks, or values they cannot take
     */
    public static List<String> set(CreateWorldScreen screen, Map<String, String> rules) {
        WorldCreationUiState state = screen.getUiState();
        Changed changed = changed(state.getGameRules(), rules);
        state.setGameRules(changed.rules());
        return changed.left();
    }

    /**
     * Game rules with some changed.
     *
     * @param rules the game rules
     * @param left  the names left as they were: rules this version lacks, or values they cannot take
     */
    public record Changed(GameRules rules, List<String> left) {
    }

    /** These game rules with some set by name, their values as the game writes them; the others as they were. */
    public static Changed changed(GameRules current, Map<String, String> rules) {
        return GameRuleValues.changed(current, rules);
    }

    /** Whether a value as the option format writes it suits a rule: true or false for a switch, else a whole number. */
    static boolean takes(boolean isSwitch, String value) {
        return isSwitch ? value.equals("true") || value.equals("false") : value.matches(WHOLE_NUMBER);
    }
}
