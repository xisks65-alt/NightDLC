package dev.wh1tew1ndows.client.gui.component.impl;

import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;

public class SliderComponent extends Component {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    private float round(float value, float increment) {
        return Math.round(value / increment) * increment;
    }

    public SliderSetting option;

    public SliderComponent(SliderSetting option) {
        this.option = option;
        this.setting = option;

    }

    boolean drag;

    float anim;

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height += 2;
        float sliderWidth = ((option.getValue() - option.min) / (option.max - option.min)) * (width - 12);
        anim = Interpolator.lerp(anim, sliderWidth, 0.4f);
        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 6, y + 2.5F, ColorUtil.getColor(220), 7);
        Fonts.SF_BOLD.draw(matrixStack, String.valueOf(option.getValue()), x + width - Fonts.SF_BOLD.getWidth(String.valueOf(option.getValue()), 7) - 6, y + 2.5F, ColorUtil.getColor(220), 7);
        RenderUtil.Rounded.smooth(matrixStack, x + 6, y + 13, width - 12, 3, new Color(0x292932).getRGB(), Round.of(1.5F));

        RenderUtil.Rounded.smooth(matrixStack, x + 6, y + 13, anim, 3, InterFace.getInstance().clientColor(), Round.of(1.5F));

        //RenderUtil.Rounded.drawCircle(x + 5 + anim, y + 14.5f, 7, ColorUtil.multDark(ColorUtil.fade(), 0.7F), matrixStack);
        //RenderUtil.Rounded.drawCircle(x + 5 + anim, y + 14.5f, 4, ColorUtil.multDark(ColorUtil.fade(), 0.3F), matrixStack);
        if (drag) {
            float draggingValue = MathHelper.clamp(round((mouseX - x) / (width - 12)
                    * (option.max - option.min) + option.min, option.increment), option.min, option.max);
            option.set(draggingValue);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHover(mouseX, mouseY, x, y, width, height)) {
            drag = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        drag = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
