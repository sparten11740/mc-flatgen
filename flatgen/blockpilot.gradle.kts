// Blockpilot (github.com/sparten11740/mc-blockpilot) in the dev client, for play tests: ./gradlew runClient
// -Pflatgen.blockpilot=<version> puts Blockpilot's release jar for this node, from its GitHub releases, on the client's
// runtime classpath only, never in Flatgen's jar or its metadata. A node marked `blockpilot=true` in its
// gradle.properties has a Blockpilot release; others leave it out. Applied by every loader's build, after it sets
// extra["blockpilotConfiguration"].
val blockpilotVersion = providers.gradleProperty("flatgen.blockpilot").orNull
    ?.takeIf { findProperty("blockpilot")?.toString() == "true" }
val minecraft = property("minecraft_version").toString()
val loader = name.substringAfterLast('-')

if (blockpilotVersion != null) {
    repositories {
        exclusiveContent {
            forRepository {
                ivy {
                    name = "Blockpilot releases"
                    url = uri("https://github.com/sparten11740/mc-blockpilot/releases/download")
                    patternLayout { artifact("v[revision]/[module]-[revision]+[classifier].[ext]") }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("dev.blockpilot") }
        }
    }
    val blockpilot = "dev.blockpilot:blockpilot:$blockpilotVersion:$minecraft-$loader@jar"
    // Fabric remaps a mod to the development names, local to the runs, up to 1.21.x; NeoForge runs it as it is, from a
    // configuration of its own so it stays out of the published dependencies.
    val configuration = extra["blockpilotConfiguration"].toString()
    if (configuration == "runtimeClasspath") {
        val blockpilotJar = configurations.create("blockpilotJar") { isTransitive = false }
        dependencies { blockpilotJar(blockpilot) }
        configurations.named("runtimeClasspath") { extendsFrom(blockpilotJar) }
    } else {
        dependencies { add(configuration, blockpilot) }
    }
}
