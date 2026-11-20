package dev.flatgen.client.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.flatgen.Flatgen;
import dev.flatgen.client.gui.rules.Rect;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;

/**
 * Flatgen's GUI sprites as 1.21.1 draws them: each is its own texture under {@code textures/gui/sprites}, stretched over
 * the area, or cut in nine by the border its {@code .mcmeta} gives, its corners at their size and its edges and middle
 * stretched. The game's own nine-slice drawing tiles the edges and the middle, one draw for each tile, which for a
 * sheet as large as the pickers' is thousands each frame, until 1.21.11 batches them and stretches the middle.
 */
final class GuiSprites {
    /** The columns and the rows a nine-slice sprite is cut into. */
    private static final int PARTS = 3;
    /** Each sprite's texture size and border, read the first time it is drawn after a screen opened. */
    private static final Map<ResourceLocation, Sprite> SPRITES = new HashMap<>();

    private GuiSprites() {
    }

    /** Forgets the sprites read, as a screen opens, so a resource pack that changed them since is drawn as it is. */
    static void forget() {
        SPRITES.clear();
    }

    /** Draws a sprite of Flatgen's GUI over this area. */
    static void draw(GuiGraphics graphics, ResourceLocation id, Rect area) {
        ResourceLocation texture = id.withPath(path -> "textures/gui/sprites/" + path + ".png");
        Sprite sprite = SPRITES.computeIfAbsent(texture, GuiSprites::sprite);
        int border = Math.min(sprite.border(), Math.min(area.width(), area.height()) / 2);
        RenderSystem.enableBlend();
        if (border == 0) {
            graphics.blit(texture, area.x(), area.y(), area.width(), area.height(), 0, 0, sprite.width(),
                    sprite.height(), sprite.width(), sprite.height());
        } else {
            drawNineSlice(graphics, texture, sprite, border, area);
        }
        RenderSystem.disableBlend();
    }

    /** Draws a sprite cut in nine over an area, its corners at their size. */
    private static void drawNineSlice(GuiGraphics graphics, ResourceLocation texture, Sprite sprite, int border,
            Rect area) {
        int edge = sprite.textureBorder();
        int[] columns = {area.x(), area.x() + border, area.right() - border, area.right()};
        int[] rows = {area.y(), area.y() + border, area.bottom() - border, area.bottom()};
        int[] us = {0, edge, sprite.width() - edge, sprite.width()};
        int[] vs = {0, edge, sprite.height() - edge, sprite.height()};
        for (int row = 0; row < PARTS; row++) {
            for (int column = 0; column < PARTS; column++) {
                graphics.blit(texture, columns[column], rows[row], columns[column + 1] - columns[column],
                        rows[row + 1] - rows[row], us[column], vs[row], us[column + 1] - us[column],
                        vs[row + 1] - vs[row], sprite.width(), sprite.height());
            }
        }
    }

    private static Sprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getResourceManager().getResource(texture).map(GuiSprites::read)
                .orElse(Sprite.NONE);
    }

    private static Sprite read(Resource resource) {
        try (InputStream in = resource.open(); NativeImage image = NativeImage.read(in)) {
            Scaling scaling = resource.metadata().getSection(Scaling.SECTION).orElse(Scaling.STRETCH);
            return new Sprite(image.getWidth(), image.getHeight(), scaling.border(),
                    scaling.border() * image.getWidth() / Math.max(1, scaling.width()));
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not read a GUI sprite of {}", resource.sourcePackId(), exception);
            return Sprite.NONE;
        }
    }

    /**
     * A sprite's texture.
     *
     * @param width         its width in texture pixels
     * @param height        its height in texture pixels
     * @param border        its border in GUI pixels, none for a sprite that stretches
     * @param textureBorder its border in texture pixels
     */
    private record Sprite(int width, int height, int border, int textureBorder) {
        /** A sprite that could not be read, drawn as the game's missing texture, as later releases' atlas draws it. */
        static final Sprite NONE = new Sprite(1, 1, 0, 0);
    }

    /**
     * A sprite's scaling as its {@code .mcmeta} gives it.
     *
     * @param width  how wide the sprite is in GUI pixels, which its texture may give in more
     * @param border how wide its border is in GUI pixels, none for a sprite that stretches
     */
    private record Scaling(int width, int border) {
        /** A sprite stretched over the area, as one without scaling is. */
        static final Scaling STRETCH = new Scaling(1, 0);
        /** The {@code gui} section of a sprite's {@code .mcmeta}, as the later releases' atlas reads it. */
        static final MetadataSectionSerializer<Scaling> SECTION = new MetadataSectionSerializer<>() {
            @Override
            public String getMetadataSectionName() {
                return "gui";
            }

            @Override
            public Scaling fromJson(JsonObject json) {
                JsonObject scaling = json.getAsJsonObject("scaling");
                boolean nineSlice = scaling != null && "nine_slice".equals(scaling.get("type").getAsString());
                return nineSlice ? new Scaling(scaling.get("width").getAsInt(), scaling.get("border").getAsInt())
                        : STRETCH;
            }
        };
    }
}
