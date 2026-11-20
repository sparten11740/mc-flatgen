# A simpler editor (F29)

The design track for an editor that looks simpler, that a child of about six who cannot read could mostly work out,
and that still does everything an adult building flat worlds needs. It is iterated on a design canvas first and built
in the game only once the owner has accepted a design. The canvas, private to the owner:
https://claude.ai/artifact/Jgoi7Xdgyi8zxsuLjNYV1Z

## Today's editor, as the panel saw it

Screenshots of the editor as built are in `current/` (taken on a Retina screen: the "scale 2" shots show the roomy
854×480 GUI space, `11-flatgen-auto-scale.png` the cramped 427×240 one most players get; `12` and `13` are the game's
own Customize and Presets screens). Four panelists reviewed them, each from a fresh start:

- **A child of six who cannot read** loved the column as "my world from the side" but got lost in the grey bars; the
  number buttons, the small + and the text fields meant nothing; they wanted to drag the top to grow a layer, a figure
  standing on top, bigger blocks sorted into tabs, a big undo, and a red tower in the Nether.
- **The user-experience lead** asked for a simple view by default with an "everything" switch for adults, blocks drawn
  one per tile with tall layers folded, a layer's controls beside the layer, three world pictures (biome, day,
  monsters), Undo, Done and Cancel in fixed places, and pickers that open on a short list.
- **A builder of flat test worlds** needs typing the ground's Y, presets one click away (shown as their columns, with
  names), undo, superflat codes and a search that finds modded blocks; missing: how much air is above the ground.
- **The accessibility expert** asked for 32 px main targets, no numbers in the first view, a solid panel behind text,
  a selection unlike the focus ring, look-alike icons redrawn by shape, and the column as the keyboard's hub.

## Directions tried

Round 1 drew three directions in the game's own look: A, a tower with a bubble of layer controls beside the chosen
layer; B, painting layers with blocks from a hotbar; C, the layers as cards, each with its block, its thickness as
tiles and big thinner, thicker and remove buttons, beside a small tower. C was followed up with moving a card by
dragging it (a grip on each card, a gap opening where it lands, the tower showing the new order), more layers than fit
(the list scrolls between two arrows that show the blocks above and below; the tower becomes a map with a frame around
the cards in view), and each layer's block count drawn on its picture as the game draws a stack's count.

## Decision

The owner chose C, drawn in a modern look at high resolution rather than the game's own (round 2 on the canvas):

- A top bar with the dimensions as one segmented switch (a dot marks a dimension with a stack of its own), Presets,
  Templates and an Advanced switch.
- Left, the world from the side with the figure on top and the air above the ground; the middle, the layer cards with
  a drag grip, the block with its count, the name, a stepper and remove, "+" lines between cards to add a layer
  there; right, the biome as a picture, the world's switches and the favourite presets as named columns; Undo and Redo,
  Cancel and Done in the footer.
- Advanced adds the ground's Y as a field, each layer's Ys and id, typed thickness, copying a layer, superflat codes,
  structures, decorations and lakes, mods' options and the game's own editor.
- An empty Nether or End shows the dimension as the game makes it and one button to make it flat.
- The block picker is a sheet over the editor that opens on the ground blocks, with favourites, recent, kinds of block,
  every block, a mod filter and a hint for searching one mod, and marks blocks that fall.

## The prototype in the game

Built overnight on this branch for every node (1.21.1 and 26.1, NeoForge and Fabric) and tried in a dev client of
each; screenshots from 1.21.1 NeoForge in `prototype/`, at a window of 1708 by 960 pixels, which the scale draws as
854 by 480 of Flatgen's GUI pixels (a 1920 by 1080 window draws exactly the canvas at 640 by 360):

