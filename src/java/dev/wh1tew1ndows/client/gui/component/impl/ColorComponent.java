package dev.wh1tew1ndows.client.gui.component.impl;

import dev.wh1tew1ndows.client.gui.component.ColorWindow;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ColorSetting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.mojang.blaze3d.matrix.MatrixStack;

public class ColorComponent extends Component {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public static ColorWindow opened;
    public ColorSetting option;
    public ColorWindow setted;

    public ColorComponent(ColorSetting option) {
        this.option = option;
        setted = new ColorWindow(this);
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 5, y + height / 2f - 3.25F, ColorUtil.getColor(220), 7);
        float size = 8;
        RenderUtil.Rounded.smooth(matrixStack, x + width - 10 - size / 2f, y + height / 2f - size / 2f, size, size, option.getValue(), Round.of(4));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float size = 12;
        if (isHover(mouseX, mouseY, x + width - 10 - size / 2f, y + height / 2f - size / 2f, size, size)) {
            if (setted == opened) {
                opened = null;
                return;
            }
            opened = setted;
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

    @Override
    public void onConfigUpdate() {
        super.onConfigUpdate();
        setted.onConfigUpdate();
    }
}
