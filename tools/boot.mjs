#!/usr/bin/env node
// Boots each built jar on a real dedicated server of its loader, as players run it, and says whether the server still
// starts and stops with it: faults of remapping, mixin configs and metadata show only in a jar as it ships
// (docs/backlog/20-release-v1.md). Flatgen is a client mod, so a server must load its jar and otherwise be
// unaffected. Each node's jar in build/libs boots on the loader version its node is built with and on the oldest one
// its metadata accepts (neo_min_version, forge_min_version, fabric_loader_min_version), 1.20.1's Forge jar on
// NeoForge 47.1 too (boot_neoforge_version), and a Fabric jar accepting older game releases than its own on the oldest
// (boot_minecraft_version). A boot holds when the server lists Flatgen among its mods, logs no error naming it,
// finishes starting and stops cleanly when told to.
//
//   node tools/boot.mjs [node...]
//   node tools/boot.mjs --matrix
//
// With no node named, every node boots. --matrix prints the nodes and their Java versions as a GitHub Actions matrix,
// for CI to boot each in a job of its own. Servers are installed under build/boot/<boot>/ and kept for the next run;
// each boot starts with a fresh world and mods folder, and leaves its log in build/boot/<boot>/logs/latest.log. Java
// comes from JAVA_HOME_<version>_* (as GitHub's setup-java sets them), Gradle's toolchains in ~/.gradle/jdks, or
// macOS's java_home. The exit code is 0 when every boot held and 1 when one failed.
import { spawn, spawnSync } from "node:child_process";
import { once } from "node:events";
import { closeSync, copyFileSync, existsSync, mkdirSync, openSync, readdirSync, readFileSync, rmSync, writeFileSync }
  from "node:fs";
import { homedir } from "node:os";
import { basename, dirname, join } from "node:path";
import { createInterface } from "node:readline";
import { jarOf, loaderOf, nodeProperties, nodes as allNodes, ROOT, treeProperties } from "./nodes.mjs";

const BOOTS = join(ROOT, "build", "boot");
// How long a server may take to start, the first time with the game and its libraries to fetch, in seconds.
const BOOT_SECONDS = 600;
// The Fabric installer whose server launcher Fabric boots start from.
const FABRIC_INSTALLER = "1.1.2";
// The argument file an installed Forge or NeoForge server starts from, by system.
const ARGS_FILE = process.platform === "win32" ? "win_args.txt" : "unix_args.txt";
// The Maven repositories the Forge and NeoForge installers come from.
const NEOFORGED_MAVEN = "https://maven.neoforged.net/releases";
const FORGE_MAVEN = "https://maven.minecraftforge.net";
// A line of a server's log that names the mod as an error: what a broken jar leaves.
const MOD_ERROR = new RegExp(`(ERROR|FATAL|Exception).*\\b${treeProperties().mod_id}\\b`, "i");
// A line of a server's log that lists the mod as loaded: Fabric's "- flatgen 1.0.0+..." and NeoForge's mod file line.
const MOD_LISTED = new RegExp(`(- ${treeProperties().mod_id} \\d|${treeProperties().mod_id}-[\\w.+-]+\\.jar)`);

/**
 * A Forge or NeoForge server: its installer, a Maven artifact at the path and version given, and the argument file the
 * installer writes to start from.
 */
function installedServer(maven, path, version) {
  const artifact = path.slice(path.lastIndexOf("/") + 1);
  const argsFile = `libraries/${path}/${version}/${ARGS_FILE}`;
  return {
    installer: `${maven}/${path}/${version}/${artifact}-${version}-installer.jar`,
    argsFile,
    launch: () => [`@${argsFile}`, "nogui"],
  };
}

/** A Fabric server of this game version on this loader version; Flatgen needs no Fabric API. */
function fabricServer(minecraft, loader) {
  return {
    launcher: `https://meta.fabricmc.net/v2/versions/loader/${minecraft}/${loader}/${FABRIC_INSTALLER}/server/jar`,
    launch: () => ["-jar", "fabric-server-launch.jar", "nogui"],
  };
}

