package dev.wh1tew1ndows.client.gui.component.impl;


import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.AnimationMath;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.HashMap;

public class ModeComponent extends Component {

    public ModeSetting option;

    public boolean opened;
    public HashMap<String, Float> animation = new HashMap<>();

    public ModeComponent(ModeSetting option) {
        this.option = option;
        for (String s : option.getValues()) {
            animation.put(s, 0f);
        }
        this.setting = option;
    }

    Animation openanim = new Animation();


    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float off = 4;
        float offset = 17 - 12;
        for (String s : option.getValues()) {
            // Добавляем новые ключи если список обновился (например нейро-модели)
            animation.putIfAbsent(s, 0f);
            offset += 9;
        }
        openanim.update();
        openanim.run(opened ? 1 : 0, 0.2F, Easings.SINE_OUT);
        if (!opened) offset *= openanim.get();
        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 7, y, ColorUtil.getColor(220), 7);

        off += 7 / 2f + 2;
        height += offset + 7;

        RenderUtil.Rounded.smooth(matrixStack, x + 5, y + off, width - 10, 20 - 6, new Color(0x292932).getRGB(), Round.of(4));

        RenderUtil.Rounded.smooth(matrixStack, x + 5, y + off + 17, width - 10, offset, ColorUtil.multAlpha(new Color(0x292932).getRGB(), openanim.get()), Round.of(4));
        Fonts.SF_BOLD.draw(matrixStack, option.getValue(), x + 10, y + 20 - 7, ColorUtil.getColor(220), 7);
        if (opened) {
            int i = 1;
            for (String s : option.getValues()) {
                boolean hovered = Mathf.isInRegion(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                animation.put(s, AnimationMath.fast(animation.get(s), hovered ? 2 : 0, 12));
                Fonts.SF_BOLD.draw(matrixStack, s, x + 9 + animation.get(s), y + off + 19.5F + i, option.getValue().equals(s) ? ColorUtil.getColor(220, openanim.get()) : ColorUtil.getColor(120, openanim.get()), 7);
                i += 9;
            }
            height += 3;
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += 12 / 2f + 2;
        if (Mathf.isInRegion(mouseX, mouseY, x + 5, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (String s : option.getValues()) {
            if (Mathf.isInRegion(mouseX, mouseY, x, y + off + 20F + i, width, 8))
                option.set(s);
            i += 9;
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
