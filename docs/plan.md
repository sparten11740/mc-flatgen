# Flatgen plan

Flatgen replaces Minecraft's superflat customisation screen (Create World → World → World Type: Superflat → Customize)
with a preset editor: a layer column edited directly, block and biome pickers with pictures, facts about the world
before it is created, presets with favourites and a default, and templates that generate presets from a few sliders.
The work itself is in `docs/backlog/`, one file per feature; this file is what holds across features.

## 1. Goals

1. **Show what the world will be like** before it is created: weather at the surface, slimes, hostile mobs, height.
2. **Know the modpack:** every block, biome and structure set comes from the game's registries, grouped by mod.
3. **Remember how the player likes it:** presets, favourites, a default applied on choosing Superflat, recents.
4. **Very good usability:** edit layers where they are drawn, undo everything, keyboard throughout.
5. **Usable without reading:** a picture for every control and fact.
6. **Worlds stay vanilla:** Flatgen only ever produces Minecraft's own flat generator settings and game rules, so a
   world opens without Flatgen.
7. **Many versions from one tree:** v1 1.21.1 (NeoForge, Fabric), v2 26.1 (NeoForge, Fabric), then 1.20.1 (Forge,
   Fabric) and 1.21.11, 26.2, 26.3 (NeoForge, Fabric), as Blockpilot builds them.

Not goals: several biomes in one flat world (needs a generator of Flatgen's own, breaking goal 6); editing the
Default, Amplified or other noise world types.

## 2. Decisions

1. **Own mod, not a fork of FlatEdit+.** FlatEdit+ on 1.21.1 is its oldest build, lacks favourites, defaults, biome
   facts and modded structures, needs BoxLib and is GPL-3.0; Flatgen is Apache-2.0 OR MIT like the owner's other mods.
2. **The Customize button is the only way in.** NeoForge's `RegisterPresetEditorsEvent` replaces the vanilla flat
   editor; Fabric has no such event, so a mixin at the head of `WorldCreationUiState#getPresetEditor` does it there.
3. **The Create World screen and its World tab are never changed.** Create Aeronautics hooks into the World tab's
   constructor and into world creation; replacing or re-laying out either would break it. Flatgen only adds listeners
   through public methods (F12) and its own screens.
4. **Client mod.** The jar is client-only on both loaders. The one server-side step, setting the clock on a new
   world's first start for "always day" (F10), runs on the integrated server inside the client.
5. **GUI behind a toolkit of Flatgen's own** (3.3), as the game's GUI drawing changed in 1.21.6 and 26.1 and its input
   events in 1.21.9.
6. **Java 17 in all shared code** from the start, as the 1.20.1 nodes compile it.
7. **Presets are files, choices are separate.** Each preset is a JSON file in `config/flatgen/presets/`; favourites,
   the default and recents are in `config/flatgen/library.json`, so preset files can be shared as they are.
8. **World options are data other mods can add** (F27). A switch in the editor that does something to the world (a
   game rule, structures, a template placed at spawn on the first start) is a JSON file in a mod's or pack's
   resources; Flatgen's own switches are written the same way. A Java API (F28) follows only once a mod needs code.
   Worlds still open without Flatgen, as every effect leaves vanilla settings or blocks behind.

## 3. Architecture

### 3.1 Packages and dependency direction

Everything is under `dev.flatgen`. Each feature has its own package (`config`, `editor`, `preset`, `layers`, `blocks`,
`biome`, `facts`, `structures`, `options`, `library`, `templates`), and logic several features share has one of its
own (`search` for the pickers' search, `json` for reading Flatgen's files), split the same way in each:

| Part | Holds | May depend on |
|---|---|---|
| `<feature>/rules/` | The feature's logic as plain Java: records, pure functions, edits. Same on every version. | Other features' `rules`, the JDK, Gson. Never Minecraft, never a loader. |
| `<feature>/` | Game-facing code that is the same on every version. | Its `rules`, other features' public types, `mc/`, Minecraft classes that do not change between the versions built. |
| `<feature>/client/` | The feature's widgets and screens. | Its feature, other features' public types, `client/gui/`, `mc/`. |
| `client/gui/` | Flatgen's GUI toolkit (3.3). | Itself, `mc/` for drawing and input, `rules`, the root. |
| `mc/` | The game's API that may differ between versions, behind Flatgen's own names (3.2). | `rules` types, the root, Minecraft. Never a feature's game-facing or client code. |
| `dev.flatgen` itself | `Flatgen`: what the whole mod names alike, such as its mod id. | Nothing of Flatgen's. Anything may depend on it but `rules`. |
| `platform/` | `ClientPlatform`, the interface shared code reaches loader hooks through, and its holder. | Nothing loader-specific. |
| `platform/<loader>/` | Entry points, event wiring, the loader's mixins. | Anything. Nothing depends on it. |

The direction is always towards `rules`: platform → client → feature → `mc` → rules. A test reads the sources, imports
and fully qualified names alike, and fails on a reference against this table (F00).

### 3.2 Versions and loaders

- Stonecutter, set up as in Blockpilot: each node is `<minecraft>-<loader>`, built by `build.<loader>.gradle.kts`, with
  its versions in `flatgen/versions/<node>/gradle.properties`. The checked-in node is `1.21.1-neoforge`.
- `src/main` is shared by every node. `src/<range>` (first `src/1.21.1`) is a version tree, serving its version up to
  the next tree's, for the classes that differ between versions. A class lives in `src/main` until a version built
  needs it to differ; then it moves into the trees, one copy per range, as each node reads one tree only. So that such
  a move stays small, the game's API that may change (world creation, drawing, input) is called from `mc/` and the
  GUI toolkit's base classes (3.3), not from features. Tree classes sit in the package they belong to and keep its
  dependency rules.
- A plain rename between versions is a rename rule in `stonecutter.gradle.kts`, naming the release that introduced it;
  a small difference is a `//? if` block; anything larger is a tree class.
- Loader code lives in `platform/<loader>/`; each loader's build excludes the others' folders. Shared code names no
  loader; `//? if neoforge` and friends appear only in build-adjacent code where a tree class would be heavier.

### 3.3 GUI toolkit

Flatgen's screens are built from widgets of its own that draw through `client/gui/Canvas` (fill, outline, text, item,
sprite, block texture, clipping) and receive Flatgen's own input records (`Pointer`, `Key`). Each version tree
implements `Canvas` over that version's `GuiGraphics` and provides `FlatgenScreen`, the one class extending
Minecraft's `Screen`, which turns the game's callbacks into Flatgen's input. Features' widgets therefore compile
unchanged on every version, and a GUI change in Minecraft is one tree class.