// Each loader's servers a node's jar boots on, by the version each is named for: the loader version it is built with
// and the oldest it accepts, for 1.20.1's Forge jar NeoForge 47.1 too, which still called itself Forge, and for a
// Fabric jar accepting older game releases than its own the oldest of them.
const SERVERS = {
  neoforge: (node) => [node.neo_version, node.neo_min_version]
    .map((version) => ({ version, server: installedServer(NEOFORGED_MAVEN, "net/neoforged/neoforge", version) })),
  forge: (node) => [node.forge_version, node.forge_min_version]
    .map((version) => ({ version, server: forgeServer(FORGE_MAVEN, "net/minecraftforge/forge", node, version) }))
    .concat([{ version: `neoforge-${node.boot_neoforge_version}`,
      server: forgeServer(NEOFORGED_MAVEN, "net/neoforged/forge", node, node.boot_neoforge_version) }]),
  fabric: (node) => [node.fabric_loader_version, node.fabric_loader_min_version]
    .map((version) => ({ version, server: fabricServer(node.minecraft_version, version) }))
    .concat(node.boot_minecraft_version ? [{ version: `minecraft-${node.boot_minecraft_version}`,
      server: fabricServer(node.boot_minecraft_version, node.fabric_loader_version) }] : []),
};

/** A server of Forge, or of NeoForge before it renamed itself, versioned by the game's version and its own. */
function forgeServer(maven, path, node, version) {
  return installedServer(maven, path, `${node.minecraft_version}-${version}`);
}

/** The boots of one node, one a server, each server once where the oldest version accepted is the one built with. */
function bootsOf(name) {
  const node = nodeProperties(name);
  const boots = SERVERS[loaderOf(name)](node).map(({ version, server }) => ({ name: `${name}-on-${version}`,
    java: node.java_version, jar: jarOf(name), server }));
  return boots.filter((boot, index) => boots.findIndex((other) => other.name === boot.name) === index);
}

// --- Java ------------------------------------------------------------------------------------------------------------

/** The java binaries under a folder, a few levels down, as JDKs lay them out (bin/java, Contents/Home/bin/java). */
function javasUnder(folder, depth = 4) {
  const binary = join(folder, "bin", "java");
  if (existsSync(binary)) {
    return [binary];
  }
  if (depth === 0 || !existsSync(folder)) {
    return [];
  }
  return readdirSync(folder, { withFileTypes: true }).filter((entry) => entry.isDirectory())
    .flatMap((entry) => javasUnder(join(folder, entry.name), depth - 1));
}

/** The major version a java binary runs, read from its own report. */
function majorVersion(java) {
  const report = spawnSync(java, ["-XshowSettings:properties", "-version"], { encoding: "utf8" }).stderr ?? "";
  return report.match(/java\.specification\.version = (\d+)/)?.[1];
}

/** A java binary of the major version given, or nothing. */
function javaFor(version) {
  const fromCi = Object.entries(process.env).filter(([key]) => key.startsWith(`JAVA_HOME_${version}_`))
    .map(([, home]) => home);
  const fromMac = process.platform === "darwin"
    ? [spawnSync("/usr/libexec/java_home", ["-v", version], { encoding: "utf8" }).stdout.trim()]
    : [];
  const homes = [...fromCi, join(homedir(), ".gradle", "jdks"), ...fromMac, process.env.JAVA_HOME].filter(Boolean);
  return homes.flatMap((home) => javasUnder(home)).find((java) => majorVersion(java) === version);
}

// --- Installing ------------------------------------------------------------------------------------------------------

/** Downloads a file, once: kept under build/boot/downloads, named by its whole address, for the next run. */
async function download(url) {
  const file = join(BOOTS, "downloads", new URL(url).pathname.slice(1).replace(/[^\w.+-]/g, "_"));
  if (existsSync(file)) {
    return file;
  }
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`could not download ${url}: ${response.status}`);
  }
  mkdirSync(dirname(file), { recursive: true });
  writeFileSync(file, Buffer.from(await response.arrayBuffer()));
  return file;
}

/** Installs a boot's server into its folder, unless it is there from an earlier run. */
async function install(boot, folder, java) {
  mkdirSync(folder, { recursive: true });
  if (boot.server.launcher) {
    copyFileSync(await download(boot.server.launcher), join(folder, "fabric-server-launch.jar"));
    return;
  }
  // Written once the installer has finished, so a server cut off part-way through installing is installed afresh.
  const installed = join(folder, "installed.ok");
  if (existsSync(installed)) {
    return;
  }
  const installer = await download(boot.server.installer);
  // The installer reports every file it patches, far more than a pipe's buffer holds, so it reports to a file.
  const log = join(folder, "installer.log");
  const output = openSync(log, "w");
  const done = spawnSync(java, ["-jar", installer, "--installServer", folder],
    { cwd: folder, stdio: ["ignore", output, output] });
  closeSync(output);
  if (done.status !== 0 || !existsSync(join(folder, boot.server.argsFile))) {
    throw new Error(`the installer failed, exit ${done.status}: see ${log}`);
  }
  writeFileSync(installed, `${boot.server.installer}\n`);
}

