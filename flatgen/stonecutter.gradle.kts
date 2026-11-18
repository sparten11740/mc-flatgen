plugins {
    id("dev.kikugie.stonecutter")
}

// The node the IDE shows and runs; switch with the "Set active project to ..." tasks, and back before committing.
stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    // `//? if fabric` and `//? if neoforge` in the sources, from the loader half of the node's name. Forge joins with
    // the 1.20.1 nodes (docs/backlog/24-port-1-20-1.md).
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")

    // Plain renames between Minecraft versions go here as they are needed, as Blockpilot's do: each names the earliest
    // release with the new name and keeps its new text out of the code every node compiles (docs/plan.md 3.2).
}
