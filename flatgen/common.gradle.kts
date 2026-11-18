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
val loaders = listOf("fabric", "neoforge")
val otherLoaders = (loaders - loader).map { "dev/flatgen/platform/$it/**" }
val sourceSets = the<SourceSetContainer>()
sourceSets.named("main") {
    java.exclude(otherLoaders)
}
// What differs between Minecraft versions: one tree per range.
apply(from = treeDir.resolve("version-sources.gradle.kts"))

repositories {
    mavenCentral()
}

dependencies {
    "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    // The rules' JSON, read in the unit tests without the game: the Gson Minecraft 1.21.1 ships.
    "testImplementation"("com.google.code.gson:gson:2.10.1")
}

// The mod's own properties, which every loader's metadata template takes beside its loader's own.
extra["modMetadata"] = mapOf(
    "mod_id" to modId,
    "mod_name" to parent!!.property("mod_name").toString(),
    "mod_description" to parent!!.property("mod_description").toString(),
    "mod_version" to modVersion,
    "mod_license" to rootProject.property("mod_license").toString(),
)

tasks.named<Test>("test") {
    useJUnitPlatform()
    // The sources as checked in, which the tests about the code itself read (BoundariesTest): inputs of the task too,
    // so that changing them runs those tests again rather than taking a result from the build cache.
    systemProperty("flatgen.project", treeDir.path)
    inputs.files(fileTree(treeDir.resolve("src")) { include("**/*.java") })
        .withPropertyName("sources").withPathSensitivity(PathSensitivity.RELATIVE)
}
