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

apply(from = treeDir.resolve("common.gradle.kts"))
// The mod's own metadata properties, which common.gradle.kts reads.
@Suppress("UNCHECKED_CAST")
val modMetadata = extra["modMetadata"] as Map<String, String>

// Every jar lands in the root build/libs.
tasks.jar {
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

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
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val properties = modMetadata + mapOf(
        "loader_version_range" to prop("loader_version_range"),
        "neo_version" to prop("neo_version"),
        "minecraft_version_range" to prop("minecraft_version_range"),
    )
    inputs.properties(properties)
    expand(properties)
    from(treeDir.resolve("src/main/templates")) {
        include("META-INF/neoforge.mods.toml")
    }
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets.main {
    resources.srcDir(generateModMetadata)
}
neoForge.ideSyncTask(generateModMetadata)