| Screen | File |
|---|---|
| The editor, simple | `01-editor.png` |
| Picking a block, a sheet over the editor | `02-block-picker.png` |
| Picking a biome | `03-biome-picker.png` |
| Advanced on | `04-advanced.png` |
| Structures | `05-structures.png` |
| A Nether the game makes, and once flat | `06-nether-game-made.png`, `07-nether-flat.png` |
| Asking before giving the Nether back | `08-question.png` |
| An End the game makes | `09-end-game-made.png` |
| The presets library, the templates | `10-library.png`, `11-templates.png` |

Tried in game on 1.21.1 NeoForge: making a layer thicker, adding one on top, dragging a card by its grip to another
place, undoing each step, picking a block from a card, Advanced, a flat Nether and giving it back. On the other three
nodes: the editor, Advanced and the block picker. A screen narrower than 640 was tried by forcing the scale: the
header's and the bar's buttons give way to their pictures.

After the build, a separate agent reviewed the branch's code against the project's standards, and five agents reviewed
screenshots of every screen on both versions and in German, each for one thing: alignment and spacing, cut borders
and focus rings, pictures for players who cannot read, translations, and fidelity to the canvas. What they found is
fixed in the commits after the build: keyboard focus follows the cards, clips round outward so no border is cut,
tooltips sit below what they tell in Flatgen's own box, buttons centre picture and words, the world shows its ground,
the header's tools sit at the right, structures have one choice of mode and names in both languages, and every
picture means one thing.

Left out, for the owner to decide on:

- the library's and templates' layouts from round 3 (they keep their old arrangement in the new frame), the block
  picker's kinds of block, and the biome picker's landscape cards;
- an illustration in the empty Nether;
- "+" lines between cards, and the favourite presets as named columns;
- windows smaller than 1280 by 720 window pixels draw at one window pixel per Flatgen pixel, so text there is small;
  respecting a smaller GUI scale the player chose is not done either;
- the aside lays all panels out again as it scrolls, which costs nothing a player notices but is not the simplest.

## How it is built in the game

- **Its own scale.** A Flatgen screen counts in GUI pixels of its own (`client/gui/rules/LayoutScale`): a whole
  number of window pixels each, as many as leave the screen at least 640 by 360 of them, apart from the game's GUI
  scale. A 1920 by 1080 window is exactly the canvas at a third; larger and in-between windows get a little more room.
  Whole pixels keep every line, rounded corner and letter sharp; half steps were tried and left seams in the game's
  tiled sprites. `GameScreen`, one class per version, sets the screen's size in these pixels, draws scaled from them
  and hands the mouse over in them; Blockpilot's queries report the game's own pixels.
- **Its own look.** Inter, which Flatgen ships under the SIL Open Font License, in three styles (`TextStyle`: body,
  strong, title), each defined once per window pixel count so the game draws its glyphs at exactly the size shown;
  rounded panels, buttons, fields, switches, chips and the focus ring as nine-slice sprites, and the icons as line
  drawings; colours and sprites in `Look`. `tools/modern_look.py` draws all of them from numbers in a few seconds.
  Every Flatgen screen gets the look through the canvas, the old ones included.
- **Regions.** An editor panel places its widgets in the editor's regions (`EditorRegion`: header tools, the bar above
  the layers, the layers, the world, the aside, the footer) and shows what only Advanced shows while the editor says
  Advanced is on; adding a panel still changes no other.
- **Cards.** `LayerCards` draws a card per layer, top first, from the existing widgets and edits: every control selects
  its layer, then makes the edit the layer controls always made; dragging a card's grip previews the move as the
  column's drag did. The block picker opens from the card's block.
- **The column** keeps everything it did and draws the stack at the bottom, under the air the world has above it, in
  the dimension's air colour.
- **Sheets.** A picker or a question stands over the screen it came from, which is drawn dimmed behind it
  (`GameScreen.behind`).
- **Not yet handled:** widgets another mod adds to a Flatgen screen through its loader's screen events would get the
  screen's own size but be drawn in the game's pixels; no mod does so today. Advanced is remembered until the game
  closes, not in the settings file.
