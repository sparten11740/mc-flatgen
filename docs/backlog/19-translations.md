# F19 English and German

Milestone: M4 finish and release v1 · Release: v1 · Status: done · Depends on: F16

## Why

Flatgen's first players speak German; most players on Modrinth and CurseForge read English.

## Acceptance criteria

- [x] Every text through a translation key in `assets/flatgen/lang/en_us.json` and `de_de.json`; no text in code.
- [x] A unit test fails when a key used in code is missing from either file, or a file has a key no code uses.

## Design

- German uses the game's own German words where the game has them: Flachland for Superflat, Vorlagen for presets as
  the game's superflat screen names them, Bauwerke for structures, Schleimchunks; Flatgen's templates are Baupläne.
  The words stay plain, for children.
- What players read that is not Flatgen's own stays as the game or the mod gives it: block, biome and mod names, and
  the reader's problems with a file, which go to the log, a card saying in the player's language that it could not be
  read in full. Symbols (the depth buttons' − and + and numbers) are not words.
- The code names every key whole, never joining one from parts (a table where a key goes with an enum's value), so
  `LanguageFilesTest`, which reads the Java sources and Flatgen's data files, checks every one; the only keys made
  from parts are an option's and its group's, which a mod's data file names, and Flatgen's own are named by their files
  (`option.flatgen.<file>` and its `.description`, and `option_group.<namespace>.<path>`). Both files must have
  exactly the keys named, and the same keys.

## Tests

- The key test; screenshots in both languages on one node.

## Tried

NeoForge, in a dev client with Blockpilot and the game set to Deutsch: the editor (Oberfläche bei Y, Ebene,
Bauwerke: 2 gewählt, Fertig, Spiel-Editor, Abbrechen, the world options Immer klar, Immer Tag, Keine Kreaturen) and
the library (Vorlagen, Neu speichern, Nehmen, Zurück, Standard, Mit Flachland starten, Gespeicherte Vorlagen, a new
preset named Vorlage) show German throughout; "Mit Flachland starten" was cut off by its box and the switches were
made wider. Not tried in German: the pickers, the templates and the questions.
