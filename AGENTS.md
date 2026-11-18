# Flatgen: notes for coding agents

Flatgen is a Minecraft client mod that replaces the superflat customisation screen with a preset editor. One source
tree builds it with Stonecutter, the setup of Blockpilot (github.com/sparten11740/mc-blockpilot), for each node
`<minecraft>-<loader>`: v1 is `1.21.1-neoforge` and `1.21.1-fabric`. `docs/plan.md` is the plan and architecture;
`docs/backlog/` is the work, one file per feature, worked through in its index's order; `README.md` is for players.

## Build and run

- `./gradlew build` builds every node and runs its tests; `./gradlew :flatgen:<node>:build` does one, as
  `:flatgen:1.21.1-neoforge:build`. Jars land in `build/libs` as `flatgen-<version>+<minecraft>-<loader>.jar`.
- `./gradlew :flatgen:<node>:runClient` starts a dev client of that node. The active node, the one the sources are
  checked in for, is `1.21.1-neoforge`; switch it with the "Set active project to ..." tasks and switch back before
  committing.
- Java 21 for 1.21.1, 25 for 26.x, 17 for 1.20.1; Gradle's toolchains fetch them.

## Layout

- `settings.gradle.kts`: the nodes. `flatgen/stonecutter.gradle.kts`: loader constants and rename rules.
  `flatgen/build.<loader>.gradle.kts`: each loader's build; `flatgen/common.gradle.kts`: what every node does alike.
  `flatgen/versions/<node>/gradle.properties`: each node's versions.
- `flatgen/src/main`: shared by every node. `flatgen/src/<range>` (first `1.21.1`): the version tree, `mc/` and the
  classes that extend or mix into Minecraft's, serving its version up to the next tree's.
- Packages and the direction they may depend in: `docs/plan.md` 3.1. Each feature has its own package; its logic in
  `rules/` (plain Java, unit-tested), its game-facing code beside it, its screens in `client/`. Loader code only in
  `platform/<loader>/`.

## Rules the code keeps

- **Modularity.** A feature reaches another only through the other's public types; `rules/` never imports Minecraft
  or a loader; nothing outside `platform/<loader>/` names a loader's API. The architecture test enforces it.
- **Dependency direction.** platform → client → feature → `mc` → rules, never back (`docs/plan.md` 3.1).
- **Open/closed.** What grows (editor panels, preset sources, templates, facts, edited world types) is a list of
  classes behind an interface (`docs/plan.md` 3.4); adding one never edits the others or adds a branch on kinds.
- **Reuse.** Shared widgets and logic live once (`client/gui/`, `search/rules/`); a second copy of something is a sign
  to move it to where both can use it.
- **Simplicity and consistency.** The simplest code that does the job; the same problem solved the same way
  everywhere; names say what things are in the game.
- **Versions.** A plain rename is a rename rule naming the release that introduced it; a small difference a `//? if`;
  anything larger a version tree class. Shared code is Java 17: no pattern-matching `switch`, record patterns,
  `getFirst`, `reversed` or `Math.clamp`.
- **Worlds stay vanilla.** Flatgen only produces Minecraft's own flat generator settings and game rules.
- **The Create World screen and its World tab are never changed** (other mods hook into them); Flatgen's way in is the
  Customize button.
- **Players.** Some players cannot read yet: every control and fact has a picture, never text alone.
- **Saves.** Preset files from any earlier release still load: missing keys get defaults, unknown keys are ignored,
  unknown ids are kept and reported, never dropped.

## Code style

- Four-space indents, lines and comments wrapped at 120 columns.
- Every class has javadoc, and so does every member whose purpose is not obvious: plain English saying what the thing
  is and why. No comments narrating the next line.
- A tunable number is a named constant with its meaning and unit.
- Methods nest loops and ifs at most two deep; no multi-statement lambdas inside calls.

## Workflow

- **Commits** are atomic, one change each, with Conventional Commits subjects (`feat(layers): ...`,
  `fix(library): ...`, `docs(backlog): ...`, `build: ...`). The body says what changed for the player and why.
- **Before every commit**, run `/diff-check` on the uncommitted changes and fix what it finds first.
- **After every milestone** (M0, M1, ... in `docs/backlog/README.md`), a full review of the milestone's code by a
  separate agent with a fresh context, given the code and the standards rather than the author's conclusions; its
  findings are fixed before the next milestone starts.
- **A feature is done** when its acceptance criteria are ticked, `./gradlew build` passes on every node of its
  release, and it has been tried in a dev client of each node; say plainly what was not tried in game. Its status
  changes in its file and in the backlog index in the commit that finishes it.
- **Versions.** Semantic versions; a build handed out for testing gets its own `-alpha.N` in its own
  `chore(release)` commit.
