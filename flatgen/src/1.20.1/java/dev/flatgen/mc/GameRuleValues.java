package dev.flatgen.mc;

import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.GameRules;

/** This version's game rules for {@link GameRuleAccess}, by their names, which are the option format's own. */
final class GameRuleValues {
    private GameRuleValues() {
    }

    /** These game rules, by name, their values as the game writes them. */
    static Map<String, String> of(GameRules rules) {
        CompoundTag values = rules.createTag();
        Map<String, String> byName = new LinkedHashMap<>();
        values.getAllKeys().forEach(name -> byName.put(name, values.getString(name)));
        return byName;
    }

    /** The game rules a new world starts with. */
    static GameRules defaults() {
        return new GameRules();
    }

    /** These game rules with some set by name, their values as the game writes them; the others as they were. */
    static GameRuleAccess.Changed changed(GameRules current, Map<String, String> rules) {
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
        return new GameRuleAccess.Changed(new GameRules(new Dynamic<>(NbtOps.INSTANCE, values)), left);
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
            return switches.contains(name) ? GameRuleAccess.takes(true, value)
                    : numbers.contains(name) && GameRuleAccess.takes(false, value);
        }
    }
}
