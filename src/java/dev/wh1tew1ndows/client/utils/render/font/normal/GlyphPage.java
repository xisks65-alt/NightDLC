package dev.wh1tew1ndows.client.utils.render.font.normal;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class GlyphPage implements IMinecraft {
    public static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "\u0410\u0411\u0412\u0413\u0414\u0415\u0401\u0416\u0417\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042A\u042B\u042C\u042D\u042E\u042F\u0430\u0431\u0432\u0433\u0434\u0435\u0451\u0436\u0437\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044A\u044B\u044C\u044D\u044E\u044F"
            + "\u0299\u0493\u0262\u029C\u026A\u029F\u0274\u01EB\u0280sx\u028F"
            + "0123456789"
            + "!?@#$%^&*()-_=+[]{}|\\;:'\"<>,./`~"
            + "\u00A9\u2122\u00AE ";
    private static final float GLYPH_OFFSET = 8F;
    private int imgSize;
    private int maxFontHeight = -1;
    private final Font font;
    private final boolean antiAliasing;
    private final boolean fractionalMetrics;
    private final HashMap<Character, Glyph> glyphCharacterMap = new HashMap<>();

    private BufferedImage BufferedImage;
    private DynamicTexture loadedTexture;

    public GlyphPage(Font font, boolean antiAliasing, boolean fractionalMetrics) {
        this.font = font;
        this.antiAliasing = antiAliasing;
        this.fractionalMetrics = fractionalMetrics;
    }

    public void generateGlyphPage() {
        double maxWidth = -1;
        double maxHeight = -1;

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, antiAliasing, fractionalMetrics);

        for (int i = 0; i < CHARS.length(); ++i) {
            char ch = CHARS.charAt(i);
            Rectangle2D bounds = font.getStringBounds(Character.toString(ch), fontRenderContext);

            if (maxWidth < bounds.getWidth())
                maxWidth = bounds.getWidth();
            if (maxHeight < bounds.getHeight())
                maxHeight = bounds.getHeight();
        }

        maxWidth += 2;
        maxHeight += 2;

        imgSize = calculateImageSize(maxWidth, maxHeight);

        BufferedImage = new BufferedImage(imgSize, imgSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = BufferedImage.createGraphics();

        graphics2D.setFont(font);
        graphics2D.setColor(new Color(255, 255, 255, 0));
        graphics2D.fillRect(0, 0, imgSize, imgSize);

        graphics2D.setColor(Color.white);

        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON
                        : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_ANTIALIAS_OFF : RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        FontMetrics fontMetrics = graphics2D.getFontMetrics();

        int currentCharHeight = 0;
        int posX = 0;
        int posY = 1;

        for (int i = 0; i < CHARS.length(); ++i) {
            char ch = CHARS.charAt(i);
            Glyph glyph = new Glyph();

            Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(ch), graphics2D);

            glyph.width = bounds.getBounds().width + 8;
            glyph.height = bounds.getBounds().height;

            if (posX + glyph.width >= imgSize) {
                posX = 0;
                posY += currentCharHeight;
                currentCharHeight = 0;
            }

            glyph.x = posX;
            glyph.y = posY;

            if (glyph.height > maxFontHeight)
                maxFontHeight = glyph.height;

            if (glyph.height > currentCharHeight)
                currentCharHeight = glyph.height;

            graphics2D.drawString(String.valueOf(ch), posX + 2, posY + fontMetrics.getAscent());

            posX += glyph.width;

            glyphCharacterMap.put(ch, glyph);

        }
        graphics2D.dispose();
    }

    private double calculateMaxSize(double size, int length) {
        return Math.ceil(Math.sqrt(size * size * length) / size);
    }

    private int calculateImageSize(double maxWidth, double maxHeight) {
        double maxDimension = Math.max(maxWidth, maxHeight);
        return (int) (calculateMaxSize(maxWidth, CHARS.length()) * maxDimension) + 1;
    }

    public void setupTexture() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(BufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            loadedTexture = new DynamicTexture(NativeImage.read(data));
        } catch (Exception ignored) {
        }
    }

    public void bindTexture() {
        RenderSystem.bindTexture(loadedTexture.getGlTextureId());
    }

    public void unbindTexture() {
        RenderSystem.bindTexture(0);
    }


    private void setVertex(Matrix4f matrix, float x, float y, float z, float u, float v, float red, float green, float blue, float alpha) {
        buffer.pos(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .tex(u, v)
                .endVertex();
    }

    public float drawChar(MatrixStack stack, char character, float x, float y, float red, float blue, float green, float alpha) {
        Glyph glyph = glyphCharacterMap.get(character);
        if (glyph == null)
            return 0;

        float pageX = (float) glyph.x / imgSize;
        float pageY = (float) glyph.y / imgSize;
        float pageWidth = (float) glyph.width / imgSize;
        float pageHeight = (float) glyph.height / imgSize;
        float width = glyph.width;
        float height = glyph.height;

        Matrix4f matrix = stack.getLast().getMatrix();

        buffer.begin(GlStateManager.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        setVertex(matrix, x, y + height, 0, pageX, pageY + pageHeight, red, green, blue, alpha);
        setVertex(matrix, x + width, y + height, 0, pageX + pageWidth, pageY + pageHeight, red, green, blue, alpha);
        setVertex(matrix, x + width, y, 0, pageX + pageWidth, pageY, red, green, blue, alpha);
        setVertex(matrix, x, y, 0, pageX, pageY, red, green, blue, alpha);
        tessellator.draw();

        return width - GLYPH_OFFSET;
    }

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder buffer = tessellator.getBuffer();

    public float getWidth(char ch) {
        Glyph glyph = glyphCharacterMap.get(ch);
        if (glyph == null)
            return 0;
        return glyph.width;
    }

    public static class Glyph {
        public int x;
        public int y;
        public int width;
        public int height;
    }

    public int getImgSize() {
        return imgSize;
    }

    public void setImgSize(int imgSize) {
        this.imgSize = imgSize;
    }

    public int getMaxFontHeight() {
        return maxFontHeight;
    }

    public void setMaxFontHeight(int maxFontHeight) {
        this.maxFontHeight = maxFontHeight;
    }

    public Font getFont() {
        return font;
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public boolean isFractionalMetrics() {
        return fractionalMetrics;
    }

    public HashMap<Character, Glyph> getGlyphCharacterMap() {
        return glyphCharacterMap;
    }

    public BufferedImage getBufferedImage() {
        return BufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        BufferedImage = bufferedImage;
    }

    public DynamicTexture getLoadedTexture() {
        return loadedTexture;
    }

    public void setLoadedTexture(DynamicTexture loadedTexture) {
        this.loadedTexture = loadedTexture;
    }
}