Shared widgets used by several features live once in `client/gui/`: `CardGrid` (biome picker, preset library,
templates), `ToggleList` (structures, world settings), `SearchField`, `IconButton`.

### 3.4 Extension points

Things that grow are lists of small classes behind an interface, so adding one never edits the others:

- `editor/EditorPanel`: the editor screen's panels (layers, biome, structures, world settings), listed in
  `editor/EditorPanels`.
- `library/PresetSource`: where presets come from (the player's folder, built-ins, pack resources).
- `templates/rules/Template`: each template.
- `options/rules/Effect`: each kind of world option effect (F27); the options themselves are data files that mods and
  packs add.
- `facts/rules/Fact`: each world fact.
- `editor/client/PresetEditors`: which world types Flatgen edits (a key, or a test on the world type from F18).

### 3.5 Storage

- Each feature that keeps a player's choices owns one file named after it, so a feature's data never grows another's
  class: `config/flatgen/flatgen.json` (the `config` feature: whether Flatgen is on), `library.json` (F12: favourites,
  the default, recents, start on Superflat), `blocks.json` and `biomes.json` (F05, F07: recent and starred).
- `config/flatgen/presets/*.json`: one preset per file, format of F02, written atomically.
- `config/flatgen/exports/`: datapacks exported by F21.

## 4. Testing

- **Unit tests** for every `rules` package: no game, fast, on every node's build.
- **GameTests** for what needs the game: settings conversions, generation of a preset's chunks, facts against the
  game's own rules. NeoForge runs them in `runGameTestServer`, Fabric in `runGameTest`.
- **Play tests** in a dev client with Blockpilot (its safety level at `all`, which lets it drive the Create World
  screen): open Customize, edit, apply, create the world, check it; screenshots of every screen.
- **Boot test** of each jar as players run it, before a release (F20).
- **The architecture test** of 3.1 and the translation key test of F19.

## 5. Working rules

The rules the code keeps and how changes are made are in `AGENTS.md`. In short: modularity, simplicity, consistency,
no open/closed violations, shared code over copies, the dependency direction of 3.1; atomic Conventional Commits, each
checked with `/diff-check` first; a full review by a separate agent after each milestone.
