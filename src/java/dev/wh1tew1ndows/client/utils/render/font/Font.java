package dev.wh1tew1ndows.client.utils.render.font;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.ScissorUtil;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import dev.wh1tew1ndows.client.utils.render.text.TextUtils;
import dev.wh1tew1ndows.client.utils.tuples.Triplet;
import lombok.Getter;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.time.Duration;
import java.util.List;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;

@Getter
public class Font implements IMinecraft, IRender {
    private final MsdfFont font;

    public Font(String name) {
        font = create(name);
    }


    public void drawWrap(MatrixStack matrix, String text, float x, float y, float width, int color, float size, float offset, Duration cycleDuration, Duration pauseDuration) {
        float textWidth = getWidth(text, size);

        ScissorUtil.enable();
        ScissorUtil.scissor(matrix, x, y - size / 4F, width, size * 1.5F);
        if (textWidth <= width) {
            draw(matrix, text, x, y, color, size);
        } else {
            long cycleMillis = cycleDuration.toMillis();
            long pauseMillis = pauseDuration.toMillis();
            long totalCycleTime = cycleMillis + pauseMillis;

            long elapsed = System.currentTimeMillis() % totalCycleTime;

            float progress = (elapsed < cycleMillis)
                    ? (float) elapsed / cycleMillis
                    : 1.0F;

            float value = (float) (Easings.SINE_IN_OUT.ease(progress) * (textWidth + offset));

            draw(matrix, text, x - value, y, color, size);
            draw(matrix, text, x - value + (textWidth + offset), y, color, size);
        }
        ScissorUtil.disable();
    }

    public void drawComponentCenter(MatrixStack matrixIn, ITextComponent componentIn, float xIn, float yIn, int colorIn, int alphaIn, float sizeIn) {
        drawComponent(matrixIn, componentIn, xIn - getWidth(componentIn.getString(), sizeIn) / 2F, yIn, colorIn, alphaIn, sizeIn);
    }

    public void drawComponentRight(MatrixStack matrix, ITextComponent text, float x, float y, int color, int alpha, float size) {
        drawComponent(matrix, text, x - (getWidth(text.getString(), size)), y, color, alpha, size);
    }

    public void drawComponent(MatrixStack matrixIn, ITextComponent componentIn, float xIn, float yIn, int colorIn, int alphaIn, float sizeIn) {
        matrixIn.push();
        ShaderManager shader = ShaderManager.fontShader;
        FontData.AtlasData atlas = font.getAtlas();
        shader.load();
        shader.setUniformi("image", 0);
        shader.setUniformf("size", atlas.width(), atlas.height());
        shader.setUniformf("range", atlas.range());
        shader.setUniformf("gamma", 1.0F);
        shader.setUniformf("edge", -1F, 1F);
        font.bind();
        BUFFER.begin(GL11.GL_QUADS, POSITION_TEX_COLOR);

        componentIn.func_241878_f().accept(new ICharacterConsumer() {
            private float x = xIn;

            @Override
            public boolean accept(int index, Style style, int codePoint) {
                int color = style.getColor() != null ? style.getColor().getColor() : colorIn;
                char codePointChar = (char) codePoint;
                x += font.applyCharacter(matrixIn, BUFFER, sizeIn, codePointChar, 0, x, yIn + font.getMetrics().baselineHeight() * sizeIn, 0, 0, ColorUtil.replAlpha(color | 0xFF000000, alphaIn));
                return true;
            }
        });

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        TESSELLATOR.draw();
        RenderSystem.disableBlend();
        font.unbind();
        shader.unload();
        matrixIn.pop();
    }

    public float getHeight(float size) {
        return font.getHeight(size);
    }

    public void drawTextComponent(MatrixStack matrix, ITextComponent textComponent, float x, float y, int color, boolean shadow, float size) {
        StringBuilder sb = new StringBuilder();
        for (ITextComponent component : textComponent.getSiblings()) {
            if (!component.getSiblings().isEmpty()) {
                for (ITextComponent charComponent : component.getSiblings()) {
                    sb.append(ColorFormatting.getColor(charComponent.getStyle().getColor() != null ? ColorUtil.replAlpha(charComponent.getStyle().getColor().getColor(), ColorUtil.alphaf(color)) : color));
                    sb.append(replaceSymbols(charComponent.getString()));
                }
            } else {
                sb.append(ColorFormatting.getColor(component.getStyle().getColor() != null ? ColorUtil.replAlpha(component.getStyle().getColor().getColor(), ColorUtil.alphaf(color)) : color));
                sb.append(replaceSymbols(component.getString()));
            }
        }
        draw(matrix, sb.toString(), x, y, color, size);
    }

