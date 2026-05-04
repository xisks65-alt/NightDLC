package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.module.settings.impl.StringSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.text.TextAlign;
import dev.wh1tew1ndows.client.utils.render.text.TextBox;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;

public class StringSettingComponent extends SettingComponent {
    private final StringSetting value;
    private final StringSetting localValue;  // Промежуточное значение
    public final TextBox textBox;

    public StringSettingComponent(StringSetting value) {
        super(value);
        this.value = this.localValue = value;
        textBox = new TextBox(new Vector2f(), Fonts.MONTSERRAT_MEDIUM, fontSize(), ColorUtil.getColor(255, 255, 255), TextAlign.LEFT, "Введите что-нибудь..", 0, false, value.isOnlyNumber());
        textBox.setText(localValue.getValue());
        textBox.setCursor(localValue.getValue().length());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
    }

    @Override
    public void init() {
        textBox.setText(localValue.getValue());
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        float valueHeight = drawName(matrix, mouseX, mouseY, size.x);
        RenderUtil.Rounded.smooth(matrix, position.x, position.y + margin + valueHeight + margin, size.x, margin + fontSize + margin, backColor(), Round.of(2));

        textBox.setColor(getWhite());
        textBox.getPosition().set(position.x + margin, position.y + margin + valueHeight + margin + margin);
        textBox.setWidth(size.x - (margin * 2));
        textBox.draw(matrix);
        localValue.set(textBox.getText());

        size.y = margin + valueHeight + margin + margin + fontSize + margin + margin + margin - 1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isLClick(button))
            textBox.mouse(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        textBox.keyPressed(keyCode);

        if (Keyboard.KEY_ENTER.isKey(keyCode)) {
            value.set(localValue.getValue());
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        textBox.charTyped(codePoint);
        return false;
    }

    @Override
    public void onClose() {
        textBox.selected = false;
        value.set(localValue.getValue());
    }
}