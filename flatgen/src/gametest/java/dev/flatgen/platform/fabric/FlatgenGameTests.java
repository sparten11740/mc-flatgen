package dev.flatgen.platform.fabric;

import dev.flatgen.Flatgen;
import dev.flatgen.gametest.FlatgenTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Flatgen's GameTests on Fabric API's GameTest server, the {@code fabric-gametest} entry point of the GameTest mod's
 * fabric.mod.json, standing in the empty site.
 */
public final class FlatgenGameTests {
    @GameTest(template = Flatgen.MOD_ID + ":" + FlatgenTests.SITE, timeoutTicks = FlatgenTests.MAX_TICKS)
    public void all(GameTestHelper helper) {
        FlatgenTests.all(helper);
    }
}
