package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

public class DelimiterSettingComponent extends SettingComponent {
    private final DelimiterSetting value;

    public DelimiterSettingComponent(DelimiterSetting value) {
        super(value);
        this.value = value;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        float lineWidth = 5;

//        font.draw(matrix, valueName, position.x + lineWidth + margin, position.y + margin, getWhite(), 8);

        float valueHeight = drawName(matrix, mouseX, mouseY, size.x - margin - lineWidth - margin);


        float textWidth = Fonts.MONTSERRAT_MEDIUM.getWidth(value.getName(), 8);
        float line = 1.5F;

        boolean lineCheck = textWidth > (size.x - margin - lineWidth - margin);


        RenderUtil.Rounded.smooth(matrix, position.x + size.x - margin - (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)), position.y + (margin + 8 + margin) / 2F + 0.07F, (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)) + margin, line, backgroundColorS(), Round.of(0.5F));

        //   RectUtil.drawRect(matrix, position.x + size.x - margin - (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)), position.y + (margin + 8 + margin) / 2F - line / 2F, (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)) + margin, line, accentColor());

        size.y = margin + valueHeight + margin / 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
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

    }
}
