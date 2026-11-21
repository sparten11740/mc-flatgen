# F20 Release v1

Milestone: M4 finish and release v1 · Release: v1 · Status: doing · Depends on: F00 to F19

## Why

v1 is done when players can install it: jars for `1.21.1-neoforge` and `1.21.1-fabric`, tried in a real pack, with a
page that says what Flatgen does.

## Acceptance criteria

- [ ] Every v1 feature done; the milestone reviews of M0 to M4 done and their findings fixed.
- [ ] Boot test: a dedicated server of each loader starts with the jar in its mods folder and is unaffected (client
  mod), and a client of each loader starts with it (`tools/boot.mjs`, as in Glowline), also on the oldest loader
  versions the metadata accepts (NeoForge 21.1.1, Fabric Loader 0.16.14).
- [ ] Played in the test instance (NeoForge, about 105 mods) with Create Aeronautics, Hydrol and TerraBlender present:
  Customize, editing, presets, a world created and loaded. Copying the jar into the instance is asked for first.
- [x] GitHub Actions: build and release workflows as in Glowline, run on request.
- [ ] README with screenshots; the CurseForge page (Modrinth not for now); changelog from the `feat` and `fix` commit
  subjects.
- [ ] Version `1.0.0`, tag `v1.0.0`.

## Design

- `tools/boot.mjs` (with `tools/nodes.mjs`, the tree as the tools see it) boots each node's jar on real dedicated
  servers of its loader, downloaded from the loaders' Maven repositories and kept in `build/boot/`: on the version the
  node is built with and on the oldest its metadata accepts. Flatgen is a client mod, so a boot holds when the server
  lists the jar, logs no error naming the mod, starts and stops cleanly. A client of each loader starting with the
  release jar is not part of it yet: the dev clients' play tests run the mod as built, not as remapped and shipped.
- `.github/workflows/build.yml` runs on request and for release tags: the pictures as `tools/sprites.py` writes them,
  `./gradlew build` with its tests and GameTests, then the boot of each jar. `release.yml` runs on a tag `v<version>`
  matching `mod_version`, writes the notes from the `feat` and `fix` subjects since the last tag, and makes a GitHub
  release with the jars; CurseForge uploads join once the project is made, Modrinth not for now.

## Tests

- `./gradlew build` on both nodes, boot test, Blockpilot play tests of the main flows on both nodes.

## Tried

Locally, 2026-11-19: `node tools/boot.mjs` booted the Fabric jar on Fabric Loader 0.20.1 and 0.16.14 and the NeoForge
jar on NeoForge 21.1.248 and 21.1.1, each listed, without errors naming Flatgen, up and stopped cleanly; the first boot
on 21.1.1, right after installing that server, did not exit after stopping and was cut off, and every boot since (three,
one of them with no mods) exited at once. The workflows have not run: they run on GitHub, which waits for the owner.

2026-11-19, later: the README has screenshots of the editor, the biome picker, the library and the templates;
`CHANGELOG.md` and `docs/release/store-page.md` draft 1.0.0's changelog and the Modrinth and CurseForge text, nothing
published. `0.1.0-alpha.2` (`chore(release)`) is the build for the test instance, its jars booting on all four servers
(sha256: NeoForge `2c69bf3e…4185f`, Fabric `e2360bd6…77b8`); the owner approved copying it into the instance, which
waits for the share to be mounted.

Later that evening: `0.1.0-alpha.2` was never copied, as the instance has Blockpilot 1.1.1 and NeoForge would have
refused to start beside it (fixed by letting an older Blockpilot stand); `0.1.0-alpha.3`'s NeoForge jar is in the
instance's mods folder with the owner's yes (sha256 `055b140f…89ad5`), waiting to be played there.

2026-11-20, afternoon: alpha.9 to alpha.12 were played in the instance. What play found was fixed in between: the block
picker's tabs, emptied by Astral Sorcery's tab code (alpha.10), its layout with all blocks and rows of recent and
starred (alpha.11), and lag on every Flatgen screen, which 1.21.1's tiled drawing of nine-sliced sprites caused
(alpha.12); with alpha.12 the screens were found smooth.

## Review of 2026-11-21

A full review before the release, by two agents with fresh contexts given the code and the standards: one of the code
(open/closed, modularity, dependency direction, duplication, consistency, dead code), one of every screen from
screenshots and the code (accessibility, user experience, pictures). No blocker; the boundaries of plan.md 3.1 hold
everywhere. Fixed the same night, each in its own commit: the focus ring, the New layer row's edge and the green
buttons' contrast; struck pictures no longer dimmed; missing parts shown as the game's missing texture; a card's
controls reached in the order they stand, the focus selecting the card's layer, Alt with an arrow moving it, and Copy's
preview under the focus; one stepper for cards and templates; one picture for each meaning (the clipboard, a copy, the
datapack, the grass block for the Overworld alone, Flatgen's own icon); the star hint naming Shift+Enter; one palette
for the dimensions' colours; heights summed once; one way to name a picture; PresetAtDone and the structures' own memory
of Generate Structures; the game's editor as a panel; shared click and key handling, badge looks, Every shelves and
export names; dead code gone (CycleButton, text-only buttons, methods only tests used, unused pictures); the drawing
tools' shared PNG writer; and the templates' preview showing a thin top.

Left for the owner, as they would change screens the owner designed or words in all ten languages:

- No slimes stands among the facts at a fact's size and reads as one; it could be a button with words on a row of its
  own, its picture the slime face with the up arrow.
- A chosen tab (the dimensions, the structures' ways, the pickers' shelves) looks much like a hovered one; the chosen
  one could take the blue ring the cards use.
- Make flat and Let the game make it are hard to read at 16 pixels, alike but for which way up; each dimension's own
  block and a 32 pixel picture in the empty state and the question would help.
- Biomes at 16 pixels are mostly sky: the ground and tree alone at that size, and bamboo for the bamboo jungle, would
  tell them apart.
- The two pickers differ: a block is picked with one click, a biome with two; the biome picker rings the biome now and
  has pictured Recent and Starred shelves, the block picker marks neither and heads its rows with words.
- Presets and Templates leave the header on the Nether and End tabs; the templates have both Back and Cancel; the mod
  filter steps one mod a click without a tooltip; giving the Nether back asks first though Undo brings it back; block
  names are cut at one line in the picker.
- One quantity has three names for players (height, depth, deeper and shallower) where the code says thickness, and the
  library's code says favourite where the screens say starred; changing the words means all ten languages.
