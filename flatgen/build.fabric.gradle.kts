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

apply(from = treeDir.resolve("common.gradle.kts"))
// The mod's own metadata properties, which common.gradle.kts reads.
@Suppress("UNCHECKED_CAST")
val modMetadata = extra["modMetadata"] as Map<String, String>

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
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")
}

loom {
    runs {
        named("client") {
            runDirectory = layout.projectDirectory.dir("run")
        }
    }
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val properties = modMetadata + mapOf(
        "fabric_loader_min_version" to prop("fabric_loader_min_version"),
        "fabric_api_version" to prop("fabric_api_version").substringBefore('+'),
        "minecraft_version_range" to prop("minecraft_version_range"),
        "java_version" to prop("java_version"),
    )
    inputs.properties(properties)
    expand(properties)
    from(treeDir.resolve("src/main/templates")) {
        include("fabric.mod.json")
    }
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets.main {
    resources.srcDir(generateModMetadata)
}
