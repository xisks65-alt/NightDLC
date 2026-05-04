package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.alt.Scissor;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

    public class BooleanSettingComponent extends SettingComponent {
    private final BooleanSetting value;

    public BooleanSettingComponent(BooleanSetting value) {
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
        value.getAnimation().update();

        float checkboxSize = 11; // сам квадратик
        float padding = 1;       // отступ между текстом и чекбоксом

        // высота текста
        float valueHeight = 5;
        Scissor.push();
        Scissor.setFromComponentCoordinates((int) (position.x + margin / 2), (int) (position.y + 1), (int) (size.x - 16), 10);
        Fonts.MONTSERRAT_MEDIUM.draw(matrix, value.getName(), position.x + margin / 2, position.y + 3.5F, ColorUtil.replAlpha(getWhite(), alpha()), 7);
        Scissor.unset();
        Scissor.pop();

        value.getAnimation().run(value.getValue() ? 1 : 0, 0.25, Easings.SINE_OUT, true);

        int backColorDark = ColorUtil.multDark(accentColor(), 0.1F);
        int backColor = ColorUtil.overCol(backColorDark, accentColor(), value.getAnimation().get());

        // Координаты чекбокса (по правому краю, выровнен по центру текста)
        float checkboxX = position.x + size.x - checkboxSize - padding;
        float checkboxY = position.y + 2 + margin + (valueHeight / 2f) - (checkboxSize / 2f);

        // рисуем фон чекбокса
        RenderUtil.Rounded.smooth(matrix, checkboxX, checkboxY, checkboxSize, checkboxSize, ColorUtil.multAlpha(accentColor(), 0.1F), Round.of(3));
        RenderUtil.Rounded.roundedOutline(matrix, checkboxX, checkboxY, checkboxSize, checkboxSize, 1, ColorUtil.multAlpha(accentColor(), 0.2F), Round.of(3));

        // рисуем галочку (иконку), тоже по центру квадрата
        Fonts.ICON_DESHUX.draw(
                matrix,
                "n",
                checkboxX + (checkboxSize / 2f) - 3.85F, // 4 = половина иконки (примерно)
                checkboxY + (checkboxSize / 2f) - 3,
                ColorUtil.overCol(ColorUtil.getColor(0, 0), accentColor(), value.getAnimation().get()),
                8
        );

        Fonts.MONTSERRAT_MEDIUM.draw(
                matrix,
                "×",
                checkboxX + (checkboxSize / 2f) - 3.3F, // 4 = половина иконки (примерно)
                checkboxY + (checkboxSize / 2f) - 6.65F,
                ColorUtil.overCol(accentColor(), ColorUtil.getColor(0, 0), value.getAnimation().get()),
                12
        );


        size.y = margin + valueHeight - 0.5F + margin + margin;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float checkboxWidth = 12;
        float checkboxSize = 6;

        if (isLClick(button) && isHover(mouseX, mouseY, position.x + size.x - checkboxWidth, position.y + 1 + margin, checkboxWidth, 10)) {
            value.set(!value.getValue());
        }
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