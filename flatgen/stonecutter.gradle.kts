plugins {
    id("dev.kikugie.stonecutter")
}

// The node the IDE shows and runs; switch with the "Set active project to ..." tasks, and back before committing.
stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    // `//? if fabric` and `//? if neoforge` in the sources, from the loader half of the node's name. Forge joins with
    // the 1.20.1 nodes (docs/backlog/24-port-1-20-1.md).
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")

    // Plain renames between Minecraft versions, as Blockpilot's are: each names the earliest release known to have the
    // new name and swaps it in for the nodes from that release on. src/main and src/1.21.1 keep the names of the node
    // they are checked in for (1.21.1), src/26.1 those of 26.1; rules run over all of them, so a new text must not
    // appear in the code the nodes share (docs/plan.md 3.2).
    fun rename(since: String, from: String, to: String) =
        replacements.string(current.parsed >= since) { replace(from, to) }

    rename("1.21.2", ".getMinBuildHeight()", ".getMinY()")
    rename("1.21.2", "MobSpawnType", "EntitySpawnReason")
    // With the type's context, so a local named location keeps its name.
    rename("1.21.11", "resources.ResourceLocation;", "resources.Identifier;")
    rename("1.21.11", "ResourceLocation ", "Identifier ")
    rename("1.21.11", "ResourceLocation.", "Identifier.")
    rename("1.21.11", "ResourceLocation,", "Identifier,")
    // A registry key's id: Flatgen's code calls only ResourceKey's location().
    rename("1.21.11", ".location()", ".identifier()")
    // 26.2 keeps the entity types' constants in EntityTypes, slimes and magma cubes in monster.cubemob, and a coloured
    // block's sixteen colours in one collection.
    rename("26.2", "EntityType.SLIME", "EntityTypes.SLIME")
    rename("26.2", "entity.monster.Slime;", "entity.monster.cubemob.Slime;")
    rename("26.2", "Blocks.BLACK_CONCRETE", "Blocks.CONCRETE.black()")
    rename("26.2", "Blocks.YELLOW_CONCRETE", "Blocks.CONCRETE.yellow()")
    // 26.3 names the server's structure templates for what they are.
    rename("26.3", "getStructureManager()", "getStructureTemplateManager()")
    rename("1.21.11", "world.level.GameRules;", "world.level.gamerules.GameRules;")
    rename("26.1", "import net.minecraft.Util;", "import net.minecraft.util.Util;")
    rename("26.1", "Minecraft.getInstance().tell(", "Minecraft.getInstance().schedule(")
    rename("26.1", ".getBlockHolder()", ".typeHolder()")
    // The game's GUI graphics became an extractor of what to draw in 26.1, its calls renamed with it.
    rename("26.1", "client.gui.GuiGraphics;", "client.gui.GuiGraphicsExtractor;")
    rename("26.1", "GuiGraphics graphics", "GuiGraphicsExtractor graphics")
    rename("26.1", "graphics.drawString(", "graphics.text(")
    rename("26.1", "graphics.renderOutline(", "graphics.outline(")
    rename("26.1", "graphics.renderItem(", "graphics.item(")
}
