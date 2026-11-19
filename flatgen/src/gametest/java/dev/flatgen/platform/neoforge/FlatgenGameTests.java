package dev.flatgen.platform.neoforge;

import dev.flatgen.Flatgen;
import dev.flatgen.gametest.FlatgenTests;
import net.minecraft.gametest.framework.GameTestHelper;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
*///?}

/**
 * Flatgen's GameTests on NeoForge's GameTest server, standing in the empty site. Up to 1.21.4 NeoForge finds them by
 * their holder; from 1.21.5, where a GameTest is a test function and a test instance in the game's registries, they
 * are registered as each.
 */
//? if <1.21.5 {
@GameTestHolder(Flatgen.MOD_ID)
@PrefixGameTestTemplate(false)
//?} else
/*@EventBusSubscriber(modid = Flatgen.MOD_ID)*/
public final class FlatgenGameTests {
    private FlatgenGameTests() {
    }

    //? if <1.21.5 {
    @GameTest(template = FlatgenTests.SITE, timeoutTicks = FlatgenTests.MAX_TICKS)
    public static void all(GameTestHelper helper) {
        FlatgenTests.all(helper);
    }
    //?} else {
    /*/^* The test's name, and its function's. ^/
    private static final ResourceLocation ALL_NAME = ResourceLocation.fromNamespaceAndPath(Flatgen.MOD_ID, "all");
    private static final ResourceKey<Consumer<GameTestHelper>> ALL =
            ResourceKey.create(Registries.TEST_FUNCTION, ALL_NAME);

    /^* Registers the test function. ^/
    @SubscribeEvent
    static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, functions -> functions.register(ALL_NAME, FlatgenTests::all));
    }

    /^* Registers the test, in an environment that changes nothing, on the empty site. ^/
    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(ResourceLocation.fromNamespaceAndPath(Flatgen.MOD_ID, "plain"));
        var site = ResourceLocation.fromNamespaceAndPath(Flatgen.MOD_ID, FlatgenTests.SITE);
        event.registerTest(ALL_NAME, data -> new FunctionGameTestInstance(ALL, data),
                new TestData<>(environment, site, FlatgenTests.MAX_TICKS, 0, true));
    }
    *///?}
}
