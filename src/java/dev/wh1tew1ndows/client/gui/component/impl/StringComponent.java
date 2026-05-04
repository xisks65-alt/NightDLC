package dev.wh1tew1ndows.client.gui.component.impl;

import dev.wh1tew1ndows.client.managers.module.settings.impl.StringSetting;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.text.TextAlign;
import dev.wh1tew1ndows.client.utils.render.text.TextBox;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;

import java.awt.*;

public class StringComponent extends Component {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public StringSetting option;
    private final StringSetting localValue;  // Промежуточное значение
    public final TextBox textBox;

    public StringComponent(StringSetting option) {
        this.option = option;
        this.setting = option;
        this.localValue = option;

        // Инициализация TextBox с параметрами, подходящими для gui стиля
        textBox = new TextBox(
                new Vector2f(),
                Fonts.SF_BOLD,
                7f,
                ColorUtil.getColor(220),
                TextAlign.LEFT,
                "Введите что-нибудь..",
                0,
                false,
                option.isOnlyNumber()
        );
        textBox.setText(localValue.getValue());
        textBox.setCursor(localValue.getValue().length());
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 30; // Уменьшаем высоту компонента

        // Рисуем название настройки
        Fonts.SF_BOLD.draw(matrixStack, option.getName(), x + 6, y + 1.5F, ColorUtil.getColor(220), 7);

        // Рисуем фон для текстового поля
        RenderUtil.Rounded.smooth(matrixStack, x + 6, y + 10, width - 12, 12, new Color(0x292932).getRGB(), Round.of(2));

        // Настраиваем и рисуем TextBox
        textBox.setColor(ColorUtil.getColor(220));
        textBox.getPosition().set(x + 8, y + 12.5F);
        textBox.setWidth(width - 16);
        textBox.draw(matrixStack);

        // Обновляем промежуточное значение
        localValue.set(textBox.getText());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHover(mouseX, mouseY, x + 6, y + 10, width - 12, 12)) {
            // Обрабатываем клик по текстовому полю
            textBox.mouse(mouseX, mouseY, mouseButton);
        } else {
            // Клик вне текстового поля - сбрасываем фокус
            if (textBox.selected) {
                textBox.selected = false;
                option.set(localValue.getValue());
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // Не требуется для текстового поля
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        // Обрабатываем нажатия клавиш
        textBox.keyPressed(keyCode);

        // При нажатии Enter сохраняем значение
        if (Keyboard.KEY_ENTER.isKey(keyCode)) {
            option.set(localValue.getValue());
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        // Обрабатываем ввод символов
        textBox.charTyped(codePoint);
    }

    /**
     * Метод для закрытия компонента (сброс фокуса и сохранение значения)
     */
    public void onClose() {
        textBox.selected = false;
        option.set(localValue.getValue());
    }
}
