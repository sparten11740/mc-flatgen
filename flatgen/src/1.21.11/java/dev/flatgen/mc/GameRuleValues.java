package dev.flatgen.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * This version's game rules for {@link GameRuleAccess}. 26.1 keeps them in a registry under new ids; the option
 * format's 1.21.1 names are turned into them as the game's own update of old worlds turns them, so a rule goes by its
 * old name, its id ({@code minecraft:advance_time}) and its short id ({@code advance_time}) alike.
 */
final class GameRuleValues {
    /** The rules renamed since 1.21.1, by their old names, as the game's update of old worlds renames them. */
    private static final Map<String, Renamed> RENAMED = renamed();

    private GameRuleValues() {
    }

    /** How an old rule's value turns into its new one's. */
    private enum Conversion {
        /** The same value. */
        SAME,
        /** A switch turned round: true under the old name, as "disableRaids", is false under the new, "raids". */
        INVERTED,
        /** "doFireTick" as the radius fire spreads in: off is none, on the new rule's default. */
        FIRE_SPREAD;

        /** The new rule's value for the old name's. */
        String toNew(String old, GameRule<?> rule) {
            return switch (this) {
                case SAME -> old;
                case INVERTED -> inverted(old);
                case FIRE_SPREAD -> old.equals("false") ? "0" : defaultOf(rule);
            };
        }

        /** The old name's value for the new rule's. */
        String toOld(String value) {
            return switch (this) {
                case SAME -> value;
                case INVERTED -> inverted(value);
                case FIRE_SPREAD -> Boolean.toString(!value.equals("0"));
            };
        }

        /** Whether the old name takes a switch's values whatever the new rule takes. */
        boolean switchesOnly() {
            return this != SAME;
        }
    }

    /**
     * A renamed rule's new id and how its value turns.
     *
     * @param id         its id, as {@code minecraft:raids}
     * @param conversion how its old name's value turns into its own
     */
    private record Renamed(String id, Conversion conversion) {
    }

    /** These game rules, by every name each goes by, their values as the game writes them under that name. */
    static Map<String, String> of(GameRules rules) {
        Map<String, String> byName = new LinkedHashMap<>();
        for (GameRule<?> rule : rules.availableRules().toList()) {
            String value = rules.getAsString(rule);
            byName.put(rule.getIdentifier().toString(), value);
            byName.put(rule.id(), value);
        }
        RENAMED.forEach((old, renamed) -> Optional.ofNullable(byName.get(renamed.id()))
                .ifPresent(value -> byName.put(old, renamed.conversion().toOld(value))));
        return byName;
    }

    /** The game rules a new world starts with. */
    static GameRules defaults() {
        return new GameRules(FeatureFlags.DEFAULT_FLAGS);
    }

