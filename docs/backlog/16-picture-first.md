# F16 Works without reading

Milestone: M4 finish and release v1 · Release: v1 · Status: doing · Depends on: F03 to F15

## Why

Some players are young children who cannot read yet (a rule in Glowline too). Flatgen should be usable from pictures
alone, and the same care makes it quicker for everyone.

## What the player gets

Every button and fact has a picture; text is extra, in tooltips and under pictures. The screen works with the keyboard
alone and with the game's narrator.

## Acceptance criteria

- [x] Every control has an icon or a picture; no control is text only. A checklist of every control is in this file
  when the feature is done (Control checklist, below).
- [x] Colour is never the only cue (shape and picture too), for colour-blind players.
- [x] Keyboard: Tab moves between areas, arrows within one, Enter uses, Escape goes back.
- [ ] Narration: every control narrates its name and state through Minecraft's narrator.
- [x] Works at GUI scale 1 to 4 and at the smallest window size.

## Design

Decided on 2026-11-19 by a panel of three agents (a UX designer for children who cannot read yet, a long-time
Minecraft and modpack player, an accessibility engineer who knows the game's GUI code), while the owner was away; the owner may
revisit any of it.

### Pictures

One picture keeps one meaning everywhere, and none takes a meaning the code already gives an item: the barrier means
missing, the book a built-in preset, the chest chosen structures, the lava bucket lakes, the grass block Superflat.
A vanilla item is used where it says the thing; Flatgen's own pixel art where no item does. Terrain blocks are never
action pictures, as beside the column they would read as layers.

| Control | Picture |
|---|---|
| Done, Use (library, templates), Yes | own: green tick |
| Cancel (editor, pickers), No | own: red cross |
| Back (library, templates) | own: left arrow, as the recipe book's page arrow |
| Biome | the biome's own colours drawn as a little landscape (sky, grass, foliage, water), and its name |
| Block, a template's block setting | the block itself, and its name |
| Structures | own: a village house |
| No slimes | the slime ball with the "no" sign |
| Undo, Redo | own: a hooked arrow curling left, right |
| Copy code, Share | paper with an "out" badge |
| Paste code, Paste | paper with an "in" badge |
| Add above, Add below | own: a layer bar with a plus above it, below it |
| Remove layer | own: a layer bar with a minus |
| Copy layer, Copy preset | the thing's own picture with a "copy" badge of two small sheets |
| Delete preset | own: a trash can |
| Presets | bookshelf |
| Templates | crafting table |
| Vanilla editor | command block |
| Surface Y | ladder |
| Thickness | own: a layer between a two-headed arrow |
| Search | compass, as the creative inventory's search tab |
| Mod filter | the state itself: all mods own 2x2 of small cubes, Minecraft the grass block, a mod its first block |
| Name field, Rename | name tag, anvil |
| Save new, Save over | writable book with a plus badge, with a circular-arrow badge |
| Built-ins setting | book; the grass block with the "no" sign; the book with the "no" sign |
| Folder | own: a folder |

- Action buttons show the picture alone, with a tooltip on hover and on keyboard focus, which the narrator reads: at
  320 wide eight pictures take 174 pixels, their words about 450. Buttons that decide (Done, Cancel, Use, Back, Yes,
  No) show the picture and a short word, as there are few, there is room, and a helping parent reads along. Biome,
  Block and the templates' block settings show the picture and the name, as the name is what they hold.
- The layer's depth buttons keep their − and + signs and their numbers, which are symbols a child learns early, not
  words.
- Delete and Save over stand at the end of their row, apart from the others. The question before them shows the
  action's picture large above Yes and No, and keyboard focus starts on No.
- A setting that cycles through few states (the built-ins setting) shows which state is on with small pips below
  its picture. The templates' block settings stand in the order their layers do, top layer first.
- Own pictures are 16 by 16 with a pixel of margin, outlined in the darkest shade of their own colour, lit from the top
  left, three or four shades a colour from vanilla's palette (emerald green, redstone red, iron grey, paper, oak),
  no anti-aliasing. A badge is 7 by 7 at the bottom right, where an item's count goes, drawn over the item as the
  "no" sign is.
- Known: where a fact's line crosses the selected layer's outline at the column's edge, the line's picture covers the
  outline, as items are drawn above what the column draws.
- Not in F16, for F29: steppers beside number fields, and a tick or a cross that answers a click on the clipboard
  buttons in place of the message line.

### Toolkit

- Own pictures are sprites of the game's GUI atlas, `assets/flatgen/textures/gui/sprites/icon/`, drawn by
  `Icon.sprite` through `Canvas.sprite`; on 1.20.1, which has no atlas, the version tree's `McCanvas` draws the same
  files as plain textures. `Icon.texture` stays for world options' data (F27). The pixel art is written as text in
  `tools/sprites.py`, which writes the PNG files, so it can be read and changed in review.
- Shapes beside colours: the toggle's light becomes an on or off picture, a button that cannot be used dims its
  picture and says so, the "no" sign (a circle and slash with a dark rim) replaces the red stroke, the card star is an
  outlined star, and the column's fact lines end in their fact's picture.
- Keyboard focus: each widget gives the controls the focus can move to (`Widget.focusables`, a group its widgets',
  a `Control` itself) and where its ring goes; `FlatgenScreen` keeps the focus as a place, an area (one of its
  top-level widgets) and a control within it, so a panel that lays itself out again as it scrolls keeps it. Tab moves
  between areas, the arrows within one when the control does not take them, Enter or Space uses, Escape goes back (a
  number field being typed in first drops what was typed), a click or the wheel ends the focus; a scrolling area
  scrolls the focused control into view. In a grid of cards the arrows move from card to card
  (`client/gui/rules/GridSteps`), the keyboard staying on its entry's card as the grid changes, Enter picks and Shift
  with Enter stars, as the mouse's buttons do.
  The order is pure (`client/gui/rules/FocusOrder`) and tested. The ring is drawn once by `FlatgenScreen`, white then
  black, a shape not a colour.
- Narration: each widget describes itself as a `Narration` in the game's own words for its kind (a button, a
  checkbox, a slider, an edit box, an entry of a list, words alone), with its state, whether it can be used, and how
  it is used with the focus or the mouse; `FlatgenScreen` hands the focused one, or else the one under the mouse, to
  the game's narrator, so no feature touches the game's narration API. Fact badges take the focus to be heard, and a
  question says its words as it opens.

