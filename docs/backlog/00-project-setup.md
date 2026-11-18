# F00 Project setup

Milestone: M0 foundation · Release: v1 · Status: doing · Depends on: nothing

## Why

Flatgen is built for many Minecraft versions and two or three loaders from one source tree, the way Blockpilot and
Glowline are. The layout has to make the next version cheap from the first commit, so the split between what is the
same everywhere and what differs is set up before any feature lands.

## What the player gets

Nothing to see yet: a jar per node that loads, does nothing and breaks nothing.

## Acceptance criteria

- [ ] Stonecutter monorepo as Blockpilot's: `settings.gradle.kts` lists the nodes, `flatgen/stonecutter.gradle.kts`
  holds the loader constants and rename rules, `flatgen/build.<loader>.gradle.kts` each loader's build,
  `flatgen/versions/<node>/gradle.properties` each node's versions. Active and checked-in node `1.21.1-neoforge`.
- [ ] Nodes `1.21.1-neoforge` and `1.21.1-fabric`. Adding a node is a line in `settings.gradle.kts`, a
  `gradle.properties` and, where the game changed, a version tree.
- [ ] `./gradlew build` builds both nodes and runs the unit tests; jars land in `build/libs` as
  `flatgen-<version>+<minecraft>-<loader>.jar`.
- [ ] The source layout of `docs/plan.md` 3: features in their own packages, pure rules in `rules/`, version trees in
  `src/<range>`, one loader each in `platform/<loader>`.
- [ ] A boundaries test that reads the sources and fails when a `rules/` package imports Minecraft or a loader, or
  code outside `platform/<loader>` names a loader's API.
- [ ] Java 17 syntax in all shared code, as the 1.20.1 nodes (v3) compile it.
- [ ] Licences (Apache-2.0 OR MIT), `.gitignore`, Gradle wrapper, `README.md` for players, `AGENTS.md` for agents.
- [ ] Both jars load in a dev client (`runClient`) without errors in the log.

## Design

- Copied from Blockpilot and Glowline: the plugin versions (ModDevGradle, Loom with loom-back-compat, Stonecutter
  0.9.8), `common.gradle.kts` for what every node does alike, `version-sources.gradle.kts` for the version tree, the
  metadata templates in `src/main/templates`, and the Gradle wrapper.
- Flatgen is a client mod: NeoForge's entry point is `@Mod(dist = Dist.CLIENT)` and the jar says
  `displayTest = "IGNORE_ALL_VERSION"`, Fabric's `fabric.mod.json` says `"environment": "client"`. The integrated
  server's hooks (F10) still run, as the integrated server runs inside the client.
- Blockpilot in the dev client comes with F01, GameTests with F02 and the boot tool with F20: each with the first
  feature that needs it, not empty here.

## Version notes

- 1.21.1: Java 21, Mojang names with Parchment parameter names on both loaders.
- 26.1 (v2): Java 25, the game ships unobfuscated, no Parchment; loom-back-compat picks the plain Loom.
- 1.20.1 (v3): Java 17 and Forge through ModDevGradle's legacyforge plugin.

## Tests

- `./gradlew build` on both nodes.
- The boundaries test.
- A dev client of each node starts with the jar and logs no error from Flatgen.

## Out of scope

CI on GitHub (comes with F20, run on request as in Glowline), publishing.
