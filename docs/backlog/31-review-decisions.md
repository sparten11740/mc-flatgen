# F31 What the release review left open

Milestone: after v1 · Release: 1.1.0 · Status: doing · Depends on: F20

## Why

The review before 1.0.0 (F20) found seven things it left for the owner, as fixing them would change screens the owner
designed or words in all ten languages. The owner decided them on 2026-11-22, walking through each with screenshots of
today's screens; two were drawn on the design canvas first (round 5: "The chosen one, four ways" and "Small biome
pictures, four ways").

## Acceptance criteria

- [x] No slimes leaves the facts row: a button row of its own under the facts, its picture a slime with the red "no"
  sign Always clear has (first an up arrow, changed by the owner on trying it) and the words "No slimes", shown while
  slimes spawn in slime chunks at the surface.
- [x] A chosen tab, shelf or way is drawn in a blue fill with light blue words (the canvas's C), hover staying a grey
  fill: the world's tabs, the structures' ways and both pickers' shelves.
- [x] Make flat and Let the game make it get two pictures unlike each other, in each dimension's own blocks: a neat
  stack of flat layers, and the dimension's own rough ground (netherrack crags with lava, an End stone island); 32
  pixels in the empty state, 16 on the button.
- [x] Small biome pictures (16 pixels: the shelves, the Biome button, the recent and starred rows) are close-ups (the
  canvas's A): the horizon high and the biome's feature big, such as a tree, a cactus, a spruce, a peak, lava, an End
  island, or bamboo for the bamboo jungle. The picker's big pictures stay whole landscapes.
- [x] One click picks in both pickers. In the biome picker the details follow the pointer and the keyboard's focus,
  showing the biome picked otherwise; its Pick button goes.
- [x] The block picker has pictured Starred and Recent shelves at the top of its list, as the biome picker has, and
  rings the block the layer has now.
- [x] On the Nether and End tabs, Presets and Templates stay in the header, greyed out, with a tooltip saying they are
  for the Overworld, so the header no longer jumps.
- [x] Let the game make it acts at once, without asking first; Undo brings the layers back, as for every other edit.
- [x] The templates keep their Back arrow and lose Cancel, as the presets library has it.
- [x] The mod filter opens a menu of All mods and the mods that add blocks or biomes, each with its icon and name,
  instead of stepping to the next mod on each click.
- [x] A layer's size is its thickness in every language: one block thicker or thinner, the thickness of the layer,
  "Thickness: 3".
- [x] Starring is starring everywhere: the library's "Use this favourite" says starred, the library's code says starred
  where it says favourite, and the README stars favourites.
- [ ] Kept as it is, by the owner's choice: block names in the picker stay one line, the whole name in the tooltip.
- [x] Copy's preview takes a row of its own below the card, the cards below moving down to make room, rather than
  being drawn over the card above; the copy goes there, below the layer, which stays chosen, so the card under the
  mouse never moves (the owner's, on trying 1.1.0-alpha.2).
- [x] The library renames the picked preset as its name is edited, with no Rename button: once the player is done
  typing (Enter, Tab or a click elsewhere) or leaves the library; a built-in's name shows again as it is (the
  owner's, on trying the library).

## Design

Each change goes where its screen's code is; the chosen look is one style the tabs, the ways and the shelves share
(`client/gui/`), so they cannot drift apart again. The pictures are drawn by `tools/sprites.py` and
`tools/modern_look.py` like the others. The words change in all ten language files at once, translated as F30's were.

## Tests

`./gradlew build` on every node; the picker and language tests for the one-click picking, the shelves and the words; a
dev client of each loader for every screen changed, with Blockpilot where it reaches the screen.

## Tried

- No slimes, in dev clients of 1.21.1 NeoForge, 1.20.1 Forge and 26.3 Fabric driven by Blockpilot: on Classic Flat
  its row shows under the facts; pressing it raises the surface to Y 40, the row goes and the slimes fact is struck
  through; Undo brings both back; a flat Nether has no row. It is a panel of its own (`facts/client/NoSlimesPanel`), so
  the editor lays the aside out again when slimes come or go.
- Copy's row, in a 1.21.1 NeoForge dev client with Advanced on: the mouse on Dirt's Copy leaves Dirt where it is and
  moves Bedrock down a row, the faint Dirt in the place between; a click puts the copy there, Dirt staying chosen; the
  mouse away closes the row. Walking a card's controls with the arrows, the row opens on Copy and closes on Remove,
  the focus staying where it is.
- Each change, in a 1.21.1 Fabric dev client driven by Blockpilot, from screenshots and the editor's queries: the blue
  chosen tab and shelf filling their bar with the hover grey; Presets and Templates greyed in place on the Nether;
  Let the game make it emptying the Nether at once and Undo bringing its two layers back; the dimensions' new pictures
  at 32 and 16 pixels; the small biome pictures as close-ups, unclipped; one click picking Desert, the panel following
  the mouse and the keyboard; the block picker's Starred and Recent shelves and Grass Block ringed; the mod menu,
  scrolling when longer than the screen (tried with its rows repeated); the templates without Cancel; "Thickness" in
  the narration; the library renaming on Enter and on a click elsewhere, a built-in's name showing again.
- The release review's fixes, there too: digits typed in a thickness field kept with the mouse over a Copy; no drag
  from a card pressed while a copy's row is open; the last card's copy shown below the list; in a list of nine layers
  scrolled down, the card under the mouse staying put after Copy.
- Every node (1.20.1 Forge and Fabric; 1.21.1, 1.21.11, 26.1, 26.2 and 26.3 NeoForge and Fabric), by one Blockpilot
  script each: the No slimes row, Copy's row moving Bedrock down, the header greyed on the Nether, Let the game make it
  and Undo, the biome picker without Pick and picking in one click, the block picker's shelves, the mod menu, the
  templates without Cancel and the library's inline rename all as above, the logs without errors from Flatgen. The
  owner tried the screens in 1.21.1 NeoForge dev clients along the way. Not tried in game: the mod menu with more than
  one mod that adds blocks or biomes, in a real modpack.
