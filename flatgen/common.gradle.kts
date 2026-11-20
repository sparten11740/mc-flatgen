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
// folder its properties name (structures/ up to 1.20.x). Only a data pack's own structure folder moves; worldgen's
// keeps its name.
findProperty("structure_folder")?.toString()?.let { folder ->
    val structures = Regex("^(data/[^/]+/)structure/")
    tasks.matching { it.name == "processGametestResources" }.configureEach {
        inputs.property("structureFolder", folder)
        (this as ProcessResources).eachFile { path = structures.replace(path, "\$1$folder/") }
    }
}
// Flatgen's fonts on a release that draws TrueType fonts with stb_truetype (1.20.1) rather than FreeType, which sizes a
// font by its whole height rather than its em, sets its glyphs higher and leaves them between pixels: each definition
// is written larger by Inter's whole height in ems, each text style's glyphs lower by what was measured against
// 1.21.11's screens, rounded to whole window pixels (the definition's own oversample), and drawn this many times finer
// than the window's pixels so thin strokes do not fade. A definition of a style not measured fails the build.
val interHeightInEms = 1.2099609375
val stbShiftsDown = mapOf("body" to 1.5, "strong" to 1.5, "title" to 0.0)
val stbFiner = 2
if (findProperty("stb_truetype")?.toString() == "true") {
    tasks.named<ProcessResources>("processResources") {
        inputs.property("stbFonts", "$interHeightInEms $stbShiftsDown $stbFiner")
        doLast {
            destinationDir.resolve("assets/$modId/font").listFiles { file -> file.extension == "json" }.orEmpty()
                .forEach { file -> stbFont(file, interHeightInEms, stbShiftsDown, stbFiner) }
        }
    }
}

/** A font definition rewritten for stb_truetype, as described above. */
fun stbFont(file: File, heightInEms: Double, shiftsDown: Map<String, Double>, finer: Int) {
    val style = file.name.substringBefore("_")
    val down = shiftsDown[style] ?: throw GradleException("$file is of a text style not measured for stb_truetype")
    @Suppress("UNCHECKED_CAST")
    val definition = groovy.json.JsonSlurper().parse(file) as Map<String, Any>
    @Suppress("UNCHECKED_CAST")
    val fonts = (definition["providers"] as List<MutableMap<String, Any>>).filter { it["type"] == "ttf" }
    if (fonts.isEmpty()) {
        throw GradleException("$file has no TrueType font to rewrite for stb_truetype")
    }
    fonts.forEach { font ->
        val pixels = (font["oversample"] as Number).toInt()
        @Suppress("UNCHECKED_CAST")
        val shift = font["shift"] as? List<Number> ?: listOf(0, 0)
        font["size"] = (font["size"] as Number).toDouble() * heightInEms
        font["oversample"] = pixels * finer
        font["shift"] = listOf(shift[0], shift[1].toDouble() + Math.round(down * pixels).toDouble() / pixels)
    }
    file.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(definition)) + "\n")
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
