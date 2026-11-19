package dev.flatgen.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
//? if >=1.21.11 {
/*import net.minecraft.world.level.storage.LevelData;
*///?}

/**
 * The overworld's time of day and spawn, as this version keeps them: its day time up to 1.21.x and the clock it keeps
 * its days by from 26.1 on; its spawn and spawn angle up to 1.21.10 and its respawn data from 1.21.11 on.
 */
public final class Overworld {
    private Overworld() {
    }

    /** The overworld's time of day, in ticks since the world began. */
    public static long dayTime(ServerLevel level) {
        //? if >=26.1 {
        /*return level.getOverworldClockTime();
        *///?} else {
        return level.getDayTime();
        //?}
    }

    /** Sets the overworld's time of day, in ticks since the world began. */
    public static void setDayTime(ServerLevel level, long ticks) {
        //? if >=26.1 {
        /*level.dimensionType().defaultClock()
                .ifPresent(clock -> level.getServer().clockManager().setTotalTicks(clock, ticks));
        *///?} else {
        level.setDayTime(ticks);
        //?}
    }

    /** The world's spawn. */
    public static BlockPos spawn(ServerLevel level) {
        //? if >=1.21.11 {
        /*return level.getRespawnData().pos();
        *///?} else {
        return level.getSharedSpawnPos();
        //?}
    }

    /** Moves the world's spawn, keeping the way players face there. */
    public static void moveSpawn(ServerLevel level, BlockPos spawn) {
        //? if >=1.21.11 {
        /*LevelData.RespawnData old = level.getRespawnData();
        level.setRespawnData(LevelData.RespawnData.of(old.dimension(), spawn, old.yaw(), old.pitch()));
        *///?} else {
        level.setDefaultSpawnPos(spawn, level.getSharedSpawnAngle());
        //?}
    }
}
