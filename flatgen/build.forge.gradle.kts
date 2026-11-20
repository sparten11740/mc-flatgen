// Builds the Forge node of the Stonecutter tree (versions/1.20.1-forge) with ModDevGradle's plugin for Forge before
// NeoForge split off; see stonecutter.gradle.kts. Its jar also loads on NeoForge 47.1, which reports itself as Forge.
plugins {
    `java-library`
    id("net.neoforged.moddev.legacyforge")
}

// This build's own properties (Minecraft and loader versions); the tree's and the repository's are read in
// common.gradle.kts. Gradle 10 drops looking them up through parent projects implicitly.
fun prop(name: String) = property(name).toString()

val modId = parent!!.property("mod_id").toString()
val treeDir = parent!!.projectDir

// Forge's metadata, which common.gradle.kts generates from its templates: the mod's, and the pack.mcmeta without which
// Forge 1.20.1 serves none of the mod's resources.
extra["metadataTemplates"] = listOf("META-INF/mods.toml", "pack.mcmeta")
extra["loaderMetadata"] = mapOf(
    "loader_version_range" to prop("loader_version_range"),
    "forge_min_version" to prop("forge_min_version"),
    "minecraft_version_range" to prop("minecraft_version_range"),
    "pack_format" to prop("pack_format"),
)
apply(from = treeDir.resolve("common.gradle.kts"))

// The GameTests (src/gametest), in a source set of their own that never reaches the jar and is loaded as part of the
// mod in development: ./gradlew runGameTestServer runs them headless, and check runs that.
val gametest = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

// Blockpilot in the client run, for play tests, when asked for with -Pflatgen.blockpilot=<version>: remapped from its
// intermediate names to the development ones, as the plugin's mod configurations do.
extra["blockpilotConfiguration"] = "modRuntimeOnly"
apply(from = treeDir.resolve("blockpilot.gradle.kts"))

// ./gradlew :flatgen:1.20.1-forge:runClient -Pflatgen.neoforge=1.20.1-47.1.106 builds and runs the node on that
// NeoForge 47.1 instead, which loads Forge 1.20.1 mods: a check that Flatgen works there too.
val neoForge = providers.gradleProperty("flatgen.neoforge").orNull

legacyForge {
    if (neoForge != null) {
        enable { neoForgeVersion = neoForge }
    } else {
        version = "${prop("minecraft_version")}-${prop("forge_version")}"
    }

    parchment {
        minecraftVersion = prop("parchment_minecraft_version")
        mappingsVersion = prop("parchment_mappings_version")
    }

    runs {
        register("client") {
            client()
        }
        register("gameTestServer") {
            type = "gameTestServer"
            sourceSet = gametest
            systemProperty("forge.enabledGameTestNamespaces", modId)
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
legacyForge.addModdingDependenciesTo(gametest)
tasks.named("check") {
    dependsOn("runGameTestServer")
}

legacyForge.ideSyncTask(tasks.named("generateModMetadata"))

// Forge runs mods with the game's intermediate names, so the shared mixin needs a refmap from development names to
// those, which Mixin finds by the name its config gives (added to the shared config below), and Forge 1.20.1 finds a
// mod's mixin configs in its manifest.
mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId.mixins.json")
}
dependencies {
    annotationProcessor("org.spongepowered:mixin:${prop("mixin_version")}:processor")
}

// Fabric's mixins stay out of this jar, and Fabric's GameTest mod out of the GameTests; the shared mixin config names
// the refmap, which only this build writes.
tasks.processResources {
    exclude("$modId-fabric.mixins.json")
    filesMatching("$modId.mixins.json") {
        val refmap = "\"refmap\": \"$modId.refmap.json\","
        filter { line -> line.replace("\"required\": true,", "\"required\": true,\n  $refmap") }
    }
}
tasks.named<ProcessResources>("processGametestResources") {
    exclude("fabric.mod.json")
}

// The jar a player installs is the one with the game's intermediate names, in the root build/libs beside every other
// node's; the development one stays in this node's build/libs.
tasks.named<Jar>("jar") {
    archiveClassifier = "dev"
    manifest.attributes("MixinConfigs" to "$modId.mixins.json")
}
tasks.named<Jar>("reobfJar") {
    archiveClassifier = ""
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}
