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

## What building it in the game takes

The game draws its GUI in whole GUI pixels with its pixel font. The modern look needs the toolkit to draw rounded
panels (nine-slice sprites, which it can already draw), a TrueType font Flatgen ships (the game's font system takes
one), finer sizes than the game's GUI scale (Flatgen's screens laying out in real pixels), and the cards, steppers,
switches, segmented control and sheet as widgets of `client/gui`. The editor's state, edits and history stay as they
are.
