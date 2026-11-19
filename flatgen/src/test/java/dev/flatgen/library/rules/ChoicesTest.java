package dev.flatgen.library.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The player's favourites, recent presets and default. */
class ChoicesTest {
    @Test
    void keepsOneDefaultAtMost() {
        Choices tall = Choices.NONE.toggleDefault("tall.json");
        assertEquals(Optional.of("tall.json"), tall.defaultFile());
        assertEquals(Optional.of("water.json"), tall.toggleDefault("water.json").defaultFile());
        assertEquals(Optional.empty(), tall.toggleDefault("tall.json").defaultFile());
    }

    @Test
    void followsAPresetRenamedAndForgetsOneDeleted() {
        Choices chosen = Choices.NONE.toggleFavourite("tall.json").used("tall.json").used("water.json")
                .toggleDefault("tall.json");
        Choices renamed = chosen.renamed("tall.json", "towering.json");
        assertEquals(List.of("towering.json"), renamed.picks().starred());
        assertEquals(List.of("water.json", "towering.json"), renamed.picks().recent());
        assertEquals(Optional.of("towering.json"), renamed.defaultFile());
        Choices deleted = renamed.deleted("towering.json");
        assertEquals(Choices.NONE.used("water.json"), deleted);
    }

    @Test
    void appliesTheDefaultOnlyOverTheGamesOwnDefaultFlatWorld() {
        Preset classic = TestPresets.TALL_FLAT.withLayers(List.of(new Layer("minecraft:bedrock", 1),
                new Layer("minecraft:dirt", 2), new Layer("minecraft:grass_block", 1)));
        assertEquals(true, Choices.mayApplyDefault(classic.withName("").withIcon("minecraft:grass_block"), classic));
        assertEquals(false, Choices.mayApplyDefault(classic.withBiome("minecraft:desert"), classic));
        assertEquals(false, Choices.mayApplyDefault(classic.withDecorations(true, false), classic));
    }

    @Test
    void readsTheLibraryFileAPackShipsAsPackAuthorsMdShowsIt() {
        Choices shipped = Choices.fromJson("""
                {
                  "default": "pack:skyislands:start",
                  "start_on_superflat": true,
                  "built_ins": "no_game",
                  "starred": ["pack:skyislands:start"],
                  "pack_version": 3
                }
                """);
        assertEquals(Optional.of("pack:skyislands:start"), shipped.defaultFile());
        assertEquals(true, shipped.startOnSuperflat());
        assertEquals(PresetLibrary.BuiltIns.NO_GAME, shipped.builtIns());
        assertEquals(List.of("pack:skyislands:start"), shipped.picks().starred());
        assertEquals(PresetLibrary.BuiltIns.SHOWN, Choices.fromJson("{\"built_ins\": \"some\"}").builtIns());
    }

    @Test
    void readsWhatItWritesAndAFileWithoutADefault() {
        Choices chosen = Choices.NONE.toggleFavourite("tall.json").toggleDefault("water.json")
                .toggleStartOnSuperflat().nextBuiltIns();
        assertEquals(PresetLibrary.BuiltIns.NO_GAME, chosen.builtIns());
        assertEquals(chosen, Choices.fromJson(chosen.toJson()));
        assertEquals(true, chosen.startOnSuperflat());
        assertEquals(Choices.NONE.used("tall.json"), Choices.fromJson("{\"recent\": [\"tall.json\"]}"));
    }
}
