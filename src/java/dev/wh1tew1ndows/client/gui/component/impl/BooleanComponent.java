package dev.wh1tew1ndows.client.gui.component.impl;


import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;


public class BooleanComponent extends Component {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public BooleanSetting option;

    public BooleanComponent(BooleanSetting option) {
        this.option = option;
        this.setting = option;
    }

    public float animationToggle;

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 15;
        float off = 0.5f;
        animationToggle = Interpolator.lerp(animationToggle, option.getValue() ? 1 : 0, 0.2f);

        int color = ColorUtil.interpolateColor(new Color(0xC0AEAEAE, true).getRGB(), ColorUtil.multDark(ColorUtil.fade(), 1), animationToggle);

        // DisplayUtils.drawShadow(x + 5, y + 1 + off, 10, 10, 8, reAlphaInt(color, 50));
        RenderUtil.Rounded.smooth(matrixStack, x + width - 25 + 5, y + 1.5F + off, 16, 9, new Color(0x292932).getRGB(), Round.of(4));
        RenderUtil.Rounded.smooth(matrixStack, x + width - 25 + 6.1F + (6.7F * animationToggle), y + 2.5F + off, 7, 7, color, Round.of(4));

        // Scissor.push();
        //
        // Scissor.setFromComponentCoordinates(x + 5, y + 1 + off, 10 * animationToggle, 10);
        // Fonts.icons[12].drawString(matrixStack, "A", x + 7, y + 6 + off, -1);
        // Scissor.unset();
        // Scissor.pop();

        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 7, y + 4.5f - 2 + off, ColorUtil.getColor(220), 7);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHover(mouseX, mouseY, x, y, width - 5, 15)) {

            option.set(!option.getValue());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
