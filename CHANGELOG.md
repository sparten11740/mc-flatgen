# Changelog

What changed for players in each release. The GitHub release's notes list every `feat` and `fix` commit since the last
tag; this file says the same in a few lines a player reads.

## 1.0.0 (not released yet)

Flatgen's first release, for Minecraft 1.21.1 on NeoForge and Fabric. Create World → World Type: Superflat →
Customize opens Flatgen in place of the game's screen:

- **Layers you edit where you see them:** the world's layers as a column, in proportion; drag a layer to move it, drag
  its edge to make it deeper, turn the wheel over it; undo and redo everything; the game's superflat code copied and
  pasted, with the wrong part named.
- **Every block and biome, modded ones too,** as pictures you search, filter by mod and star; hints for layers that
  fall or slow the game.
- **Know the world before you make it:** the surface's height, rain, snow or sun there, whether slimes and monsters
  spawn, and lines on the column where snow starts and slimes stop.
- **Structures, decorations and lakes,** every structure set the game knows, modded ones too, marked where the biome
  will not have it.
- **World options:** always day, always clear and no mobs as picture switches, saved with the preset; mods and packs
  add their own as data files ([docs/world-options.md](docs/world-options.md)).
- **Presets:** save your own, use the game's and Flatgen's built-in ones, star favourites, pick a default that is ready
  the moment you choose Superflat, share a preset as text and paste one back, drop preset files on the screen.
- **Templates:** a tall flat world, an airship field, a water world, the void with a platform or test layers from a
  few settings.
- **Works without reading:** every control has a picture, colour is never the only sign, the screens work with the
  keyboard alone, and the game's narrator reads them.
- **English and German.**
- **Mods' flat world types,** such as an airship-ready type, open Flatgen too.
- **For modpacks:** ship presets and a default ([docs/pack-authors.md](docs/pack-authors.md)).
- **Exports:** a preset as the lines a server's `server.properties` needs, or as a datapack that adds it as a World
  Type, for worlds made without Flatgen.

Worlds made with Flatgen are plain superflat worlds: they open without the mod.