    /** These game rules with some set by name, their values as the game writes them; the others as they were. */
    static GameRuleAccess.Changed changed(GameRules current, Map<String, String> rules) {
        GameRules changed = current.copy(features(current));
        List<String> left = new ArrayList<>();
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            if (!setByName(changed, rule.getKey(), rule.getValue())) {
                left.add(rule.getKey());
            }
        }
        return new GameRuleAccess.Changed(changed, left);
    }

    /** The features these game rules' rules need together, which give a copy the same rules and no others. */
    private static FeatureFlagSet features(GameRules rules) {
        return rules.availableRules().map(GameRule::requiredFeatures).reduce(FeatureFlagSet.of(), FeatureFlagSet::join);
    }

    /** Sets a rule by any name it goes by; whether this version has it and it takes the value. */
    private static boolean setByName(GameRules rules, String name, String value) {
        Renamed renamed = RENAMED.getOrDefault(name, new Renamed(name, Conversion.SAME));
        Optional<GameRule<?>> rule = rules.availableRules()
                .filter(known -> known.getIdentifier().toString().equals(renamed.id()) || known.id().equals(name))
                .findFirst();
        if (rule.isEmpty()) {
            return false;
        }
        boolean isSwitch = renamed.conversion().switchesOnly() || rule.get().valueClass() == Boolean.class;
        boolean takes = (isSwitch || rule.get().valueClass() == Integer.class) && GameRuleAccess.takes(isSwitch, value);
        return takes && setValue(rules, rule.get(), renamed.conversion().toNew(value, rule.get()));
    }

    private static <T> boolean setValue(GameRules rules, GameRule<T> rule, String value) {
        Optional<T> parsed = rule.deserialize(value).result();
        parsed.ifPresent(found -> rules.set(rule, found, null));
        return parsed.isPresent();
    }

    private static <T> String defaultOf(GameRule<T> rule) {
        return rule.serialize(rule.defaultValue());
    }

    private static String inverted(String value) {
        return value.equals("true") ? "false" : value.equals("false") ? "true" : value;
    }

    private static Map<String, Renamed> renamed() {
        Map<String, Renamed> renamed = new HashMap<>();
        String[][] names = {
            {"allowEnteringNetherUsingPortals", "allow_entering_nether_using_portals"},
            {"announceAdvancements", "show_advancement_messages"},
            {"blockExplosionDropDecay", "block_explosion_drop_decay"},
            {"commandBlockOutput", "command_block_output"},
            {"enableCommandBlocks", "command_blocks_work"},
            {"commandBlocksEnabled", "command_blocks_work"},
            {"commandModificationBlockLimit", "max_block_modifications"},
            {"doDaylightCycle", "advance_time"},
            {"doEntityDrops", "entity_drops"},
            {"doImmediateRespawn", "immediate_respawn"},
            {"doInsomnia", "spawn_phantoms"},
            {"doLimitedCrafting", "limited_crafting"},
            {"doMobLoot", "mob_drops"},
            {"doMobSpawning", "spawn_mobs"},
            {"doPatrolSpawning", "spawn_patrols"},
            {"doTileDrops", "block_drops"},
            {"doTraderSpawning", "spawn_wandering_traders"},
            {"doVinesSpread", "spread_vines"},
            {"doWardenSpawning", "spawn_wardens"},
            {"doWeatherCycle", "advance_weather"},
            {"drowningDamage", "drowning_damage"},
            {"enderPearlsVanishOnDeath", "ender_pearls_vanish_on_death"},
            {"fallDamage", "fall_damage"},
            {"fireDamage", "fire_damage"},
            {"forgiveDeadPlayers", "forgive_dead_players"},
            {"freezeDamage", "freeze_damage"},
            {"globalSoundEvents", "global_sound_events"},
            {"keepInventory", "keep_inventory"},
            {"lavaSourceConversion", "lava_source_conversion"},
            {"locatorBar", "locator_bar"},
            {"logAdminCommands", "log_admin_commands"},
            {"maxCommandChainLength", "max_command_sequence_length"},
            {"maxCommandForkCount", "max_command_forks"},
            {"maxEntityCramming", "max_entity_cramming"},
            {"minecartMaxSpeed", "max_minecart_speed"},
            {"mobExplosionDropDecay", "mob_explosion_drop_decay"},
            {"mobGriefing", "mob_griefing"},
            {"naturalRegeneration", "natural_health_regeneration"},
            {"playersNetherPortalCreativeDelay", "players_nether_portal_creative_delay"},
            {"playersNetherPortalDefaultDelay", "players_nether_portal_default_delay"},
            {"playersSleepingPercentage", "players_sleeping_percentage"},
            {"projectilesCanBreakBlocks", "projectiles_can_break_blocks"},
            {"randomTickSpeed", "random_tick_speed"},
            {"reducedDebugInfo", "reduced_debug_info"},
            {"sendCommandFeedback", "send_command_feedback"},
            {"showDeathMessages", "show_death_messages"},
            {"snowAccumulationHeight", "max_snow_accumulation_height"},
            {"spawnMonsters", "spawn_monsters"},
            {"spawnRadius", "respawn_radius"},
            {"spawnerBlocksEnabled", "spawner_blocks_work"},
            {"spectatorsGenerateChunks", "spectators_generate_chunks"},
            {"tntExplodes", "tnt_explodes"},
            {"tntExplosionDropDecay", "tnt_explosion_drop_decay"},
            {"universalAnger", "universal_anger"},
            {"waterSourceConversion", "water_source_conversion"},
        };
        for (String[] name : names) {
            renamed.put(name[0], new Renamed("minecraft:" + name[1], Conversion.SAME));
        }
        renamed.put("disableElytraMovementCheck", new Renamed("minecraft:elytra_movement_check", Conversion.INVERTED));
        renamed.put("disablePlayerMovementCheck", new Renamed("minecraft:player_movement_check", Conversion.INVERTED));
        renamed.put("disableRaids", new Renamed("minecraft:raids", Conversion.INVERTED));
        renamed.put("doFireTick", new Renamed("minecraft:fire_spread_radius_around_player", Conversion.FIRE_SPREAD));
        return renamed;
    }
}
