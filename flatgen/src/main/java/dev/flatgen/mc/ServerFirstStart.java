package dev.flatgen.mc;

import dev.flatgen.Flatgen;
import dev.flatgen.options.rules.FirstStart;
import dev.flatgen.options.rules.PlaceAtSpawn;
import dev.flatgen.options.rules.Position;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** What first-start effects do to a world's overworld, as this version's server does it. */
public final class ServerFirstStart implements FirstStart {
    private final MinecraftServer server;

    public ServerFirstStart(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void setTime(long dayTime) {
        Overworld.setDayTime(server.overworld(), dayTime);
    }

    /** Places the template on the top layer at the spawn; a template the game lacks is logged and left out. */
    @Override
    public void placeAtSpawn(PlaceAtSpawn effect) {
        Optional<StructureTemplate> template = Optional.ofNullable(ResourceLocation.tryParse(effect.template()))
                .flatMap(server.getStructureManager()::get);
        if (template.isEmpty()) {
            Flatgen.LOGGER.warn("Flatgen found no structure template {} to place at the spawn", effect.template());
            return;
        }
        ServerLevel level = server.overworld();
        BlockPos spawn = Overworld.spawn(level);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, spawn.getX(), spawn.getZ());
        Vec3i size = template.get().getSize();
        PlaceAtSpawn.Placement placement = effect.place(new Position(size.getX(), size.getY(), size.getZ()),
                spawn.getX(), spawn.getZ(),
                surface > level.getMinBuildHeight() ? OptionalInt.of(surface) : OptionalInt.empty());
        BlockPos origin = blockPos(placement.origin());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.valueOf(effect.rotation().name()));
        template.get().placeInWorld(level, origin, origin, settings, level.getRandom(), Block.UPDATE_CLIENTS);
        placement.spawn().ifPresent(moved -> Overworld.moveSpawn(level, blockPos(moved)));
    }

    private static BlockPos blockPos(Position position) {
        return new BlockPos(position.x(), position.y(), position.z());
    }
}
