# F00 Project setup

Milestone: M0 foundation · Release: v1 · Status: done · Depends on: nothing

## Why

Flatgen is built for many Minecraft versions and two or three loaders from one source tree, the way Blockpilot and
Glowline are. The layout has to make the next version cheap from the first commit, so the split between what is the
same everywhere and what differs is set up before any feature lands.

## What the player gets

Nothing to see yet: a jar per node that loads, does nothing and breaks nothing.

## Acceptance criteria

- [x] Stonecutter monorepo as Blockpilot's: `settings.gradle.kts` lists the nodes, `flatgen/stonecutter.gradle.kts`
  holds the loader constants and rename rules, `flatgen/build.<loader>.gradle.kts` each loader's build,
  `flatgen/versions/<node>/gradle.properties` each node's versions. Active and checked-in node `1.21.1-neoforge`.
- [x] Nodes `1.21.1-neoforge` and `1.21.1-fabric`. Adding a node is a line in `settings.gradle.kts`, a
  `gradle.properties` and, where the game changed, a version tree.
- [x] `./gradlew build` builds both nodes and runs the unit tests; jars land in `build/libs` as
  `flatgen-<version>+<minecraft>-<loader>.jar`.
- [x] The source layout of `docs/plan.md` 3 set up and enforced by the boundaries test: one loader each in
  `platform/<loader>`, the version tree `src/1.21.1` read by every node (empty until a class differs between versions);
  the first feature packages, with their `rules/`, arrive with F01 and F02.
- [x] A boundaries test that reads the sources and fails when a `rules/` package imports Minecraft or a loader, or
  code outside `platform/<loader>` names a loader's API.
- [x] Java 17 in all shared code, as the 1.20.1 nodes (v3) compile it: the main compile runs at Java 17 language
  level (`-source 17`, classes still for the node's Java), so newer language features fail now; newer JDK methods
  (`getFirst`, `Math.clamp`, ...) are kept out by the rule in `AGENTS.md` until the 1.20.1 nodes compile them.
- [x] Licences (Apache-2.0 OR MIT), `.gitignore`, Gradle wrapper, `README.md` for players, `AGENTS.md` for agents.
- [x] Both jars load in a dev client (`runClient`) without errors in the log.

## Design

- Copied from Blockpilot and Glowline: the plugin versions (ModDevGradle, Loom with loom-back-compat, Stonecutter
  0.9.8), `common.gradle.kts` for what every node does alike, `version-sources.gradle.kts` for the version tree, the
  metadata templates in `src/main/templates`, and the Gradle wrapper.
- Flatgen is a client mod: NeoForge constructs its entry point on the client only (`@Mod(dist = Dist.CLIENT)`) and
  the jar says `displayTest = "IGNORE_ALL_VERSION"`; Fabric applies its mixins on the client only. Both jars load
  harmlessly on a dedicated server, where nothing of theirs runs, which the GameTest server relies on (F02). The
  integrated server's hooks (F27) still run, as the integrated server runs inside the client.
- Blockpilot in the dev client comes with F01, GameTests with F02 and the boot tool with F20: each with the first
  feature that needs it, not empty here.

## Version notes

- 1.21.1: Java 21, Mojang names with Parchment parameter names on both loaders.
- 26.1 (v2): Java 25, the game ships unobfuscated, no Parchment; loom-back-compat picks the plain Loom.
- 1.20.1 (v3): Java 17 and Forge through ModDevGradle's legacyforge plugin.

## Tests

- `./gradlew build` on both nodes.
- The boundaries test, seen to fail on a `rules` class importing Minecraft and a loader's platform half.
- The Java 17 level, seen to reject a pattern-matching `switch`.
- A dev client of each node starts with the jar and logs no error: NeoForge lists `mod/flatgen` among its resources,
  Fabric lists `flatgen 0.1.0-alpha.1` among its 55 mods; both reach the title screen.

## Out of scope

CI on GitHub (comes with F20, run on request as in Glowline), publishing.
