package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import com.google.gson.JsonParser;
import dev.flatgen.exports.PresetExports;
import dev.flatgen.mc.Exports;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.WorldTypes;
import dev.flatgen.options.OptionsAtDone;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/** Exports (F21) read back by the game: a server's generator settings, and a datapack's world preset. */
final class ExportGameTests {
    private static final Preset GLASS = new Preset("Glass floor", "minecraft:glass", List.of(
            new Layer("minecraft:bedrock", 1), new Layer("minecraft:stone", 3), new Layer("minecraft:glass", 1)),
            "minecraft:plains", Optional.of(List.of("minecraft:villages")), false, false, Map.of());

    private ExportGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("the server lines give the game the preset's layers", ExportGameTests::serverLines);
        tests.put("the datapack's world preset is the Default world type with the preset's overworld",
                ExportGameTests::worldPreset);
        return tests;
    }

    private static void serverLines(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        Properties read = new Properties();
        try {
            read.load(new StringReader(PresetExports.serverLines(GLASS, registries, List.of())));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        check("minecraft:flat".equals(read.getProperty("level-type")), "level-type " + read.getProperty("level-type"));
        FlatLevelGeneratorSettings settings = FlatSettings.orThrow(FlatLevelGeneratorSettings.CODEC.parse(
                FlatSettings.ops(registries), JsonParser.parseString(read.getProperty("generator-settings"))));
        check(layers(settings).equals(layers(OptionsAtDone.settings(GLASS, registries, List.of()))),
                "the server's layers are " + layers(settings));
    }

    private static void worldPreset(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        FlatLevelGeneratorSettings settings = OptionsAtDone.settings(GLASS, registries, List.of());
        WorldPreset preset = FlatSettings.orThrow(WorldPreset.DIRECT_CODEC.parse(FlatSettings.ops(registries),
                JsonParser.parseString(Exports.worldPreset(settings, Map.of(), registries))));
        Holder<WorldPreset> type = Holder.direct(preset);
        check(WorldTypes.editableWhole(type, registries)
                && WorldTypes.flatDimensions(type).equals(EnumSet.of(Dimension.OVERWORLD)),
                "the world preset is not the Default world type with a superflat overworld");
    }

    private static List<String> layers(FlatLevelGeneratorSettings settings) {
        return settings.getLayersInfo().stream().map(FlatLayerInfo::toString).toList();
    }
}
