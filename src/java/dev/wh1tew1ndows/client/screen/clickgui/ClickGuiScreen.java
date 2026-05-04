package dev.wh1tew1ndows.client.screen.clickgui;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.api.interfaces.IMouse;
import dev.wh1tew1ndows.client.api.interfaces.IWindow;
import dev.wh1tew1ndows.client.screen.clickgui.component.Panel;
import dev.wh1tew1ndows.client.screen.clickgui.component.category.CategoryComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.module.ModuleComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl.StringSettingComponent;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.math.ScaleMath;
import dev.wh1tew1ndows.client.utils.other.SoundUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.text.TextAlign;
import dev.wh1tew1ndows.client.utils.render.text.TextBox;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;

@Getter
@Accessors(fluent = true)
public class ClickGuiScreen extends Screen implements IMinecraft, IWindow, IMouse {
    private boolean exit = false;
    public static final Animation alpha = new Animation();


    private final float categoryWidth = 100, categoryHeight = 23, categoryOffset = 12;
    private final Panel panel = new Panel(this);
    public static long startTime = System.currentTimeMillis();
    private final TextBox searchField = new TextBox(new Vector2f(), Fonts.MONTSERRAT_MEDIUM, 8,
            ColorUtil.getColor(255, 255, 255), TextAlign.CENTER, "Search: Ctrl + F", 0, false, false);


    public ClickGuiScreen() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.panel.resize(minecraft, width, height);
    }

    @Override
    protected void init() {
        super.init();
        SoundUtil.playSound("opengui.wav", 0.35F);
        Zetrix.inst().configManager().set();
        searchField.setText("");
        searchField.setSelected(false);

        alpha.set(0.0);

        alpha.run(1.0, 0.3, Easings.SINE_OUT, false);

        exit = false;


        if (panel != null) {
            panel.init();
        }


    }


    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        draw(matrixStack, finalMouseX, finalMouseY, partialTicks);

    }


    private final boolean lastZvukValue = false;
    private final long lastMusicUpdate = 0;
    private static final long MUSIC_UPDATE_INTERVAL = 50; // мс

    public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        alpha.update();


        this.mouseCheck();
        this.closeCheck();
        ScaleMath.scalePre();

        this.drawPanel(matrixStack, mouseX, mouseY, partialTicks);
        if (isSearching() || isSearchFieldSelected()) {
            renderSearchField(matrixStack);
        }
        ScaleMath.scalePost();
    }

    private void renderSearchField(MatrixStack matrixStack) {
        searchField.setEmptyText("Поиск: Ctrl + F");
        float searchFieldHeight = searchField.getFontSize() * 2F + 6;

        double old_y = 10 - 10 * alpha.get();

        double searchFieldX = (scaled().x) / 24;
        double searchFieldY = (scaled().y / 9) - 50 - old_y;


        float searchWidth = isSearching() ?
                searchField.getFont().getWidth(searchField.getText(), searchField.getFontSize()) + 10F :
                searchField.getFont().getWidth(searchField.getEmptyText(), searchField.getFontSize()) + 10F;

        searchField.position.set(searchFieldX, searchFieldY + ((searchFieldHeight / 2F) - (searchField.getFontSize() / 2F) - 3.6F));
        searchField.setWidth(100);
        searchField.setColor(ColorUtil.getColor(255, alpha.get()));

        RenderUtil.clientStyledRectDark(matrixStack, (float) (searchFieldX - (searchWidth / 2F)) - 2, (float) (searchFieldY - 0.6) - 2, searchWidth + 4, 16 + 4, alpha.get(), 5);
        //  RenderUtil.clientStyledRectDark(matrixStack, (float) (searchFieldX + searchWidth / 2 ), (float) (searchFieldY + 0.7F - 3), 20, 20, alpha.get(), 6);


        searchField.draw(matrixStack);


    }

    private void drawPanel(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        panel.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;

        if (!exit) {
            if (searchField.isSelected()) {
                searchField.setSelected(false);
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (!searchField.isSelected()) {
                panel.mouseClicked(finalMouseX, finalMouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        if (!searchField.isSelected()) {
            panel.mouseReleased(finalMouseX, finalMouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        panel.mouseDragged(mouse.x, mouse.y);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        if (!searchField.isSelected()) {
            return panel.mouseScrolled(finalMouseX, finalMouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!exit) {
            if (searchField.isSelected() && keyCode == Keyboard.KEY_ESCAPE.getKey()) {
                searchField.setSelected(false);
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT_CONTROL.getKey()) && keyCode == Keyboard.KEY_F.getKey()) {
                searchField.setSelected(!searchField.isSelected());
            }
            searchField.keyPressed(keyCode);
            if (!searchField.isSelected()) {
                panel.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        panel.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!exit) {
            searchField.charTyped(codePoint);
            if (!searchField.isSelected()) {
                panel.charTyped(codePoint, modifiers);
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        boolean noneMatch = panel.getCategoryComponents().stream().noneMatch(category -> category.getModuleComponents().stream().anyMatch(ModuleComponent::isBinding));
        if (!exit && alpha.getValue() > 0.0F && noneMatch) {
            alpha.run(0.0, 0.3, Easings.SINE_IN_OUT, false);
            exit = true;
            SoundUtil.playSound("closegui.wav", 0.35F);
            mc.mouseHelper.forceGrabMouse(false);
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Zetrix.inst().configManager().set();
        panel.onClose();
        searchField.setText("");
        searchField.setSelected(false);
    }

    private boolean lastNoneMatch = true;
    private long lastMouseCheck = 0;
    private static final long MOUSE_CHECK_INTERVAL = 16;

    private void mouseCheck() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseCheck < MOUSE_CHECK_INTERVAL) {
            return;
        }
        lastMouseCheck = currentTime;


        boolean noneMatch = true;
        for (CategoryComponent category : panel.getCategoryComponents()) {
            for (ModuleComponent module : category.getModuleComponents()) {
                for (SettingComponent settingComponent : module.settingComponents) {
                    if (settingComponent instanceof StringSettingComponent component && component.textBox.selected) {
                        noneMatch = false;
                        break;
                    }
                }
                if (!noneMatch) break;
            }
            if (!noneMatch) break;
        }

        if (!Minecraft.IS_RUNNING_ON_MAC && noneMatch && lastNoneMatch != noneMatch) {
            KeyBinding.updateKeyBindState();
        }
        lastNoneMatch = noneMatch;

        boolean alphaCheck = alpha.isFinished() && alpha.getValue() == 1.0D;
        if (alphaCheck && mc.mouseHelper.isMouseGrabbed()) {
            mc.mouseHelper.ungrabMouse();
        }
    }

    private void closeCheck() {

        boolean noneMatch = true;
        for (CategoryComponent category : panel.getCategoryComponents()) {
            for (ModuleComponent module : category.getModuleComponents()) {
                if (module.isBinding()) {
                    noneMatch = false;
                    break;
                }
            }
            if (!noneMatch) break;
        }

        if (exit && alpha.isFinished() && noneMatch) {
            closeScreen();
            exit = false;
        }
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public boolean isSearchFieldSelected() {
        return searchField.isSelected();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        if (!isSearching()) return false;
        return !text
                .trim()
                .toLowerCase()
                .startsWith(getSearchText()
                        .trim()
                        .toLowerCase());
    }


}
