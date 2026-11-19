// The Stonecutter tree as the tools see it: its nodes, each named <minecraft>-<loader> by its folder in
// flatgen/versions, with its versions in the gradle.properties there, beside the tree's own (the mod's id, name and
// version) in flatgen/gradle.properties. Shared by the tools so each reads them the one way.
import { existsSync, readdirSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

/** The repository's root, and the tree's folder in it. */
export const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
export const TREE = join(ROOT, "flatgen");

/** Every node, by name, in order: each folder in flatgen/versions with its gradle.properties, not a leftover. */
export function nodes() {
  return readdirSync(join(TREE, "versions")).filter((node) => existsSync(join(nodeDir(node), "gradle.properties")))
    .sort();
}

/** A node's folder, where its build and runs are. */
export function nodeDir(node) {
  return join(TREE, "versions", node);
}

/** A node's loader: the part of its name after the last dash. */
export function loaderOf(node) {
  return node.slice(node.lastIndexOf("-") + 1);
}

/** A node's gradle.properties, as a map. */
export function nodeProperties(node) {
  return readProperties(join(nodeDir(node), "gradle.properties"));
}

/** The tree's gradle.properties, as a map: the mod's id, name and version. */
export function treeProperties() {
  return readProperties(join(TREE, "gradle.properties"));
}

/** The jar a node's build leaves in build/libs, for the mod's version now. */
export function jarOf(node) {
  const tree = treeProperties();
  return join(ROOT, "build", "libs", `${tree.mod_id}-${tree.mod_version}+${node}.jar`);
}

/** A gradle.properties file as a map, comments left out. */
function readProperties(file) {
  const lines = readFileSync(file, "utf8").split("\n").filter((line) => line.includes("=") && !line.startsWith("#"));
  const pairs = lines.map((line) => [line.slice(0, line.indexOf("=")), line.slice(line.indexOf("=") + 1)]);
  return Object.fromEntries(pairs.map(([key, value]) => [key.trim(), value.trim()]));
}
