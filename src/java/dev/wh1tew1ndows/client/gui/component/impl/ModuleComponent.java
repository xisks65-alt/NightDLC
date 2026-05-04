package dev.wh1tew1ndows.client.gui.component.impl;

import dev.wh1tew1ndows.client.gui.ClickGui;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.*;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ModuleComponent extends Component {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public Module function;

    public List<Component> components = new ArrayList<>();

    public ModuleComponent(Module function) {
        this.function = function;
        for (Setting<?> setting : function.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof MultiBooleanSetting mode) {
                components.add(new ListComponent(mode));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
    }

    public float animationToggle;
    public static ModuleComponent binding;


    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

        float totalHeight = 2;
        for (Component component : components) {
            if (component.setting != null && component.setting.getVisible().get()) {
                totalHeight += component.height;
            }
        }

        float off = 2f;

        components.forEach(c -> {
            c.function = function;
            c.parent = parent;
        });

        animationToggle = Interpolator.lerp(animationToggle, function.isEnabled() ? 1 : 0, 0.2f);


        RenderUtil.Rounded.smooth(matrixStack, x, y, width, height + totalHeight, new Color(0x4D1A1C20, true).getRGB(), Round.of(5));
        RenderUtil.Rounded.smooth(matrixStack, x, y, width, 20, new Color(0x4D1F2127, true).getRGB(), Round.of(0, 5, 0, 5));

        int colors = ColorUtil.overCol(new Color(0x662D2D35, true).getRGB(), ColorUtil.multDark(ColorUtil.fade(), 0.6F), animationToggle);

        RenderUtil.Rounded.smooth(matrixStack, x + width - 20, y, 20, 20, colors, Round.of(0, 0, 0, 5));

        RenderUtil.Rounded.smooth(matrixStack, x + width - (10 + (3 / 2)), y + (8.25F), 3, 3, -1, Round.of(1.5F));


        // RenderUtil.Rounded.smooth(matrixStack,x + 5, y + 20, width - 10, 1, ColorUtil.replAlpha(new Color(45, 45, 45).getRGB(),(int) (135 )), Round.of(1));
        Fonts.SF_BOLD.draw(matrixStack, function.getName(), x + 6, y + 6.5F, ColorUtil.getColor(220), 7.5F);

        String key = Keyboard.keyName(function.getKey());

        colors = ColorUtil.overCol(new Color(0x6F373742, true).getRGB(), ColorUtil.multDark(ColorUtil.fade(), 0.5F), animationToggle);


        RenderUtil.Rounded.smooth(matrixStack, x + width - 34.5F - Fonts.SF_BOLD.getWidth(function.getKey() == 0 ? "bind = null" : "bind = " + key, 7) + 5, y, 10 + Fonts.SF_BOLD.getWidth(function.getKey() == 0 ? "bind = null" : "bind = " + key, 7),
                20, colors, Round.of(0, 0, 0, 0));
        Fonts.SF_BOLD.drawCenter(matrixStack, function.getKey() == 0 ? "bind = null" : "bind = " + key, x + width - 34.5F - Fonts.SF_BOLD.getWidth(function.getKey() == 0 ? "bind = null" : "bind = " + key, 7) + 5 + (10 + Fonts.SF_BOLD.getWidth(function.getKey() == 0 ? "bind = null" : "bind = " + key, 7)) / 2, y + 6.5F, ColorUtil.getColor(220), 7);
        //}

        //    int color = ColorUtil.interpolateColor(ColorUtil.getColor(25), ColorUtil.multDark(ColorUtil.fade(), 0.6F), animationToggle);

        // RenderUtil.Rounded.smooth(matrixStack,x + 5, y + 23 + off, 10, 10,  color, Round.of(3));
        // Scissor.push();

        // Scissor.setFromComponentCoordinates(x + 5, y + 23 + off, 10 * animationToggle, 10);
        // Fonts.icons[12].drawString(matrixStack, "A", x + 7, y + 28 + off, -1);
        // Scissor.unset();
        // Scissor.pop();

        //  client.util.screenUtil.normalFont.Fonts.SF_SEMIBOLD.draw(matrixStack, "Включен", x + 18f, y + 22 + 5 - 2 + off, ColorUtil.getColor(90),7);

        float offsetY = 0;
        for (Component component : components) {
            if (component.setting != null && component.setting.getVisible().get()) {
                component.setPosition(x, y + height + offsetY, width, 20);
                component.drawComponent(matrixStack, mouseX, mouseY);
                offsetY += component.height;
            }
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHover(mouseX, mouseY, x, y, width - 10, 20) && mouseButton <= 1) {
            function.toggle();
        }

        if (binding == this && mouseButton > 2) {
            function.setKey(-100 + mouseButton);
            binding = null;
        }

        if (isHover(mouseX, mouseY, x, y, width - 10, 20)) {
            if (mouseButton == 2) {
                ClickGui.typing = false;
                binding = this;
            }
        }
        components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        components.forEach(component -> component.keyTyped(keyCode, scanCode, modifiers));
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        components.forEach(component -> component.charTyped(codePoint, modifiers));
    }
}
