# Changelog

What changed for players in each release, in a few lines a player reads. A release's notes on CurseForge and GitHub
are its section here ([docs/release/releasing.md](docs/release/releasing.md)).

## 1.1.0 (2026-11-22)

What the review before 1.0.0 left open, as decided on today's screens, and what trying it in game asked for:

- **No slimes on a row of its own:** under the facts, a slime with the red "no" sign and the words "No slimes", there
  only while slimes can spawn at the surface; it raises the ground out of their reach.
- **Copy shows where the copy goes:** the copy takes a row of its own below the card, the cards below making room,
  and lands there.
- **The chosen one is blue:** the chosen dimension, shelf and structures' way have a blue fill across their bar,
  unlike the grey one under the mouse.
- **Pictures that tell things apart:** Make flat shows a neat stack and Let the game make it the dimension's own rough
  ground, each in that dimension's blocks; small biome pictures are close-ups, a tree, a cactus or a peak big; the
  desert gets its cactus, the bamboo jungle its bamboo and the Nether its lava.
- **One click picks a biome,** the panel showing the biome under the mouse or the keyboard; the block picker has
  Starred and Recent shelves as the biome picker has, and rings the layer's block.
- **The mod filter is a menu** of the mods, each with its picture, rather than a button stepping through them.
- **Rename by typing:** a preset's name in the library saves once you are done typing it, with no Rename button.
- **Steadier screens:** Presets and Templates stay in the header on the Nether and End, greyed out; Let the game make
  it acts at once, Undo bringing the layers back; the templates leave by their Back arrow alone; a click on a button
  right after typing in a field works with what was typed.
- **Words:** a layer's size is its thickness in every language, and starring is starring everywhere.

## 1.0.0 (2026-11-22)

Flatgen's first release, for Minecraft 1.20.1 on Forge and Fabric, and for 1.21.1, 1.21.11, 26.1 (26.1.2 on
NeoForge), 26.2 and 26.3 on NeoForge and Fabric. Create World → World Type: Superflat → Customize opens Flatgen in
place of the game's screen:

- **Layers as cards beside the world they make:** the world drawn at the left in proportion, the layers as cards
  with their block, how thick they are in squares, and + and −; New layer above the cards, or Insert, adds one from
  the block picker; drag a card by any part of it to move it, or a layer's edge in the world to make it deeper; undo
  and redo everything.
- **Simple, or Advanced:** Advanced, remembered between games, adds the Y levels, thicknesses to type, structures,
  the game's superflat code copied and pasted with the wrong part named, and the game's own editor.
- **Every block and biome, modded ones too,** as pictures you search, filter by mod and star; blocks sorted by the
  creative inventory's tabs, biomes by kind, each biome painted as a little landscape with what it does to the world
  in words; hints for layers that fall or slow the game.
- **Know the world before you make it:** the surface's height, rain, snow or sun there, whether slimes and monsters
  spawn, and lines on the column where snow starts and slimes stop.
- **Structures, decorations and lakes,** every structure set the game knows, modded ones too, marked where the biome
  will not have it.
- **World options:** always day, always clear and whether mobs spawn as picture switches, saved with the preset;
  mods and packs add their own as data files
  ([world options](https://github.com/sparten11740/mc-flatgen/blob/main/docs/world-options.md)).
- **Presets:** save your own, use the game's and Flatgen's built-in ones, each a card of its layers in the sky; search
  them, star favourites, pick a default that is ready the moment you choose Superflat, share a preset as text and
  paste one back, drop preset files on the screen.
- **Templates:** a tall flat world, an airship field, a water world, the void with a platform or test layers from a
  few settings, with what each makes drawn beside them as you change them.
- **Works without reading:** every control has a picture, colour is never the only sign, the screens work with the
  keyboard alone, and the game's narrator reads them.
- **Ten languages:** English, German, Simplified Chinese, Japanese, Korean, Russian, Brazilian Portuguese, Spanish,
  French and Polish.
- **Mods' flat world types,** such as an airship-ready type, open Flatgen too.
- **For modpacks:** ship presets and a default
  ([pack authors](https://github.com/sparten11740/mc-flatgen/blob/main/docs/pack-authors.md)).
- **A flat Nether and End:** tabs above the column give the Nether and the End layers and a biome of their own;
  portals, the Nether's fortresses and bastions, the dragon and the way home work as always, and the world opens
  without Flatgen.
- **Exports:** a preset as the lines a server's `server.properties` needs, or as a datapack that adds it as a World
  Type, for worlds made without Flatgen.

Worlds made with Flatgen are plain superflat worlds: they open without the mod. Flatgen is a client mod: servers don't
need it, and on Fabric it needs no Fabric API.
