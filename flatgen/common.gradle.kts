// What every node's build does alike: its identity, Java, licence files, the loader halves it takes, and its tests.
// The loader's own build script applies this and keeps what only that loader needs.
val modId = parent!!.property("mod_id").toString()
val modVersion = parent!!.property("mod_version").toString()
val minecraft = property("minecraft_version").toString()
val loader = name.substringAfterLast('-')
val treeDir = parent!!.projectDir
val javaVersion = property("java_version").toString()

version = "$modVersion+$minecraft-$loader"
group = parent!!.property("mod_group_id").toString()

the<BasePluginExtension>().archivesName = modId

the<JavaPluginExtension>().toolchain.languageVersion =
    JavaLanguageVersion.of(javaVersion.toInt())

// The mod's code and its tests are written in Java 17, which the 1.20.1 nodes compile them with (docs/plan.md 2.6), so
// a newer language feature fails here already; the classes still target the node's own Java. -Xlint:-options silences
// javac's note that an older source level is compiled against a newer JDK's classes.
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = javaVersion
    options.compilerArgs.add("-Xlint:-options")
}

// Both licences, named so they can't clash with another jar's.
tasks.named<Jar>("jar") {
    from(rootProject.files("LICENSE-APACHE", "LICENSE-MIT")) { rename { "${it}_$modId" } }
}

// Each loader's half of dev/flatgen/platform: a node's build takes its own and leaves out the others' (their entry
// points, event wiring and mixins).
val loaders = listOf("fabric", "forge", "neoforge")
val otherLoaders = (loaders - loader).map { "dev/flatgen/platform/$it/**" }
val sourceSets = the<SourceSetContainer>()
sourceSets.named("main") {
    java.exclude(otherLoaders)
}
// The GameTests (src/gametest), whichever way the loader's build makes their source set: the other loaders' halves
// stay out of it too.
sourceSets.matching { it.name == "gametest" }.all {
    java.exclude(otherLoaders)
}
// What differs between Minecraft versions: one tree per range.
apply(from = treeDir.resolve("version-sources.gradle.kts"))
// The GameTests' structures, in the data folder the node's release reads them from: structure/ as from 1.21, or the
// folder its properties name (structures/ up to 1.20.x).
findProperty("structure_folder")?.toString()?.let { folder ->
    tasks.matching { it.name == "processGametestResources" }.configureEach {
        (this as ProcessResources).eachFile { path = path.replace("/structure/", "/$folder/") }
    }
}
// Flatgen's fonts as the node's release must be told them to draw them as the others do: a release that sizes a
// TrueType font by its whole height rather than its em, and sets its glyphs elsewhere, has them written larger and
// lower by what its properties give (1.20.1): one factor, and how far down each text style's glyphs go. Each shift is
// rounded to the glyphs' own pixels, as many to a GUI pixel as the definition oversamples, so letters stay on whole
// window pixels.
findProperty("ttf_size_factor")?.toString()?.toDouble()?.let { factor ->
    val shifts = property("ttf_shift_down").toString().split(",")
        .associate { it.substringBefore(":") to it.substringAfter(":").toDouble() }
    val oversample = property("ttf_oversample_factor").toString().toInt()
    val size = Regex("\"size\": ([0-9.]+)")
    val shift = Regex("\"oversample\": ([0-9]+), \"shift\": \\[0, 0]")
    tasks.named<ProcessResources>("processResources") {
        inputs.property("ttfSizeFactor", factor)
        inputs.property("ttfShiftDown", shifts.toString())
        inputs.property("ttfOversampleFactor", oversample)
        shifts.forEach { (style, down) ->
            filesMatching("assets/$modId/font/${style}_*.json") {
                filter { line -> size.replace(line) { "\"size\": ${it.groupValues[1].toDouble() * factor}" } }
                filter { line ->
                    shift.replace(line) {
                        val pixels = it.groupValues[1].toInt()
                        val shifted = Math.round(down * pixels).toDouble() / pixels
                        "\"oversample\": ${pixels * oversample}, \"shift\": [0, $shifted]"
                    }
                }
            }
        }
    }
}

// Where the GameTests leave word that they played, which each loader's GameTest run is told: a game that never started,
// or stopped as its mods loaded, ends its run without playing a test, and some loaders call that a success.
val played = layout.buildDirectory.file("gametest/played").get().asFile
extra["gameTestPlayedFile"] = played.path
tasks.matching { it.name == "runGameTestServer" || it.name == "runGameTest" }.configureEach {
    doFirst { played.delete() }
    doLast {
        if (!played.exists()) {
            throw GradleException("The GameTests never played: the game did not start, or stopped as mods loaded")
        }
    }
}

repositories {
    mavenCentral()
    // Blockpilot's API, from the jar attached to its GitHub release, so the build needs no local Maven repository.
    exclusiveContent {
        forRepository {
            ivy {
                name = "Blockpilot API"
                url = uri("https://github.com/sparten11740/mc-blockpilot/releases/download")
                patternLayout { artifact("v${parent!!.property("blockpilot_release")}/[module]-[revision].[ext]") }
                metadataSources { artifact() }
            }
        }
        filter { includeModule("dev.blockpilot", "blockpilot-api") }
    }
}

dependencies {
    // Only compiled against: Flatgen's Blockpilot commands load only when Blockpilot, which brings its API, is there.
    "compileOnly"("dev.blockpilot:blockpilot-api:${parent!!.property("blockpilot_api_version")}")
    "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    // What the unit tests use without the game: the Gson and SLF4J Minecraft 1.21.1 ships.
    "testImplementation"("com.google.code.gson:gson:2.10.1")
    "testImplementation"("org.slf4j:slf4j-api:2.0.9")
}

// The loader's metadata files, from their templates in src/main/templates with the mod's own properties and the
// loader's, which the loader's build sets before applying this script: the templates' paths
// (extra["metadataTemplates"]) and their properties (extra["loaderMetadata"]).
@Suppress("UNCHECKED_CAST")
val loaderMetadata = extra["loaderMetadata"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
val metadataTemplates = extra["metadataTemplates"] as List<String>
val modMetadata = mapOf(
    "mod_id" to modId,
    "mod_name" to parent!!.property("mod_name").toString(),
    "mod_description" to parent!!.property("mod_description").toString(),
    "mod_version" to modVersion,
    "mod_license" to rootProject.property("mod_license").toString(),
    "blockpilot_release" to parent!!.property("blockpilot_release").toString(),
) + loaderMetadata
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    inputs.properties(modMetadata)
    expand(modMetadata)
    from(treeDir.resolve("src/main/templates")) {
        include(metadataTemplates)
    }
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.named("main") {
    resources.srcDir(generateModMetadata)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // The sources as checked in, which the tests about the code itself read (BoundariesTest): inputs of the task too,
    // so that changing them runs those tests again rather than taking a result from the build cache.
    systemProperty("flatgen.project", treeDir.path)
    inputs.files(fileTree(treeDir.resolve("src")) { include("**/*.java") })
        .withPropertyName("sources").withPathSensitivity(PathSensitivity.RELATIVE)
}