/** A fresh world and mods folder for a boot, with Flatgen's jar alone in it. */
function prepare(boot, folder) {
  for (const stale of ["mods", "world", "logs"]) {
    rmSync(join(folder, stale), { recursive: true, force: true });
  }
  mkdirSync(join(folder, "mods"));
  copyFileSync(boot.jar, join(folder, "mods", basename(boot.jar)));
  writeFileSync(join(folder, "eula.txt"), "eula=true\n");
  // A small flat world on any free port, so boots start quickly and beside a running game.
  writeFileSync(join(folder, "server.properties"), ["online-mode=false", "server-port=0", "level-type=minecraft\\:flat",
    "generate-structures=false", "spawn-protection=0", "view-distance=4", "simulation-distance=4", ""].join("\n"));
}

// --- Booting ---------------------------------------------------------------------------------------------------------

/** Notes what a server's log line says: the mod listed, an error naming it, or the server up, told to stop then. */
function note(line, seen, server) {
  seen.listed ||= MOD_LISTED.test(line);
  if (MOD_ERROR.test(line)) {
    seen.errors.push(line.trim());
  }
  if (!seen.up && /Done \(\d/.test(line)) {
    seen.up = true;
    server.stdin.write("stop\n");
  }
}

/** Starts a boot's server, stops it once it is up, and says what it saw. */
async function run(boot, folder, java) {
  const seen = { listed: false, errors: [], up: false };
  const server = spawn(java, boot.server.launch(), { cwd: folder, stdio: ["pipe", "pipe", "pipe"] });
  const timer = setTimeout(() => server.kill("SIGKILL"), BOOT_SECONDS * 1000);
  createInterface({ input: server.stdout }).on("line", (line) => note(line, seen, server));
  createInterface({ input: server.stderr }).on("line", (line) => note(line, seen, server));
  const [exit] = await once(server, "exit");
  clearTimeout(timer);
  return { ...seen, exit };
}

/** Whether the server's debug log lists the mod: Forge names the mods it found there only. */
function listedInDebugLog(folder) {
  const log = join(folder, "logs", "debug.log");
  return existsSync(log) && MOD_LISTED.test(readFileSync(log, "utf8"));
}

/** Boots one jar on one server; whether it held. */
async function boot(item) {
  const folder = join(BOOTS, item.name);
  const java = javaFor(item.java);
  if (!java) {
    console.log(`  FAIL ${item.name}: no Java ${item.java} found`);
    return false;
  }
  if (!existsSync(item.jar)) {
    console.log(`  FAIL ${item.name}: no jar at ${item.jar}; run ./gradlew assemble`);
    return false;
  }
  await install(item, folder, java);
  prepare(item, folder);
  const seen = await run(item, folder, java);
  seen.listed ||= listedInDebugLog(folder);
  const held = seen.listed && seen.errors.length === 0 && seen.up && seen.exit === 0;
  const what = `${seen.listed ? "listed" : "not listed"}, ${seen.errors.length} errors naming it, `
    + `${seen.up ? "up" : "never up"}, exit ${seen.exit ?? "none"}`;
  console.log(`  ${held ? "ok  " : "FAIL"} ${item.name}: ${what}`);
  seen.errors.slice(0, 3).forEach((error) => console.log(`       ${error}`));
  return held;
}

/** The nodes and the Java each runs on, as a GitHub Actions matrix. */
function matrix(nodes) {
  const include = nodes.map((node) => ({ node, java: nodeProperties(node).java_version }));
  return JSON.stringify({ include });
}

async function main() {
  const named = process.argv.slice(2).filter((arg) => !arg.startsWith("--"));
  const nodes = named.length ? named : allNodes();
  if (process.argv.includes("--matrix")) {
    console.log(matrix(nodes));
    return 0;
  }
  const failed = [];
  for (const item of nodes.flatMap(bootsOf)) {
    try {
      if (!(await boot(item))) {
        failed.push(item.name);
      }
    } catch (error) {
      console.log(`  FAIL ${item.name}: ${error.message}`);
      failed.push(item.name);
    }
  }
  console.log(failed.length === 0
    ? "\nEvery jar booted."
    : `\n${failed.length} failed (logs in build/boot/<boot>/logs):\n  ${failed.join("\n  ")}`);
  return failed.length === 0 ? 0 : 1;
}

process.exit(await main());
