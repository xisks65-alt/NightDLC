package dev.wh1tew1ndows.client.screen.clickgui.component.module;

import dev.wh1tew1ndows.client.api.annotations.Beta;
import dev.wh1tew1ndows.client.api.annotations.Client;
import dev.wh1tew1ndows.client.api.annotations.Funtime;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.*;
import dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen;
import dev.wh1tew1ndows.client.screen.clickgui.component.WindowComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl.*;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.draw.StencilUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class ModuleComponent extends WindowComponent {
    private static final float APPEND = 1.5F;

    private final Module module;
    public List<SettingComponent> settingComponents = new ArrayList<>();
    private boolean expanded = false;
    private float settingHeight = 8;
    private final float margin = 5;
    private final Script script = new Script();

    private final Random random = new FastRandom();


    public ModuleComponent(Module module, ClickGuiScreen clickGui) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BindSetting value) {
                settingComponents.add(new BindSettingComponent(value));
            } else if (setting instanceof BooleanSetting value) {
                settingComponents.add(new BooleanSettingComponent(value));
            } else if (setting instanceof ColorSetting value) {
                settingComponents.add(new ColorSettingComponent(value));
            } else if (setting instanceof DelimiterSetting value) {
                settingComponents.add(new DelimiterSettingComponent(value));
            } else if (setting instanceof ListSetting<?> value) {
                settingComponents.add(new ListSettingComponent(value));
            } else if (setting instanceof ModeSetting value) {
                settingComponents.add(new ModeSettingComponent(value));
            } else if (setting instanceof MultiBooleanSetting value) {
                settingComponents.add(new MultiBooleanSettingComponent(value));
            } else if (setting instanceof SliderSetting value) {
                settingComponents.add(new SliderSettingComponent(value));
            } else if (setting instanceof StringSetting value) {
                settingComponents.add(new StringSettingComponent(value));
            }
        }
        size.set(clickGui.categoryWidth(), clickGui.categoryHeight());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        settingComponents.forEach(component -> component.resize(minecraft, width, height));
    }

    @Override
    public void init() {
        settingComponents.forEach(SettingComponent::init);
        if (expanded && panel().getExpandedModule() != this) {
            expandAnimation.set(0F);
            expanded = false;
        }
    }


    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        // апдейты
        script.update();
        expandAnimation.update();
        hoverAnimation.update();
        module.getAnimation().update();

        // локальные кэши для минимизации вызовов и аллокаций
        clickgui();
        final float alpha = ClickGuiScreen.alpha.get();
        final float expand = expandAnimation.get();
        final boolean hasSettings = !settingComponents.isEmpty();


        final int themeColor = InterFace.getInstance().themeColor();


        if (expanded && panel().getExpandedModule() != this) {
            expandAnimation.run(0, 0.25, Easings.BACK_OUT, true).onFinished(() -> expanded = false);
        }


        final boolean isHover = isHover(mouseX, mouseY, position.x + APPEND, position.y + APPEND, size.x - (APPEND * 2F), size.y - (APPEND * 2F));
        final boolean bindingActive = binding && !script.isFinished();


        hoverAnimation.run(bindingActive ? 1.5 : (isHover ? 1 : 0), 0.25, bindingActive ? Easings.BACK_OUT : Easings.QUAD_OUT, true);
        final float hoverVal = hoverAnimation.get();


        RenderUtil.Rounded.smooth(
                matrix,
                position.x + APPEND, position.y + APPEND,
                size.x - (APPEND * 2F), size.y - (APPEND * 2F),
                ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.035F * alpha),
                Round.of(4)
        );

        // подсветка при наведении — цвет темы
        if (hoverVal > 0.01f) {
            RenderUtil.Rounded.smooth(
                    matrix,
                    position.x + APPEND, position.y + APPEND,
                    size.x - (APPEND * 2F), size.y - (APPEND * 2F),
                    ColorUtil.multAlpha(InterFace.getInstance().themeColor(),
                            (float) Math.min(hoverVal, 1.0) * 0.08F * alpha),
                    Round.of(4)
            );
        }

        RenderUtil.Rounded.roundedOutline(
                matrix,
                position.x + APPEND, position.y + APPEND,
                size.x - (APPEND * 2F), size.y - (APPEND * 2F), 1, ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.045F * alpha),
                Round.of(4)
        );

        // обводка цветом темы когда включён
        if (module.isEnabled()) {
            int enabledOutline = ColorUtil.multBright(InterFace.getInstance().themeColor(), 0.7F);
            RenderUtil.Rounded.roundedOutline(
                    matrix,
                    position.x + APPEND, position.y + APPEND,
                    size.x - (APPEND * 2F), size.y - (APPEND * 2F), 1,
                    ColorUtil.multAlpha(enabledOutline, (float)(0.55F * alpha * module.getAnimation().get())),
                    Round.of(4)
            );
        }


        int clientcolors = ColorUtil.overCol(ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.035F * alpha), ColorUtil.multAlpha(InterFace.getInstance().themeColor(), alpha), module.getAnimation().get());
        RenderUtil.Rounded.smooth(
                matrix,
                position.x + APPEND, position.y + APPEND,
                size.x - (APPEND * 2F), size.y - (APPEND * 2F),
                ColorUtil.multAlpha(clientcolors, 0.1F * alpha),
                Round.of(4)
        );


        boolean noneMatch = panel().getCategoryComponents()
                .stream()
                .noneMatch(category -> category.getModuleComponents()
                        .stream()
                        .anyMatch(m -> m.isBinding() || m.settingComponents
                                .stream()
                                .anyMatch(sc -> sc instanceof StringSettingComponent s && s.textBox.selected)
                        )
                );


        if (hasSettings) {
            Fonts.MONTSERRAT_MEDIUM.drawRight(matrix, "...",
                    position.x + size.x - 7,
                    position.y + (size.y / 2F) - (7.5F / 2F) - 2.7F,
                    ColorUtil.replAlpha(ColorUtil.getColor(200), alphaPC()), 8);

        }
        String moduleText = binding
                ? "Binding | " + Keyboard.keyName(module.getKey())
                : (Keyboard.KEY_RIGHT_CONTROL.isKeyDown() && noneMatch)
                ? Keyboard.keyName(module.getKey()) + " | " + module.getName()
                : module.getName();


        Fonts.MONTSERRAT_MEDIUM.draw(matrix, moduleText,
                position.x + 7,
                position.y + (size.y / 2F) - (7 / 2F) - 0.15F,
                ColorUtil.replAlpha(ColorUtil.getColor(200), alphaPC()), 7);

        float badgeX = position.x + 7 + (Fonts.MONTSERRAT_MEDIUM.getWidth(moduleText, 7)) + 1F;
        float badgeY = position.y + (size.y / 2F) - (8 / 2F);
        int badgeCol = ColorUtil.multAlpha(accentColor(), 0.5F);
        Class<?> clazz = module.getClass();
        float x = position.x + size.x - 7, y = position.y + (size.y / 2F) - (7 / 2F) - 0.15F;
        if (hasSettings)
            x -= 11;


        if (clazz.isAnnotationPresent(Beta.class)) {
            Fonts.MONTSERRAT_MEDIUM.drawRight(matrix, "BETA", x, y, badgeCol, 7);

        }

        if (clazz.isAnnotationPresent(Funtime.class)) {
            String f = ColorFormatting.getColor(ColorUtil.replAlpha(ColorUtil.getColor(200, 76, 76), alpha)) + "F";
            String t = ColorFormatting.getColor(ColorUtil.replAlpha(ColorUtil.getColor(200, 200, 200), alpha)) + "T";
            Fonts.MONTSERRAT_MEDIUM.drawRight(matrix, f + t, x, y, badgeCol, 7);

        }

        if (clazz.isAnnotationPresent(Client.class)) {
            Fonts.MONTSERRAT_MEDIUM.drawRight(matrix, "Client", x, y, badgeCol, 7);
        }

        StencilUtil.enable();
        RectUtil.drawRect(matrix, position.x, position.y + size.y, size.x, expand * settingHeight, ColorUtil.getColor(128, 255));
        StencilUtil.read(1);
        RenderUtil.Rounded.smooth(matrix, position.x + 1F, position.y + size.y,
                size.x - 2F, expand * (settingHeight),
                ColorUtil.replAlpha(InterFace.getInstance().themeColor(), 0.04F * alpha), ColorUtil.replAlpha(InterFace.getInstance().themeColor(), 0.04F * alpha),
                ColorUtil.replAlpha(InterFace.getInstance().themeColor(), 0.02F * alpha), ColorUtil.replAlpha(InterFace.getInstance().themeColor(), 0.02F * alpha), Round.of(4));


        settingHeight = 0F;
        if ((expanded || !expandAnimation.isFinished()) && hasSettings) {
            float offset = 3;
            for (SettingComponent component : settingComponents) {
                if (!component.value().getVisible().get()) continue;
                component.position().set(position.x + margin, position.y + size.y + offset - 2);
                component.size().x = size.x - (margin * 2F);
                component.render(matrix, mouseX, mouseY, partialTicks);
                offset += component.size().y;
            }
            settingHeight = offset;
        }
        StencilUtil.disable();

    }

    @Getter
    @Setter
    private boolean binding = false;


    public void renderDesk(MatrixStack matrix, double mouseX, double mouseY, double ysefs) {

        final boolean isHover = isHover(mouseX, mouseY, position.x + APPEND, position.y + APPEND, size.x - (APPEND * 2F), size.y - (APPEND * 2F));


        if (isHover)
            Fonts.MONTSERRAT_MEDIUM.drawCenter(matrix, ColorFormatting.getColor(ColorUtil.replAlpha(ColorUtil.fade(), 1 * alphaPC())) + module.getName() + ColorFormatting.reset() + " - " + module.getDesc(), mc.getMainWindow().getScaledWidth() / 2, 20, ColorUtil.getColor(225, 1 * alphaPC()), 10);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY, position.x + APPEND, position.y + APPEND, size.x - (APPEND * 2F), size.y - (APPEND * 2F))) {
            if (isMClick(button)) {
                // оставил стрим-очистку биндинга у соседей в этой категории
                panel().getCategoryComponents()
                        .stream()
                        .filter(component -> component.getCategory().equals(module.getCategory()))
                        .flatMap(component -> component.getModuleComponents().stream())
                        .filter(m -> m != this)
                        .forEach(m -> m.setBinding(false));

                setBinding(!isBinding());
            }
            if (!isBinding() && isLClick(button)) module.toggle();
            if (isRClick(button) && !settingComponents.isEmpty()) {
                expanded = !expanded;
                expandAnimation.run(expanded ? 1 : 0, 0.25, Easings.QUART_OUT);
                //    SoundUtil.playSound("gui/moduleopen.wav", 5);
                if (expanded) panel().setExpandedModule(this);
            }
        }

        boolean valid = button != Keyboard.MOUSE_MIDDLE.getKey()
                && button != Keyboard.MOUSE_RIGHT.getKey()
                && button != Keyboard.MOUSE_LEFT.getKey();

        if (isBinding()) {
            if (valid && script.isFinished()) {
                module.setKey(button);
                stopBinding();
            }
        } else {
            setBinding(false);
        }

        if (expanded && !settingComponents.isEmpty() && expandAnimation.get() == 1.0F && expandAnimation.isFinished()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!settingComponents.isEmpty()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding()) {
            if (keyCode == Keyboard.KEY_ESCAPE.getKey() || keyCode == Keyboard.KEY_DELETE.getKey()) {
                module.setKey(Keyboard.KEY_NONE.getKey());
                stopBinding();
                return true;
            }
            if (script.isFinished()) {
                module.setKey(keyCode);
                stopBinding();
            }
        }
        if (expanded && !settingComponents.isEmpty()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return false;
    }

    private void stopBinding() {

        script.cleanup().addStep(50, () -> {


            setBinding(false);
        });
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!expanded || settingComponents.isEmpty()) return false;
        for (SettingComponent component : settingComponents) {
            if (component.value().getVisible().get()) {
                component.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!expanded || settingComponents.isEmpty()) return false;
        for (SettingComponent component : settingComponents) {
            if (component.value().getVisible().get()) {
                component.charTyped(codePoint, modifiers);
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        setBinding(false);
        settingComponents.forEach(SettingComponent::onClose);
    }
}
