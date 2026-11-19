package dev.flatgen.platform.fabric;

import dev.flatgen.Flatgen;
import dev.flatgen.gametest.FlatgenTests;
import net.minecraft.gametest.framework.GameTestHelper;
//? if <1.21.5 {
import net.minecraft.gametest.framework.GameTest;
//?} else
/*import net.fabricmc.fabric.api.gametest.v1.GameTest;*/

/**
 * Flatgen's GameTests on Fabric API's GameTest server, the {@code fabric-gametest} entry point of the GameTest mod's
 * fabric.mod.json, standing in the empty site.
 */
public final class FlatgenGameTests {
    //? if <1.21.5 {
    @GameTest(template = Flatgen.MOD_ID + ":" + FlatgenTests.SITE, timeoutTicks = FlatgenTests.MAX_TICKS)
    //?} else
    /*@GameTest(structure = Flatgen.MOD_ID + ":" + FlatgenTests.SITE, maxTicks = FlatgenTests.MAX_TICKS)*/
    public void all(GameTestHelper helper) {
        FlatgenTests.all(helper);
    }
}
