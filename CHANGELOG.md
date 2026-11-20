# Changelog

What changed for players in each release. The GitHub release's notes list every `feat` and `fix` commit since the last
tag; this file says the same in a few lines a player reads.

## 1.0.0 (not released yet)

Flatgen's first release, for Minecraft 1.21.1 on NeoForge and Fabric. Create World → World Type: Superflat →
Customize opens Flatgen in place of the game's screen:

- **Layers as cards beside the world they make:** the world drawn at the left in proportion, the layers as cards
  with their block, how thick they are in squares, and + and −; drag a card by any part of it to move it, or a layer's
  edge in the world to make it deeper; undo and redo everything.
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
  mods and packs add their own as data files ([docs/world-options.md](docs/world-options.md)).
- **Presets:** save your own, use the game's and Flatgen's built-in ones, each a card of its layers in the sky; search
  them, star favourites, pick a default that is ready the moment you choose Superflat, share a preset as text and
  paste one back, drop preset files on the screen.
- **Templates:** a tall flat world, an airship field, a water world, the void with a platform or test layers from a
  few settings, with what each makes drawn beside them as you change them.
- **Works without reading:** every control has a picture, colour is never the only sign, the screens work with the
  keyboard alone, and the game's narrator reads them.
- **English and German.**
- **Mods' flat world types,** such as an airship-ready type, open Flatgen too.
- **For modpacks:** ship presets and a default ([docs/pack-authors.md](docs/pack-authors.md)).
- **A flat Nether and End:** tabs above the column give the Nether and the End layers and a biome of their own;
  portals, the Nether's fortresses and bastions, the dragon and the way home work as always, and the world opens
  without Flatgen.
- **Exports:** a preset as the lines a server's `server.properties` needs, or as a datapack that adds it as a World
  Type, for worlds made without Flatgen.

Worlds made with Flatgen are plain superflat worlds: they open without the mod.
