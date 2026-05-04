package dev.wh1tew1ndows.client.screen.mainmenu;

import com.google.common.collect.Lists;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.api.interfaces.IScreen;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.Namespaced;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.List;

public class MainMenu extends Screen implements IMinecraft {
    private final List<Button> buttons = Lists.newArrayList();
    private final Animation alpha = new Animation();

    public MainMenu() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        buttons.forEach(btn -> btn.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        super.init();
        alpha.set(0F);
        alpha.run(1.0, 0.5);

        float margin = 5;
        float buttonWidth = 200F;
        float buttonHeight = 25F;

        buttons.clear();

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F - buttonHeight / 2F - margin - buttonHeight), new Vector2f(buttonWidth, buttonHeight), "singleplayer", "w",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new WorldSelectionScreen(this))));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F - buttonHeight / 2F), new Vector2f(buttonWidth, buttonHeight), "multiplayer", "x",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new MultiplayerScreen(this))));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F + buttonHeight / 2F + margin), new Vector2f((buttonWidth / 2F) - (margin / 2F), buttonHeight), "settings", "h",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new OptionsScreen(this, this.minecraft.gameSettings))));

        buttons.add(new Button(new Vector2f(width / 2F + (margin / 2F), height / 2F + buttonHeight / 2F + margin), new Vector2f((buttonWidth / 2F) - (margin / 2F), buttonHeight), "exit", "A",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> this.minecraft.shutdown()));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 4F, height / 2F + buttonHeight / 2F + margin + buttonHeight + margin), new Vector2f(buttonWidth / 2F, buttonHeight), "account", "y",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(Zetrix.inst().altWidget())));

        buttons.forEach(Button::init);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        alpha.update();
        RectUtil.drawRect(matrixStack, 0, 0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), ColorUtil.multAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.06F), alpha.get() * 1));

        RenderUtil.drawImage(new Namespaced("texture/top_glow.png"), matrixStack, 0, 0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alpha.get()));
        RenderUtil.drawImage(new Namespaced("texture/circles_effect.png"), matrixStack, 0, 0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alpha.get()));

        int textColor = ColorUtil.getColor(200, 200, 230, alpha.get());
        int accentColor = ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alpha.get());

        float darker = 0.5F;

        StringBuilder fadeTitle = new StringBuilder();
        String namespace = "Zetrix.cc";
        for (int i = 0; i < namespace.length(); i++) {
            fadeTitle.append(ColorFormatting.getColor(ColorUtil.fade(5, i * 20, accentColor, ColorUtil.multDark(accentColor, darker))));
            fadeTitle.append(namespace.charAt(i));
        }

        String title = fadeTitle.toString();
        Fonts.MONTSERRAT_BOLD.drawCenter(matrixStack, title, width / 2F, height / 2F - 75, textColor, 16);


        buttons.forEach(btn -> {
            btn.alpha(alpha.get());
            btn.render(matrixStack, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        buttons.forEach(btn -> btn.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        buttons.forEach(Button::onClose);
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public static final class Button implements IScreen {
        private final Vector2f position;
        private final Vector2f size;
        private final String text;
        private final String icon;
        private final Vector2i glowColor;
        private final IPressable action;
        private Animation hover = new Animation();
        private float alpha = 0;

        @Override
        public void resize(Minecraft minecraft, int width, int height) {

        }

        @Override
        public void init() {

        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {

            return false;
        }


        @Override
        public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            float fontSize = 8.5F;

            hover.update();

            boolean hovered = isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);

            hover.run(hovered ? 1.0 : 0.0, 0.25, Easings.SINE_OUT, true);

            int first = ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alpha / 4F);


            RenderUtil.Rounded.smooth(matrix, position.x, position.y, size.x, size.y, ColorUtil.multAlpha(ColorUtil.multDark(InterFace.getInstance().clientColor(), 0.15F), alpha), Round.of(6));
            RenderUtil.Rounded.roundedOutline(matrix, position.x, position.y, size.x, size.y, 1, ColorUtil.multAlpha(ColorUtil.multDark(InterFace.getInstance().clientColor(), 0.23F), alpha), Round.of(6));

            float hoverAlpha = hover.get() / 8;
            int hoverFirst = ColorUtil.multAlpha(InterFace.getInstance().clientColor(), hoverAlpha);
            int hoverSecond = ColorUtil.multAlpha(InterFace.getInstance().clientColor(), hoverAlpha);
            RenderUtil.Rounded.smooth(matrix, position.x, position.y, size.x, size.y,
                    hoverFirst,
                    hoverFirst,
                    hoverSecond,
                    hoverSecond,
                    Round.of(6)
            );
            RenderUtil.Rounded.roundedOutline(matrix, position.x, position.y, size.x, size.y, 1,
                    hoverFirst,
                    hoverFirst,
                    hoverSecond,
                    hoverSecond,
                    Round.of(6)
            );
            int textColor = ColorUtil.getColor(200, 200, 230, alpha);
            int accentColor = ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alpha);

            Fonts.MONTSERRAT_MEDIUM.drawCenter(matrix, text, position.x + (size.x / 2F), position.y + (size.y / 2F) - (fontSize / 2F) - 0.3F, ColorUtil.overCol(textColor, accentColor, hover.get()), fontSize - (hover.get() / 2F));


            int fonts = 10;

            Fonts.ICON_V1.drawRight(
                    matrix,
                    icon,
                    position.x + (size.x / 2F) - Fonts.MONTSERRAT_MEDIUM.getWidth(text, fontSize - (hover.get() / 2F)) / 2 - 3,
                    position.y + (size.y / 2F) - (fontSize / 2F) + 0.7F,
                    ColorUtil.overCol(textColor, accentColor, hover.get()),
                    fontSize - (hover.get() / 2F)
            );

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHover(mouseX, mouseY, position.x, position.y, size.x, size.y)) {
                action.onPress(this);
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

        public interface IPressable {
            void onPress(Button action);
        }
    }
}