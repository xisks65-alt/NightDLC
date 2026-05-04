package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.alt.Scissor;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.SoundUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;

public class SliderSettingComponent extends SettingComponent {
    private final SliderSetting value;
    private final SliderSetting localValue;

    public SliderSettingComponent(SliderSetting value) {
        super(value);
        this.value = this.localValue = value;
    }

    private boolean drag;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {
    }
    private long lastSoundTime = 0L;
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        value.getAnimation().update();

        float sliderHeight = 2.5F;

        String currentValue = String.valueOf(localValue.getValue());


        float valueHeight = 0;

        value.getAnimation().run(Mathf.step(size.x * (localValue.getValue() - value.min) / (value.max - value.min), value.increment / size.x), 0.25, Easings.CUBIC_OUT, true);

        Scissor.push();
        Scissor.setFromComponentCoordinates((int) (position.x + margin / 2), (int) (position.y + margin), (int) (size.x - Fonts.MONTSERRAT_MEDIUM.getWidth(currentValue, 7) - 10), 10);
        Fonts.MONTSERRAT_MEDIUM.draw(matrix, value.getName(), position.x + margin / 2, position.y + margin + 1.7F, ColorUtil.replAlpha(getWhite(), alpha()), 7);
        Scissor.unset();
        Scissor.pop();


        int backColorDark = ColorUtil.multDark(accentColor(), 0.5F);
        int backColor = ColorUtil.overCol(backColorDark, accentColor(), value.getAnimation().get());

        float trackY = position.y + margin + margin + margin / 2 + margin + margin + valueHeight + margin;
        float animValue = (float) value.getAnimation().getValue();
        float filledFrac = size.x > 0 ? animValue / size.x : 0;

        RenderUtil.Rounded.smooth(matrix, position.x, trackY, size.x, sliderHeight, backColor(), Round.of(sliderHeight / 2F));
        // свечение заполненной части слайдера
        if (filledFrac > 0.01f) {
            RenderUtil.Shadow.drawShadow(matrix, position.x, trackY, animValue, sliderHeight, 3,
                    ColorUtil.replAlpha(accentColor(), (int)(alpha() * 0.25f)));
        }
        RenderUtil.Rounded.smooth(matrix, position.x, trackY, animValue, sliderHeight, backColor, backColor, backColorDark, backColorDark, Round.of(sliderHeight / 2F));
        float circleSize = drag ? 6 : 4;
        float knobX = position.x + animValue - (circleSize / 2F);
        float knobY = position.y + margin / 2 + margin + margin + margin + margin + valueHeight + margin - (circleSize / 2F) + (sliderHeight / 2F);
        // свечение под кружком
        RenderUtil.Shadow.drawShadow(matrix, knobX, knobY, circleSize, circleSize, 4,
                ColorUtil.replAlpha(accentColor(), (int)(alpha() * 0.4f)));
        RenderUtil.Rounded.smooth(matrix, knobX, knobY, circleSize, circleSize, getWhite(), Round.of((circleSize / 2F)));

        if (drag) {
            long currentTime = System.currentTimeMillis();

            boolean fff = false;
            /// gpscode!
            if (currentTime - lastSoundTime >= 300 && fff) {
                SoundUtil.playSound("slider.WAV");
                lastSoundTime = currentTime;
            }

            localValue.set((float) MathHelper.clamp(
                    Mathf.step((mouseX - position.x) / size.x * (value.max - value.min) + value.min, value.increment),
                    value.min, value.max
            ));
        }

        float badgeX = position.x + size.x - margin - Fonts.MONTSERRAT_MEDIUM.getWidth(currentValue, 7) - 3;
        float badgeW = 6 + Fonts.MONTSERRAT_MEDIUM.getWidth(currentValue, 7);
        RenderUtil.Shadow.drawShadow(matrix, badgeX, position.y + margin, badgeW, 11, 3,
                ColorUtil.replAlpha(accentColor(), (int)(alpha() * 0.15f)));
        RenderUtil.Rounded.smooth(matrix, badgeX, position.y + margin, badgeW, 11, ColorUtil.multAlpha(accentColor(), 0.15F * alphaPC()), Round.of(3.5F));
        RenderUtil.Rounded.roundedOutline(matrix, badgeX, position.y + margin, badgeW, 11, 1, ColorUtil.multAlpha(accentColor(), 0.25F * alphaPC()), Round.of(3.5F));
        //  Fonts.MONTSERRAT_MEDIUMdraw(matrix, String.valueOf(value.min), position.x, position.y + margin + valueHeight + margin + sliderHeight + margin, ColorUtil.multDark(getWhite(), 0.75F), fontSize);
        Fonts.MONTSERRAT_MEDIUM.drawRight(matrix, currentValue, position.x + size.x - margin, position.y + margin + 2.1F, ColorUtil.replAlpha(getWhite(), alpha()), 7);
        // font.drawRight(matrix, String.valueOf(value.max), position.x + size.x, position.y + margin + valueHeight + margin + sliderHeight + margin, ColorUtil.multDark(getWhite(), 0.75F), fontSize);


        size.y = margin + valueHeight + margin + sliderHeight + margin + fontSize + margin * 2  - 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY, position.x , position.y + margin + margin + margin + margin + margin , size.x, size.y - (margin * 6))) {
            drag = true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        drag = false;
        value.set(localValue.getValue());
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public void onClose() {
        drag = false;
        value.set(localValue.getValue());
    }

}