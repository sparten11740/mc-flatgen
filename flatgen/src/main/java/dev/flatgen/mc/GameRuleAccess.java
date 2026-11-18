package dev.flatgen.mc;

import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.GameRules;

/**
 * The game rules of the world on the Create World screen, by the option format's names, which are Minecraft 1.21.1's
 * and so this version's own; a later version's tree turns them into its names here.
 */
public final class GameRuleAccess {
    /** A whole number as a game rule value is written. */
    private static final String WHOLE_NUMBER = "-?\\d{1,9}";

    private GameRuleAccess() {
    }

    /** The world's game rules now, by name, their values as the game writes them. */
    public static Map<String, String> values(CreateWorldScreen screen) {
        CompoundTag values = screen.getUiState().getGameRules().createTag();
        Map<String, String> byName = new LinkedHashMap<>();
        values.getAllKeys().forEach(name -> byName.put(name, values.getString(name)));
        return byName;
    }

    /** The game's own default values of these game rules, by name, of those this version has. */
    public static Map<String, String> defaults(Collection<String> names) {
        CompoundTag defaults = new GameRules().createTag();
        Map<String, String> values = new LinkedHashMap<>();
        names.stream().filter(defaults::contains).forEach(name -> values.put(name, defaults.getString(name)));
        return values;
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
        Rules known = Rules.of();
        CompoundTag values = current.createTag();
        List<String> left = new ArrayList<>();
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            if (known.takes(rule.getKey(), rule.getValue())) {
                values.putString(rule.getKey(), rule.getValue());
            } else {
                left.add(rule.getKey());
            }
        }
        return new Changed(new GameRules(new Dynamic<>(NbtOps.INSTANCE, values)), left);
    }

    /** This version's game rules by what they take: true or false, or a whole number. */
    private record Rules(Set<String> switches, Set<String> numbers) {
        static Rules of() {
            Rules rules = new Rules(new HashSet<>(), new HashSet<>());
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
                @Override
                public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key,
                        GameRules.Type<GameRules.BooleanValue> type) {
                    rules.switches().add(key.getId());
                }

                @Override
                public void visitInteger(GameRules.Key<GameRules.IntegerValue> key,
                        GameRules.Type<GameRules.IntegerValue> type) {
                    rules.numbers().add(key.getId());
                }
            });
            return rules;
        }

        boolean takes(String name, String value) {
            boolean isSwitch = value.equals("true") || value.equals("false");
            return switches.contains(name) ? isSwitch : numbers.contains(name) && value.matches(WHOLE_NUMBER);
        }
    }
}