## Control checklist

Every control and what shows it besides words; a word alone is never all (checked against the code on 2026-11-19).

| Screen | Control | Picture |
|---|---|---|
| Editor | the column, each layer | the layer's block side; the selected one outlined, the focus ring round it |
| Editor | fact badges | each fact's item, under the "no" sign for no; the snow and slime lines end in theirs |
| Editor | No slimes | slime ball under the "no" sign |
| Editor | favourites row | each favourite's top block |
| Editor | Presets, Templates | bookshelf, crafting table |
| Editor | Biome | the biome's landscape in its colours, and its name |
| Editor | Structures | village house, and how they are chosen |
| Editor | Block | the selected layer's block, and its name |
| Editor | depth − + and its numbers, the depth field | a layer between two arrows before them; − + and numbers are symbols |
| Editor | Surface at Y | ladder |
| Editor | Add above, Add below, Copy layer, Remove layer | own: layer bar with plus above, below, the block with a copy badge, bar with minus |
| Editor | Undo, Redo | own: hooked arrows |
| Editor | Copy code, Paste code | paper with an arrow out, in |
| Editor | world options | each option's own picture, a ticked or empty box; a missing one the barrier |
| Editor | Done, Vanilla editor, Cancel | tick, command block, cross, with their words |
| Pickers | search | compass |
| Pickers | mod filter | four small cubes for all, else the mod's first block or biome |
| Pickers | cards | the block, or the biome's colours and facts; starred ones a gold star |
| Pickers | Cancel | cross |
| Structures | the three ways to choose | grass block, barrier, chest, each with its box |
| Structures | Decorations, Lakes | poppy, lava bucket |
| Structures | each structure set | its own picture, under the "no" sign where it cannot appear |
| Structures | Done | tick |
| Library | name field | name tag |
| Library | Save new, Rename | writable book with a plus badge, anvil |
| Library | Use, Back | tick, left arrow, with their words |
| Library | Copy, Share, Paste, Folder | the picked preset with a copy badge, paper out, paper in, folder |
| Library | built-ins setting | book; grass block or book under the "no" sign; pips for its states |
| Library | Save over, Delete | writable book with a circular-arrow badge, trash can; apart at the row's end |
| Library | Default, Start on Superflat | nether star, grass block, each with its box |
| Library | cards | the preset's column and top block; marks for default (nether star), built in (book), missing parts (barrier), starred (gold star) |
| Templates | the templates | each template's item |
| Templates | number settings | ladder, water bucket, iron shovel, white carpet before each slider |
| Templates | block settings | the block |
| Templates | switches | the switch's item, with its box |
| Templates | fact badges | as the editor's |
| Templates | Use, Back | tick, left arrow, with their words |
| Question | what Yes does | its picture above the words: trash can, writable book with a circular arrow, barrier |
| Question | Yes, No | tick, cross, with their words |

## Tests

- Dev client screenshots of every screen at GUI scales 1 to 4 on both nodes; keyboard-only run through the main flows.

## Tried

Both loaders, in dev clients with Blockpilot, at GUI scale 4 in a 427 by 240 window: the editor, the library, the
templates and the question show their pictures (the biome's landscape, the village house, the bookshelf and crafting
table, the tick, cross and arrow, the ticked and empty boxes, the pips of the built-ins setting, unusable buttons
dimmed), and Tab, Tab and Down put the focus ring on the second fact badge. On NeoForge the editor shows whole and in
order at GUI scales 4, 3, 2 and 1, and in the library Tab moves from row to row, round to the first, Right moves to
Save new and Enter saves a preset, all by keyboard. After the M4 review's fixes, on NeoForge in the narrowest window,
320 by 240 at GUI scale 4: the editor lays its panels out whole (the depth buttons wrapping to a second row) with the
footer in sight, the library's three rows of controls and a row of cards fit, Tab to the library's grid, Right twice
and Enter pick the third card, and in the biome picker Tab, Tab, Right, Down and Enter choose Bamboo Jungle. Not tried
yet: the narrator, which speaks aloud through the system's voice and waits for the owner to be about.
