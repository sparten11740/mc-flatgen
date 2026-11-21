package dev.flatgen.templates.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** What each template makes, at its defaults and its edges. */
class TemplatesTest {
    private static final World OVERWORLD = new World(-64, 384);
    /** A world as short as the Nether, in which every template must still fit. */
    private static final World SHORT = new World(0, 128);

    @Test
    void everyTemplateMakesAPresetThatFitsTheWorldAtItsDefaultsAndEdges() {
        for (World world : List.of(OVERWORLD, SHORT)) {
            Templates.ALL.forEach(template -> assertFitsAtDefaultsAndEdges(template, world));
        }
    }

    private static void assertFitsAtDefaultsAndEdges(Template template, World world) {
        Values defaults = Values.defaults(template.parameters());
        assertFits(template.make(defaults, world), world, template.name());
        for (Parameter parameter : template.parameters()) {
            if (parameter instanceof Parameter.Number number) {
                assertFits(template.make(defaults.with(number, number.min()), world), world, template.name());
                assertFits(template.make(defaults.with(number, number.max()), world), world, template.name());
            }
        }
    }

    @Test
    void theTallFlatsSurfaceIsWhereItIsSet() {
        Template tall = Templates.ALL.get(0);
        Preset made = tall.make(Values.defaults(tall.parameters()), OVERWORLD);
        assertEquals(40, OVERWORLD.floor() + made.height());
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:stone", 102),
                new Layer("minecraft:grass_block", 1)), made.layers());
    }

    @Test
    void theAirshipFieldHasNoStructuresAndClearWeather() {
        Template field = Templates.ALL.get(1);
        Preset made = field.make(Values.defaults(field.parameters()), OVERWORLD);
        assertEquals(Optional.of(List.of()), made.structureSets());
        assertEquals(Map.of("flatgen:always_clear", true), made.options());
    }

    @Test
    void testLayersAlternateTheTwoBlocks() {
        Template test = Templates.ALL.get(4);
        Preset made = test.make(Values.defaults(test.parameters()), OVERWORLD);
        assertEquals(9, made.layers().size());
        assertEquals("minecraft:white_concrete", made.layers().get(1).block());
        assertEquals("minecraft:black_concrete", made.layers().get(2).block());
    }

    private static void assertFits(Preset preset, World world, String template) {
        assertTrue(preset.height() <= world.height(), template + " is too tall: " + preset.height());
        assertTrue(preset.layers().stream().allMatch(layer -> layer.height() >= 1), template + " has an empty layer");
    }
}
