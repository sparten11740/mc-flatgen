package dev.flatgen.platform.forge;

import dev.flatgen.Flatgen;
import dev.flatgen.gametest.FlatgenTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Flatgen's GameTests on Forge's GameTest server, which finds them by their holder, standing in the empty site. */
@GameTestHolder(Flatgen.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FlatgenGameTests {
    private FlatgenGameTests() {
    }

    @GameTest(template = FlatgenTests.SITE, timeoutTicks = FlatgenTests.MAX_TICKS)
    public static void all(GameTestHelper helper) {
        FlatgenTests.all(helper);
    }
}
