# F30 Eight more languages

Milestone: M4 finish and release v1 · Release: v1 · Status: done · Depends on: F19

## Why

Flatgen spoke English and German. Most players on CurseForge read neither as their first language, and a picture-first
editor still has names, tooltips and messages; a player should meet them in the language the game is set to.

## Which languages

No source gives CurseForge's players by language, so the choice weighs proxies (2026-11-21): Semrush puts the audience
of CurseForge's file servers (edge.forgecdn.net) in the United States first, then Brazil and South Korea; Minecraft
streams on Twitch are in English, then Spanish, German, French and Russian (February 2023); the Chinese modding
community around mcmod.cn is among CurseForge's largest neighbours; and these are the languages large mods ship most.
Chosen: Simplified Chinese (zh_cn), Russian (ru_ru), Brazilian Portuguese (pt_br), Spanish (es_es), French (fr_fr),
Korean (ko_kr), Japanese (ja_jp) and Polish (pl_pl). Next candidates: Traditional Chinese (zh_tw), Turkish, Ukrainian
and Italian.

## Acceptance criteria

- [x] A language file for each, with every key of `en_us.json`, in the game's own words where the game names the
  thing (Superflat, Customize, Generate Structures, Create New World, the dimensions, blocks, biomes, mobs).
- [x] The language test checks every file in the language folder and that each text keeps the English placeholders.
- [x] Tried in game: the editor, the block picker, the library and the templates in Chinese, Russian and French, where
  Chinese falls back to the game's own font for its characters and Russian and French run longest.

## Design

Each language was translated by an agent of its own from en_us.json, with the German file for tone, the README and the
design notes for context, and the game's own language file of 1.21.1 for every term the game names; labels are kept as
short as the English, as text that overflows a button is cut. Russian and Polish word texts with a number so that they
read for any number. Where two words clashed, presets take the game's word and templates another: Russian Шаблоны and
Заготовки, Polish Szablony and Projekty, Portuguese modelos and moldes, Spanish plantillas and moldes.

## Tried

1.21.1 NeoForge, in a dev client set to each language: Chinese draws its characters in the game's font beside Inter,
crisp and on the same line, with nothing cut; Russian and French fit the header, the cards, the switches and the
library's footer, the game's own long preset names shortened with an ellipsis on their cards as in English. Not tried in
game: Japanese, Korean, Portuguese, Spanish and Polish.

## Left for native speakers

Structure names the game's files lack were taken from the Minecraft Wiki and are the least sure, above all Polish (Ruiny
szlaków, Skamieliny Netheru, Monumenty oceaniczne, Kopalnie). Words the game has none for were chosen: the game's editor
(原版编辑器, 標準エディター), built-in presets and world options. A review by a player of each language, or comments after release,
would settle them.
