pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = "flatgen-monorepo"

// The mod is built once per Minecraft version and loader from the one source tree: each node is named
// <minecraft>-<loader> and built by that loader's script. See flatgen/stonecutter.gradle.kts and docs/plan.md 3.2.
// The releases that add nodes are in docs/backlog/README.md.
stonecutter {
    create("flatgen") {
        fun target(minecraft: String, vararg loaders: String) = loaders.forEach { loader ->
            version("$minecraft-$loader", minecraft).buildscript = "build.$loader.gradle.kts"
        }

        target("1.21.1", "neoforge", "fabric")
        target("26.1", "neoforge", "fabric")
        target("26.2", "neoforge", "fabric")
        target("26.3", "neoforge", "fabric")

        vcsVersion = "1.21.1-neoforge"
    }
}
