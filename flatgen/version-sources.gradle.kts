// Adds the version tree beside src/main to a node's main source set: src/<version_sources>/, the classes that differ
// between Minecraft versions (docs/plan.md 3.2). It is a source set of its own only so that it goes through
// Stonecutter like src/main, with `//? if` and the rename rules; its classes compile as part of main, as the two depend
// on each other. Applied by every build.
val sourceSets = the<SourceSetContainer>()
val tree = property("version_sources").toString()

val sources = sourceSets.create(tree)
sourceSets["main"].java.srcDir(sources.java.sourceDirectories)
sourceSets["main"].resources.srcDir(sources.resources.sourceDirectories)
tasks.named(sources.compileJavaTaskName) {
    enabled = false
}
// Switching the active node writes its sources back to src/, but a tree isn't on every node: switching to another
// tree's node and back would leave this tree as that node had it. Every build reads the conditions as checked in.
// The task's name capitalises the tree's.
tasks.matching { it.name == "stonecutterMerge${tree.replaceFirstChar(Char::uppercase)}" }.configureEach {
    enabled = false
}
