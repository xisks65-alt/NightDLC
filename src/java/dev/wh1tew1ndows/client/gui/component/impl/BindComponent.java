package dev.wh1tew1ndows.client.gui.component.impl;


import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;

public class BindComponent extends Component {

    public BindSetting option;
    boolean bind;


    public BindComponent(BindSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

        height -= 3;

        String bindString = option.getValue() == 0 ? "n/a" : Keyboard.keyName(option.getValue());

        if (bindString == null) {
            bindString = "";
        }

        float width = Fonts.SF_BOLD.getWidth(bindString, 7) + 4;
        RenderUtil.Rounded.smooth(matrixStack, x + 5, y + 2, width, 12, bind ? new Color(0x292932).brighter().brighter().getRGB() : new Color(0x292932).brighter().getRGB(), Round.of(3));
        Fonts.SF_BOLD.drawCenter(matrixStack, bindString, x + 5 + (width / 2), y + 4.5F, ColorUtil.getColor(220), 7);
        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 5 + width + 3, y + 4.5F, ColorUtil.getColor(220), 7);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bind && mouseButton > 1) {
            option.set(-100 + mouseButton);
            bind = false;
        }
        if (isHovered(mouseX, mouseY) && mouseButton == 0) {
            bind = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (bind) {
            if (keyCode == 261) {
                option.set(0);
                bind = false;
                return;
            }
            option.set(keyCode);
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
