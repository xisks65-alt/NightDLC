package dev.wh1tew1ndows.client.gui.component;


import dev.wh1tew1ndows.client.gui.component.impl.ColorComponent;
import dev.wh1tew1ndows.client.managers.events.input.MousePressEvent;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;


public class ColorWindow {

    /**
     * ЭТО ГПТ КОД Я КРУТО ДА \(￣︶￣*\))
     */

    private final ColorComponent component;
    private float[] hsb;
    private float alpha;
    private boolean dragging;
    private boolean draggingHue;
    private boolean draggingAlpha;

    // Размеры и позиция окна
    private float posX, posY;
    private final float windowWidth = 228 / 2f;
    private final float windowHeight = 321 / 2f;

    public ColorWindow(ColorComponent component) {
        this.component = component;
        float[] rgb = ColorUtil.rgba(component.option.getValue());
        hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
        alpha = rgb[3] / 255f;
    }

    public static float[] copied = new float[2];

    public static void drawGradientRound(float x, float y, float width, float height, float radius, int bottomLeft, int topLeft, int bottomRight, int topRight) {

        //  ShaderManager gradientRound = ShaderManager.create(new GradientRoundGlsl());
        //  gradientRound.load();
        //  // ShaderManager.setupRoundedRectUniforms(x, y, width, height, radius, gradientRound);
        //  MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
        //  gradientRound.setUniformi("location", (int) (x * 2),
        //          (int) ((mainWindow.getHeight() - (height * 2)) - (y * 2)));
        //  gradientRound.setUniformi("rectSize", (int) (width * 2), (int) (height * 2));
        //  gradientRound.setUniformi("radius", (int) (radius * 2));
        //  gradientRound.setUniformf("color1", ColorUtil.rgba(bottomLeft));
        //  gradientRound.setUniformf("color2", ColorUtil.rgba(topLeft));
        //  gradientRound.setUniformf("color3", ColorUtil.rgba(bottomRight));
        //  gradientRound.setUniformf("color4", ColorUtil.rgba(topRight));

        //  ShaderManager.drawQuads(x - 1, y - 1, width + 2, height + 2);
        //  gradientRound.unload();

    }

    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        posX = component.x + component.width - 10 + 8 / 2f;
        posY = component.y + component.height / 2f + 8 / 2f;

        float x = posX;
        float y = posY;
        float width = windowWidth;


        RenderUtil.Rounded.smooth(stack, x, y, width, windowHeight, new Color(0x1A1C20).getRGB(), Round.of(4));
        RenderUtil.Rounded.roundedOutline(stack, x, y, width, windowHeight, 1, new Color(0x484D57).getRGB(), Round.of(4));

        drawGradientRound(x + 4, y + 4, width - 8, width - 8, 1,
                Color.WHITE.getRGB(), Color.BLACK.getRGB(),
                Color.getHSBColor(hsb[0], 1, 1).getRGB(), Color.BLACK.getRGB());


        if (MousePressEvent.getInstance().isKey(0)) {
            if (dragging) {
                float saturation = MathHelper.clamp((mouseX - x - 4), 0, width - 8) / (width - 8);
                float brightness = MathHelper.clamp((mouseY - y - 4), 0, width - 8) / (width - 8);
                hsb[1] = saturation;
                hsb[2] = 1 - brightness;
            } else {
                dragging = false;
            }

            if (draggingHue) {
                hsb[0] = MathHelper.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            } else {
                draggingHue = false;
            }

            if (draggingAlpha) {
                alpha = MathHelper.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            } else {
                draggingAlpha = false;
            }


        }


        float circleX = x + 4 + hsb[1] * (width - 8);
        float circleY = y + 4 + (1 - hsb[2]) * (width - 8);
        //  RenderUtil.drawCircle(circleX + 1, circleY + 1, 8, Color.BLACK.getRGB(), stack);
        //  RenderUtil.drawCircle(circleX + 1, circleY + 1, 6, reAlphaInt(component.option.getValue(), 255), stack);


        for (int i = 0; i < width - 12; i++) {
            float hue = i / (width - 12);
            //     RenderUtil.drawCircle(x + 6 + i, y + width + 6, 6, reAlphaInt(Color.HSBtoRGB(hue, 1, 1), 255), stack);
        }


