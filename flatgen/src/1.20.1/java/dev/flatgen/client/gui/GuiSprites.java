package dev.flatgen.client.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.flatgen.Flatgen;
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
 * Flatgen's GUI sprites as 1.20.1 draws them, which has no atlas of GUI sprites: each is its own texture under
 * {@code textures/gui/sprites}, stretched over the area, or cut in nine by the border its {@code .mcmeta} gives, as the
 * later releases' atlas reads it, so that its corners keep their size.
 */
final class GuiSprites {
    /** The columns and the rows a nine-slice sprite is cut into. */
    private static final int PARTS = 3;
    /** Each sprite's texture size and border, read the first time it is drawn. */
    private static final Map<ResourceLocation, Sprite> SPRITES = new HashMap<>();

    private GuiSprites() {
    }

    /** Draws a sprite of Flatgen's GUI over this area. */
    static void draw(GuiGraphics graphics, ResourceLocation id, int x, int y, int width, int height) {
        ResourceLocation texture = id.withPath(path -> "textures/gui/sprites/" + path + ".png");
        Sprite sprite = SPRITES.computeIfAbsent(texture, GuiSprites::sprite);
        int border = Math.min(sprite.border(), Math.min(width, height) / 2);
        int edge = border == 0 ? 0 : sprite.textureBorder();
        int[] columns = {x, x + border, x + width - border, x + width};
        int[] rows = {y, y + border, y + height - border, y + height};
        int[] us = {0, edge, sprite.width() - edge, sprite.width()};
        int[] vs = {0, edge, sprite.height() - edge, sprite.height()};
        RenderSystem.enableBlend();
        for (int row = 0; row < PARTS; row++) {
            for (int column = 0; column < PARTS; column++) {
                graphics.blit(texture, columns[column], rows[row], columns[column + 1] - columns[column],
                        rows[row + 1] - rows[row], us[column], vs[row], us[column + 1] - us[column],
                        vs[row + 1] - vs[row], sprite.width(), sprite.height());
            }
        }
        RenderSystem.disableBlend();
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
        /** A sprite that could not be read, drawn as nothing. */
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