    public float getWidth(ITextComponent textComponent, float size) {
        StringBuilder sb = new StringBuilder();
        for (ITextComponent component : textComponent.getSiblings()) {
            if (!component.getSiblings().isEmpty()) {
                for (ITextComponent charComponent : component.getSiblings()) {
                    sb.append(ColorFormatting.getColor(-1));
                    sb.append(replaceSymbols(charComponent.getString()));
                }
            } else {
                sb.append(ColorFormatting.getColor(-1));
                sb.append(replaceSymbols(component.getString()));
            }
        }
        return getWidth(sb.toString(), size);
    }

    private String replaceSymbols(String string) {
        return string
                .replaceAll("⚡", "")
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("s", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("x", "x")
                .replaceAll("ʏ", "y")
                .replaceAll("ᴢ", "z");
    }

    private MsdfFont create(String name) {
        final MsdfFont font;
        font = MsdfFont.builder().withAtlas(name + ".png").withData(name + ".json").build();
        return font;
    }

    public void drawWithSpace(MatrixStack matrix, String text, float x, float y, int color, float size, float space) {
        draw(matrix, text, x, y, color, size, false, 0F, -1, space);
    }

    public void drawGradient(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        int speed = 8;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sb.append(ColorFormatting.getColor(ColorUtil.fade(speed, i * 4 * speed, color1, color2))).append(ch);
        }
        draw(matrix, sb.toString(), x, y, -1, size);
    }


