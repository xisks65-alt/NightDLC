package dev.wh1tew1ndows.client.gui;


import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.gui.component.impl.ColorComponent;
import dev.wh1tew1ndows.client.gui.component.impl.ModuleComponent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.AnimationMath;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.ScaleMath;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.draw.Scissor;
import dev.wh1tew1ndows.client.utils.render.draw.StencilUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.wh1tew1ndows.client.gui.component.impl.ModuleComponent.binding;

public class ClickGui extends Screen implements dev.wh1tew1ndows.client.api.interfaces.IMinecraft {

    private boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public ClickGui() {
        super(new StringTextComponent("GUI"));
    }

    @Getter
    private static final Animation openanim = new Animation();
    double xPanel, yPanel;
    Category current = Category.COMBAT;

    float animation;
    boolean anims = false;

    public ArrayList<ModuleComponent> objects = new ArrayList<>();

    public float scroll = 0;
    public float animateScroll = 0;

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll += delta * 15;
        ColorComponent.opened = null;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        mc.displayScreen(null);
        if (typing || !searchText.isEmpty()) {
            typing = false;
            searchText = "";
        }
        if (configTyping || !configName.isEmpty()) {
            configTyping = false;
            configName = "";
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    boolean searchOpened;
    float seacrh;

    private String searchText = "";
    public static boolean typing;
    Animation anim = new Animation();

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        float scale = 2f;
        float width = 1000 / scale;
        float height = 650 / scale;
        float leftPanel = 125 / scale;
        float x = (mc.getMainWindow().getScaledWidth() - width) / 2f;
        float y = (mc.getMainWindow().getScaledHeight() - height) / 2f;
        xPanel = x;
        yPanel = y;
        anim.update();
        anim.run(anims ? 1 : 0, 1, Easings.SINE_OUT);

        animation = AnimationMath.fast(animation, 0, 5);

        Vector2f scaledMouse = ScaleMath.getMouse(mouseX, mouseY);
        mouseX = (int) scaledMouse.x;
        mouseY = (int) scaledMouse.y;

        int finalMouseX = mouseX;
        int finalMouseY = mouseY;

        mc.gameRenderer.setupOverlayRendering(2);

        openanim.update();
        if (openanim.get() < 0.1) {
            closeScreen();
        }

        GlStateManager.pushMatrix();

        renderBackground(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);

        StencilUtil.enable();
        RenderUtil.Rounded.smooth(matrixStack, x + width - 5 - 1, y + 3, 2.5F + 2, height - 6, new Color(0x1A1C20).getRGB(), Round.of(1.5F));
        StencilUtil.read(1);

        RenderUtil.Rounded.smooth(matrixStack, x + width - 5, y + 3, 2.5F, height - 6, ColorUtil.replAlpha(new Color(0x1A1C20).getRGB(), (int) (255 * openanim.get())), Round.of(1.5F));

        float yOffset = MathHelper.clamp(y + 4 - animateScroll, y, height + 72);

        RenderUtil.Rounded.smooth(matrixStack, x + width - 5, yOffset, 2.5F, 24, ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.7F), (int) (255 * openanim.get())), Round.of(1.5F));

        StencilUtil.disable();
        renderCategories(matrixStack, x, y, width, height, leftPanel - 10, finalMouseX, finalMouseY);
        renderComponents(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderColorPicker(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderSearchBar(matrixStack, x + (width) - 80, y - 28, width / 2.7F, height, leftPanel, finalMouseX, finalMouseY);
        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering();
    }

    void renderColorPicker(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        if (ColorComponent.opened != null) {
            ColorComponent.opened.draw(matrixStack, mouseX, mouseY);
        }
    }

    void renderBackground(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        RenderUtil.Rounded.smooth(matrixStack, x, y - 24, width, 25, ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.15F), 255), Round.of(0, 8, 0, 8));
        RenderUtil.Rounded.smooth(matrixStack, x, y, width, height, ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.1F), 255), Round.of(8, 0, 8, 0));
        RenderUtil.Rounded.smooth(matrixStack, x, y - 24, width, 25, ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.15F), 255), Round.of(0, 8, 0, 8));
    }

    void renderCategories(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        float heightCategory = 60;

        for (Category t : Category.values()) {
            float rectX = x + 2 + t.ordinal() * heightCategory;
            float rectY = y - 22;
            float rectWidth = 25 + Fonts.SF_BOLD.getWidth(t.getName(), 8);
            float rectHeight = 20;

            float textCenterX = rectX + 6;
            float textCenterY = rectY + 6;

            int alphapc = (int) (255 * openanim.get());

            Fonts.ICON_DESHUX.draw(matrixStack, t.getIcon(), textCenterX, textCenterY, t == current ?
                    ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 1), alphapc) :
                    ColorUtil.getColor(90, alphapc), 10);

            Fonts.SF_BOLD.draw(matrixStack, t.getName(), textCenterX + 13, textCenterY,
                    t == current ? ColorUtil.getColor(235, alphapc) : ColorUtil.getColor(180, alphapc), 8);
        }
    }

    void renderComponents(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, width, height - 1);
        drawComponents(matrixStack, mouseX, mouseY);
        Scissor.unset();
        Scissor.pop();
    }

    void renderSearchBar(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        seacrh = AnimationMath.lerp(seacrh, 1, 15);
        matrixStack.push();
        float xOffset = 0;
        float fontTextWidth = Fonts.SF_BOLD.getWidth(searchText, 7);
        if (fontTextWidth > (width - leftPanel - 32 - (64 / 2f) / 2f) * (seacrh)) {
            xOffset = fontTextWidth - ((width - leftPanel - 32 - (64 / 2f) / 2f) * (seacrh));
        }
        StencilUtil.enable();
        RenderUtil.Rounded.drawRoundedRect(matrixStack, x, y + 7, (width - leftPanel - 32 - (64 / 2f) / 2f) * (seacrh), 64 / 2f - 14, 1, -1);
        StencilUtil.read(1);
        Fonts.SF_BOLD.draw(matrixStack, searchText + (typing ? System.currentTimeMillis() % 1000 > 500 ? "_" : "" : "Поиск"), x + 5, y + 12.1F, ColorUtil.getColor(220), 8);
        StencilUtil.disable();
        matrixStack.pop();
    }

    private String configName = "";
    private boolean configTyping;
    public static String confign;

    void drawComponents(MatrixStack stack, int mouseX, int mouseY) {
        if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
            for (Module function : Zetrix.inst().moduleManager().values()) {
                objects.add(new ModuleComponent(function));
            }
        }

        List<ModuleComponent> moduleComponentList = objects.stream()
                .filter(moduleObject -> {
                    if (!searchText.isEmpty()) return true;
                    return moduleObject.function.getCategory() == current;
                }).collect(Collectors.toList());

        List<ModuleComponent> first = moduleComponentList.stream().filter(moduleObject -> objects.indexOf(moduleObject) % 2 == 0).collect(Collectors.toList());
        List<ModuleComponent> second = moduleComponentList.stream().filter(moduleObject -> objects.indexOf(moduleObject) % 2 != 0).collect(Collectors.toList());

        float scale = 2f;
        animateScroll = AnimationMath.fast(animateScroll, scroll, 5);
        float width = 1000 / scale;
        float height = 650 / scale;
        float leftPanel = 125 / scale;
        float x = (mc.getMainWindow().getScaledWidth() - width) / 2f;
        float y = (mc.getMainWindow().getScaledHeight() - height) / 2f;

        float offset = (float) (yPanel + (64 / 2f) - 22) + animateScroll;
        float size1 = 0;
        for (ModuleComponent component : first) {
            if (searchText.isEmpty()) {
                if (component.function.getCategory() != current) continue;
            } else {
                if (!component.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            component.parent = this;
            component.setPosition((float) (xPanel + 8), offset, 237, 25);
            component.drawComponent(stack, mouseX, mouseY);
            if (!component.components.isEmpty()) {
                for (dev.wh1tew1ndows.client.gui.component.impl.Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.getVisible().get()) {
                        offset += settingComp.height;
                        size1 += settingComp.height;
                    }
                }
            }
            offset += component.height + 8;
            size1 += component.height + 8;
        }
        offset = (float) (yPanel + (64 / 2f) - 22) + animateScroll;
        for (ModuleComponent component : second) {
            if (searchText.isEmpty()) {
                if (component.function.getCategory() != current) continue;
            } else {
                if (!component.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            component.parent = this;
            component.setPosition((float) (xPanel + (250 + 2)), offset, 237, 25);
            component.drawComponent(stack, mouseX, mouseY);
            if (!component.components.isEmpty()) {
                for (dev.wh1tew1ndows.client.gui.component.impl.Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.getVisible().get()) {
                        offset += settingComp.height;
                        size1 += settingComp.height;
                    }
                }
            }
            offset += component.height + 8;
            size1 += component.height + 8;
        }

        float max = Math.max(size1, 0);
        if (max < height) {
            scroll = 0;
        } else {
            scroll = MathHelper.clamp(scroll, -(max - height + 50), 0);
        }
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        openanim.run(1, 0.1F, Easings.SINE_OUT);
        super.init(minecraft, width, height);
        if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
            for (Module function : Zetrix.inst().moduleManager().values()) {
                objects.add(new ModuleComponent(function));
            }
        }
        ColorComponent.opened = null;
        typing = false;
        configTyping = false;
        configOpened = false;
        configName = "";
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2f scaledMouse = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = scaledMouse.x;
        mouseY = scaledMouse.y;
        if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
            for (Module function : Zetrix.inst().moduleManager().values()) {
                objects.add(new ModuleComponent(function));
            }
        }
        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        if (ColorComponent.opened != null) {
            ColorComponent.opened.unclick((int) mouseX, (int) mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            openanim.run(0, 0.1F, Easings.SINE_OUT);
            this.minecraft.keyboardListener.enableRepeatEvents(false);
            return false;
        }
        boolean ctrlDown = Screen.hasControlDown();
        if (typing) {
            if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
                String pasteText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
                searchText += pasteText;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) searchText = "";
            if (keyCode == GLFW.GLFW_KEY_ENTER) typing = false;
        }
        if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
            for (Module function : Zetrix.inst().moduleManager().values()) {
                objects.add(new ModuleComponent(function));
            }
        }
        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.keyTyped(keyCode, scanCode, modifiers);
        }
        if (binding != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) binding.function.setKey(0);
            else binding.function.setKey(keyCode);
            binding = null;
        }
        if (configTyping) {
            if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
                String pasteText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
                configName += pasteText;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!configName.isEmpty()) configName = configName.substring(0, configName.length() - 1);
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) configName = "";
            if (keyCode == GLFW.GLFW_KEY_ENTER) configTyping = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) searchText += codePoint;
        if (configTyping) configName += codePoint;
        if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
            for (Module function : Zetrix.inst().moduleManager().values()) {
                objects.add(new ModuleComponent(function));
            }
        }
        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.function.getCategory() != current) continue;
            } else {
                if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean configOpened;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2f scaledMouse = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = scaledMouse.x;
        mouseY = scaledMouse.y;

        float scale = 2f;
        float width = 1000 / scale;
        float height = 650 / scale;
        float leftPanel = 125 / scale;
        float x = (mc.getMainWindow().getScaledWidth() - width) / 2f;
        float y = (mc.getMainWindow().getScaledHeight() - height) / 2f;

        if (ColorComponent.opened != null) {
            if (!ColorComponent.opened.click((int) mouseX, (int) mouseY))
                return super.mouseClicked(mouseX, mouseY, button);
        }

        for (Category t : Category.values()) {
            float rectX = x + 2 + t.ordinal() * 60;
            float rectY = y - 22;
            float rectWidth = 25 + Fonts.SF_BOLD.getWidth(t.getName(), 8);
            float rectHeight = 20;
            if (isHover(mouseX, mouseY, rectX, rectY, rectWidth, rectHeight)) {
                if (current == t) continue;
                current = t;
                animation = 1;
                anims = true;
                scroll = 0;
                searchText = "";
                ColorComponent.opened = null;
                typing = false;
            }
        }

        if (isHover(mouseX, mouseY, x, y + 8, width, height - 64 / 2f)) {
            if (objects.isEmpty() && Zetrix.inst().moduleManager() != null) {
                for (Module function : Zetrix.inst().moduleManager().values()) {
                    objects.add(new ModuleComponent(function));
                }
            }
            for (ModuleComponent m : objects) {
                if (searchText.isEmpty()) {
                    if (m.function.getCategory() != current) continue;
                } else {
                    if (!m.function.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }

        if (isHover(mouseX, mouseY, x + leftPanel + 15, y + 64 / 2F + 15, width - leftPanel - 35 * 2 + 3, 32 / 2f)) {
            configTyping = !configTyping;
        }

        float searchBarX = x + (width) - 80;
        float searchBarY = y - 28 + 7;
        float searchBarWidth = (width - leftPanel - 32 - (64 / 2f) / 2f) * (seacrh);
        float searchBarHeight = 64 / 2f - 14;

        if (isHover(mouseX, mouseY, searchBarX, searchBarY, searchBarWidth, searchBarHeight)) {
            typing = !typing;
        } else {
            typing = false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
