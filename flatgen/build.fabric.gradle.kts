// Builds one Fabric node of the Stonecutter tree (versions/<minecraft>-fabric); see stonecutter.gradle.kts.
plugins {
    `java-library`
    // Applies Fabric Loom: the remapping variant up to 1.21.x, the plain one for the unobfuscated 26.x releases.
    id("dev.kikugie.loom-back-compat")
}

// This build's own properties (Minecraft and loader versions); the tree's and the repository's are read in
// common.gradle.kts. Gradle 10 drops looking them up through parent projects implicitly.
fun prop(name: String) = property(name).toString()

val minecraft = prop("minecraft_version")
val treeDir = parent!!.projectDir

// Fabric's metadata, which common.gradle.kts generates from its template.
extra["metadataTemplate"] = "fabric.mod.json"
extra["loaderMetadata"] = mapOf(
    "fabric_loader_min_version" to prop("fabric_loader_min_version"),
    "minecraft_version_range" to prop("minecraft_version_range"),
    "java_version" to prop("java_version"),
)
apply(from = treeDir.resolve("common.gradle.kts"))

// Every jar lands in the root build/libs.
loomx.modJar {
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

repositories {
    maven("https://maven.parchmentmc.org") {
        content { includeGroup("org.parchmentmc.data") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    // Releases from 26.1 on ship with Mojang's names; older ones are mapped to them, with Parchment's parameter names.
    if (!project.loomx.isUnobfuscated) {
        mappings(loom.layered {
            officialMojangMappings()
            val parchment = "${prop("parchment_minecraft_version")}:${prop("parchment_mappings_version")}"
            parchment("org.parchmentmc.data:parchment-$parchment@zip")
        })
    }
    modImplementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
    // Flatgen does without Fabric API; the dev client runs it for Blockpilot, which needs it.
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")
}

// The GameTests (src/gametest), in a source set of their own that never reaches the jar, loaded in development as a mod
// of their own, flatgen-gametest (src/gametest/resources/fabric.mod.json): ./gradlew runGameTest runs them headless on
// Fabric API's GameTest server, and check runs that.
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "flatgen-gametest"
        enableClientGameTests = false
    }
}
// From 1.21.5 the GameTest annotation is Fabric API's own: a node marked `fabric_gametest_api=true` compiles its
// GameTests against that module, which the run has with the rest of Fabric API.
if (findProperty("fabric_gametest_api")?.toString() == "true") {
    dependencies {
        "gametestCompileOnly"(fabricApi.module("fabric-gametest-api-v1", prop("fabric_api_version")))
    }
}
loom.runs.named("gameTest") {
    property("flatgen.played", extra["gameTestPlayedFile"].toString())
}
tasks.named("check") {
    dependsOn("runGameTest")
}

// Blockpilot in the client run, for play tests, when asked for with -Pflatgen.blockpilot=<version>.
extra["blockpilotConfiguration"] = if (loomx.isUnobfuscated) "localRuntime" else "modLocalRuntime"
apply(from = treeDir.resolve("blockpilot.gradle.kts"))

// The dev client loads the GameTests' mod too, for its fixtures (a world option and its template), as NeoForge's
// client run does with the GameTests' source set registered to Flatgen's mod.
loom {
    runs {
        named("client") {
            runDirectory = layout.projectDirectory.dir("run")
            sourceSet = "gametest"
        }
    }
}