    public void drawGRS(MatrixStack ms, CharSequence text, double x, double y, int c1, int c2, float size) {
        if (text == null) return;
        final int len = text.length();
        if (len == 0) return;

        float xOff = 0f;

        // Предотвращаем деление на ноль и лишние вычисления
        final boolean single = (len == 1);
        final float invDen = single ? 0f : 1f / (len - 1);

        // Извлекаем компоненты один раз
        final int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        final int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;

        // Маленький буфер вместо String.valueOf для каждого символа
        final StringBuilder sb = new StringBuilder(1);
        sb.append('\0');

        for (int i = 0; i < len; i++) {
            final char ch = text.charAt(i);
            final float t = single ? 0f : (i * invDen);

            // Линейная интерполяция компонент (целочисленно, без Math*)
            final int a = a1 + Math.round((a2 - a1) * t);
            final int r = r1 + Math.round((r2 - r1) * t);
            final int g = g1 + Math.round((g2 - g1) * t);
            final int b = b1 + Math.round((b2 - b1) * t);
            final int argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);

            sb.setCharAt(0, ch);
            draw(ms, String.valueOf(sb), (float) (x + xOff), (float) y, argb, size);

            // Если есть быстрый глиф-метод — лучше использовать его
            xOff += getWidth(String.valueOf(sb), size);
        }
    }


    public void drawGradient(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size, int speed) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sb.append(ColorFormatting.getColor(ColorUtil.fade(speed, i * speed, color1, color2))).append(ch);
        }
        draw(matrix, sb.toString(), x, y, -1, size);
    }

    public void drawGradientB(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        if (text == null || text.isEmpty()) return;

        final int len = text.length();
        if (len == 1) {
            // один символ — просто color1
            draw(matrix, ColorFormatting.getColor(color1) + text, x, y, -1, size);
            return;
        }

        // распаковываем компоненты ARGB
        final int a1 = (color1 >>> 24) & 0xFF, r1 = (color1 >>> 16) & 0xFF, g1 = (color1 >>> 8) & 0xFF, b1 = color1 & 0xFF;
        final int a2 = (color2 >>> 24) & 0xFF, r2 = (color2 >>> 16) & 0xFF, g2 = (color2 >>> 8) & 0xFF, b2 = color2 & 0xFF;

        final float inv = 1f / (len - 1);
        StringBuilder sb = new StringBuilder(len * 12);

        // плавность можно усилить лёгким сглаживанием по синусоиде:
        // t -> 0.5 - 0.5*cos(pi * t) (S-образная кривая)
        for (int i = 0; i < len; i++) {
            float t = i * inv;                           // [0..1] слева направо
            t = 0.5f - 0.5f * (float) Math.cos(Math.PI * t); // S-образное сглаживание

            int a = a1 + Math.round((a2 - a1) * t);
            int r = r1 + Math.round((r2 - r1) * t);
            int g = g1 + Math.round((g2 - g1) * t);
            int b = b1 + Math.round((b2 - b1) * t);
            int argb = (a << 24) | (r << 16) | (g << 8) | b;

            sb.append(ColorFormatting.getColor(argb)).append(text.charAt(i));
        }

        // один draw — без просадки по FPS
        draw(matrix, sb.toString(), x, y, -1, size);
    }

    public void drawGradientCenter(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        drawGradient(matrix, text, x - (getWidth(text, size) / 2F), y, color1, color2, size);
    }

    public void drawGradientCenter(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size, int speed) {
        drawGradient(matrix, text, x - (getWidth(text, size) / 2F), y, color1, color2, size, speed);
    }

    public void drawGradientRight(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        drawGradient(matrix, text, x - getWidth(text, size), y, color1, color2, size);
    }

    public float drawSplitted(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> draw(m, s, x, y + o, color, size), true);
    }

    public float drawSplittedCenter(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> drawCenter(m, s, x + (width / 2F), y + o, color, size), true);
    }

    public float drawSplittedRight(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> drawRight(m, s, x + width, y + o, color, size), true);
    }

    public float splitted(MatrixStack matrix, String text, String splitter, float width, float size, Triplet.TriConsumer<MatrixStack, String, Float> drawFunction, boolean draw) {
        List<String> strings = TextUtils.splitLine(text, this, size, width, splitter);
        float offset = 0.0F;
        for (String str : strings) {
            if (draw) drawFunction.accept(matrix, str, offset);
            offset += size;
        }
        return offset;
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x, y, color, size, false, 0F, -1);
    }

    public void drawShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x + 0.25F, y + 0.25F, ColorUtil.multDark(color, 0.25F), size, false, 0F, -1);
        draw(matrix, text, x, y, color, size, false, 0F, -1);
    }

    public void drawOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x, y, color, size, true, 0.25F, ColorUtil.multDark(ColorUtil.multAlpha(color, 0.5F), 0.25F));
    }

    public void drawRight(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawRightShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawShadow(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawRightOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawOutline(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawCenter(MatrixStack matrix, String text, float x, float y, int color, float size, float tick) {
        draw(matrix, text, x - (getWidth(text, size) / 2F), y, color, size, true, tick, color);
    }

    public void drawCenter(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void drawCenterShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawShadow(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void drawCenterOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawOutline(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size, boolean outline, float outlineThickness, int outlineColor) {
        draw(matrix, text, x, y, color, size, outline, outlineThickness, outlineColor, 0);
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size, boolean outline, float outlineThickness, int outlineColor, float space) {

        matrix.push();
        ShaderManager shader = ShaderManager.fontShader;
        FontData.AtlasData atlas = this.font.getAtlas();
        shader.load();
        shader.setUniformi("image", 0)
                .setUniformf("textureSize", atlas.width(), atlas.height())
                .setUniformf("range", atlas.range())
                .setUniformf("edgeStrength", -0.5F, 0.5F)
                .setUniformf("thickness", 0.0F)
                .setUniformi("outline", (outline ? 1 : 0))
                .setUniformf("outlineThickness", outlineThickness)
                .setUniformf("outlineColor", ColorUtil.getRGBAf(outlineColor));

        this.font.bind();
        IRender.BUFFER.begin(GL11.GL_QUADS, POSITION_TEX_COLOR);
        this.font.applyGlyphs(matrix, BUFFER, size, TextFormatting.removeFormatting(text), 0, x, y + font.getMetrics().baselineHeight() * size, 0, space, color);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        IRender.TESSELLATOR.draw();
        RenderSystem.disableBlend();
        this.font.unbind();
        shader.unload();
        matrix.pop();
    }

    public float getWidth(String text, float size) {
        return font.getWidth(text, size);
    }


    public float getWidth(String text, float size, float thickness) {
        return font.getWidth(text, size, thickness);
    }
}