        for (int i = 0; i < width - 12; i++) {
            float hue = i / (width - 12);
            //      RenderUtil.drawCircle(x + 6 + i, y + width + 18, 6,
            //              ColorUtil.interpolateColor(reAlphaInt(component.option.getValue(), 255),
            //                      ColorUtil.getColor(17, 18, 21, 255), 1 - hue), stack);
        }

        // Кружки Hue и Alpha
        // RenderUtil.drawCircle(x + 6 + hsb[0] * (width - 12), y + width + 6, 8, Color.BLACK.getRGB(), stack);
        // RenderUtil.drawCircle(x + 6 + hsb[0] * (width - 12), y + width + 6, 6, Color.HSBtoRGB(hsb[0], 1, 1), stack);
        // RenderUtil.drawCircle(x + 6 + alpha * (width - 12), y + width + 18, 8, Color.BLACK.getRGB(), stack);
        // RenderUtil.drawCircle(x + 6 + alpha * (width - 12), y + width + 18, 6,
        //         ColorUtil.interpolateColor(ColorUtil.getColor(17, 18, 21, 255),
        //                 reAlphaInt(component.option.getValue(), 255), alpha), stack);

        // Кнопки Copy / Paste
        RenderUtil.Rounded.smooth(stack, x + 4, y + windowHeight - 36 / 2f - 4, 102 / 2f, 36 / 2f, new Color(0x26292E).getRGB(), Round.of(3));
        RenderUtil.Rounded.smooth(stack, x + 4 + 51 + 4, y + windowHeight - 36 / 2f - 4, 102 / 2f, 36 / 2f, new Color(0x26292E).getRGB(), Round.of(3));
        Fonts.SF_BOLD.drawCenter(stack, "Copy", x + 4 + 51 / 2f, y + windowHeight - 16.9f, dev.wh1tew1ndows.client.utils.render.color.ColorUtil.getColor(220), 7);
        dev.wh1tew1ndows.client.utils.render.font.Fonts.SF_BOLD.drawCenter(stack, "Paste", x + 4 + 51 + 4 + 51 / 2f, y + windowHeight - 16.9f, dev.wh1tew1ndows.client.utils.render.color.ColorUtil.getColor(220), 7);

        // Применение цвета
        // Применять только если значения реально меняются
        //  if (dragging || draggingAlpha || draggingHue) {
        //      component.option.set(reAlphaInt(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB(), (int) (alpha * 255)));
        //  }
    }

    public boolean click(int mouseX, int mouseY) {
        float x = posX;
        float y = posY;
        float width = windowWidth;

        if (Mathf.isInRegion(mouseX, mouseY, x + 4, y + 4, width - 8, width - 8)) {
            dragging = true;
            return false;
        }

        if (Mathf.isInRegion(mouseX, mouseY, x + 4, y + width + 1, width - 8, 6)) {
            draggingHue = true;
            return false;
        }

        if (Mathf.isInRegion(mouseX, mouseY, x + 4, y + width + 13, width - 8, 6)) {
            draggingAlpha = true;
            return false;
        }

        if (Mathf.isInRegion(mouseX, mouseY, x + 4, y + windowHeight - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            float[] rgb = ColorUtil.rgba(component.option.getValue());
            copied = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
            return false;
        }

        if (Mathf.isInRegion(mouseX, mouseY, x + 4 + 51 + 4, y + windowHeight - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            if (copied.length >= 3) {
                component.option.set(Color.HSBtoRGB(copied[0], copied[1], copied[2]));
                float[] rgb = ColorUtil.rgba(component.option.getValue());
                hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
                alpha = rgb[3] / 255f;
            }
            return false;
        }

        return true;
    }

    public void unclick(int mouseX, int mouseY) {
        // Просто сбрасываем все флаги при отпускании мыши
        dragging = false;
        draggingHue = false;
        draggingAlpha = false;
    }


    public void onConfigUpdate() {
        float[] rgb = ColorUtil.rgba(component.option.getValue());
        hsb = Color.RGBtoHSB((int) rgb[0], (int) rgb[1], (int) rgb[2], null);
        alpha = rgb[3] / 255f;
    }
}
