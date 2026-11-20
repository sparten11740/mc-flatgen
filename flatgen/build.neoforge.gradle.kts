// Builds one NeoForge node of the Stonecutter tree (versions/<minecraft>-neoforge); see stonecutter.gradle.kts.
plugins {
    `java-library`
    id("net.neoforged.moddev")
}

// This build's own properties (Minecraft and loader versions); the tree's and the repository's are read in
// common.gradle.kts. Gradle 10 drops looking them up through parent projects implicitly.
fun prop(name: String) = property(name).toString()

val modId = parent!!.property("mod_id").toString()
val treeDir = parent!!.projectDir

// NeoForge's metadata, which common.gradle.kts generates from its template.
extra["metadataTemplates"] = listOf("META-INF/neoforge.mods.toml")
extra["loaderMetadata"] = mapOf(
    "loader_version_range" to prop("loader_version_range"),
    "neo_min_version" to prop("neo_min_version"),
    "minecraft_version_range" to prop("minecraft_version_range"),
)
apply(from = treeDir.resolve("common.gradle.kts"))

// Every jar lands in the root build/libs.
tasks.jar {
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

// The GameTests (src/gametest), in a source set of their own that never reaches the jar and is loaded as part of the
// mod in development: ./gradlew runGameTestServer runs them headless, and check runs that.
val gametest = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

// Blockpilot in the client run, for play tests, when asked for with -Pflatgen.blockpilot=<version>.
extra["blockpilotConfiguration"] = "runtimeClasspath"
apply(from = treeDir.resolve("blockpilot.gradle.kts"))

neoForge {
    version = prop("neo_version")

    // Parameter names for the obfuscated releases up to 1.21.x; from 26.1 on the game ships with its own.
    if (hasProperty("parchment_mappings_version")) {
        parchment {
            minecraftVersion = prop("parchment_minecraft_version")
            mappingsVersion = prop("parchment_mappings_version")
        }
    }

    runs {
        register("client") {
            client()
        }
        register("gameTestServer") {
            type = "gameTestServer"
            sourceSet = gametest
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            systemProperty("flatgen.played", extra["gameTestPlayedFile"].toString())
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(gametest)
        }
    }
}
neoForge.addModdingDependenciesTo(gametest)
tasks.named("check") {
    dependsOn("runGameTestServer")
}

neoForge.ideSyncTask(tasks.named("generateModMetadata"))

// Fabric's mixins stay out of this jar, and Fabric's GameTest mod out of the GameTests: NeoForge reaches the game
// through its events and finds the GameTests by their holder.
tasks.processResources {
    exclude("$modId-fabric.mixins.json")
}
tasks.named<ProcessResources>("processGametestResources") {
    exclude("fabric.mod.json")
}
