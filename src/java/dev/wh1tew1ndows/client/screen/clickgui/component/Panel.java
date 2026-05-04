package dev.wh1tew1ndows.client.screen.clickgui.component;

import dev.wh1tew1ndows.client.api.interfaces.IScreen;
import dev.wh1tew1ndows.client.api.interfaces.IWindow;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen;
import dev.wh1tew1ndows.client.screen.clickgui.component.category.CategoryComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.module.ModuleComponent;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.draw.Scissor;
import dev.wh1tew1ndows.client.utils.render.draw.StencilUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;

import static dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen.alpha;

@Getter
public class Panel implements IScreen, IWindow {

    // ── layout ────────────────────────────────────────────────────────────────
    private static final float LEFT_W  = 90f;   // шире левая панель
    private static final float TOP_H   = 18f;   // высота хлебных крошек
    private static final float GUI_W   = 380f;  // ширина GUI (уменьшено)
    private static final float GUI_H   = 240f;  // высота GUI (уменьшено)

    // ── refs ──────────────────────────────────────────────────────────────────
    private final ClickGuiScreen clickGui;
    private final ResourceLocation avatar = new Namespaced("texture/avatar.png");
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();

    @Setter public ModuleComponent expandedModule = null;

    private Category currentCategory = Category.COMBAT;
    private final Animation[] catHoverAnims;

    // ── scroll (простой float + анимация) ─────────────────────────────────────
    private float scrollTarget = 0f;
    private final Animation scrollAnimation = new Animation();

    private boolean firstInit = true;
    private long lastScrollArrow = 0;
    private static final long ARROW_INTERVAL = 16;
    /** Позиция GUI (-1 = по центру экрана) */
    private float guiX = -1, guiY = -1;
    private boolean dragging = false;
    private float dragOffX, dragOffY;

    // ── constructor ───────────────────────────────────────────────────────────

    public Panel(ClickGuiScreen clickGui) {
        this.clickGui = clickGui;
        Category[] cats = Category.values();
        catHoverAnims = new Animation[cats.length];
        for (int i = 0; i < cats.length; i++) catHoverAnims[i] = new Animation();
        for (Category category : cats) {
            categoryComponents.add(new CategoryComponent(category, clickGui));
        }
    }

    // ── init / resize ─────────────────────────────────────────────────────────

    private void setPosition() { /* ничего не нужно */ }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        for (CategoryComponent c : categoryComponents) c.resize(minecraft, width, height);
    }

    @Override
    public void init() {
        if (firstInit) { firstInit = false; }
        for (CategoryComponent c : categoryComponents) if (c != null) c.init();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private float getGuiX() {
        if (guiX < 0) return (scaled().x - GUI_W) / 2f;
        return guiX;
    }

    private float getGuiY() {
        if (guiY < 0) return (scaled().y - GUI_H) / 2f;
        return guiY;
    }

    private boolean isHover(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && my >= y && mx <= x + w && my <= y + h;
    }

    // ── scroll ────────────────────────────────────────────────────────────────

    private float contentH() { return GUI_H - TOP_H - 6; }

    private float currentModH() {
        for (CategoryComponent comp : categoryComponents)
            if (comp.getCategory() == currentCategory) return comp.getModuleHeight();
        return 0;
    }

    private float maxScroll() {
        float excess = currentModH() - contentH();
        return excess > 0 ? -excess : 0;
    }

    private void clampScroll() {
        float ms = maxScroll();
        if (scrollTarget > 0) scrollTarget = 0;
        if (scrollTarget < ms) scrollTarget = ms;
    }

    private void updateScroll() {
        clampScroll();
        scrollAnimation.update();
        scrollAnimation.run(scrollTarget, 0.15, Easings.QUAD_OUT, true);
        handleScrollArrow();
    }

    private void handleScrollArrow() {
        if (clickGui.isSearchFieldSelected()) return;
        long now = System.currentTimeMillis();
        if (now - lastScrollArrow < ARROW_INTERVAL) return;
        lastScrollArrow = now;
        float amt = 15f;
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN.getKey()))
            scrollTarget = Math.max(scrollTarget - amt, maxScroll());
        else if (Keyboard.isKeyDown(Keyboard.KEY_UP.getKey()))
            scrollTarget = Math.min(scrollTarget + amt, 0);
    }

    // ── render ────────────────────────────────────────────────────────────────

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        RectUtil.drawRect(matrix, 0, 0, width(), height(), ColorUtil.getColor(0, 0.5F * alpha.get()));
        RenderUtil.drawMainMenuShader(
                mc.getMainWindow().getScaledWidth() * 2,
                mc.getMainWindow().getScaledHeight() * 2, mouseX, mouseY, 1);

        updateScroll();

        float gx = getGuiX();
        float gy = getGuiY();
        float a  = alpha.get();
        int  ai  = (int)(255 * a);

        // ── основной фон ──
        RenderUtil.Rounded.smooth(matrix, gx, gy, GUI_W, GUI_H,
                ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), ai), Round.of(8));

        // ── левая панель ──
        RenderUtil.Rounded.smooth(matrix, gx, gy, LEFT_W, GUI_H,
                ColorUtil.replAlpha(ColorUtil.getColor(10, 10, 14), ai), Round.of(8, 8, 0, 0));
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W, gy, 0.5F, GUI_H,
                ColorUtil.replAlpha(ColorUtil.getColor(40, 40, 50), ai), Round.of(0));

        drawLeftPanel(matrix, gx, gy, mouseX, mouseY, ai);

        // ── верхняя полоса ──
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W + 1, gy, GUI_W - LEFT_W - 1, TOP_H,
                ColorUtil.replAlpha(ColorUtil.getColor(12, 12, 16), ai), Round.of(0, 8, 0, 0));
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W + 1, gy + TOP_H, GUI_W - LEFT_W - 1, 0.5F,
                ColorUtil.replAlpha(ColorUtil.getColor(40, 40, 50), ai), Round.of(0));
        drawBreadcrumbs(matrix, gx, gy, ai);

        // ── контент ──
        float contentX = gx + LEFT_W + 6;
        float contentY = gy + TOP_H + 3;
        float contentW = GUI_W - LEFT_W - 12;
        float contentH = GUI_H - TOP_H - 6;
        float colW = (contentW - 5f) / 2f;

        // debug: убедимся что контентная область видна
        RenderUtil.Rounded.smooth(matrix, contentX, contentY, contentW, contentH,
                ColorUtil.replAlpha(ColorUtil.getColor(20, 20, 25), ai), Round.of(0));

        Scissor.push();
        Scissor.setFromComponentCoordinates(contentX - 1, contentY - 1, contentW + 2, contentH + 2);

        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            if (clickGui.searchCheck(comp.getCategory().getName())) continue;
            comp.setColumnWidth(colW);
            comp.position().set(contentX, contentY + (float) scrollAnimation.getValue());
            comp.render(matrix, mouseX, mouseY, partialTicks);
        }

        Scissor.unset();
        Scissor.pop();

        drawScrollbar(matrix, gx, gy, contentH, ai);
    }

    // ── left panel ────────────────────────────────────────────────────────────

    private void drawLeftPanel(MatrixStack ms, float gx, float gy, int mx, int my, int ai) {
        // логотип Z — текстура
        float logoSz = 22f;
        float logoX  = gx + 8;
        float logoY  = gy + 8;
        RenderUtil.Rounded.smooth(ms, logoX, logoY, logoSz, logoSz,
                ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), ai), Round.of(5));
        // рисуем текстуру z_logo.png с прозрачностью 0.75
        RenderUtil.drawImage(new Namespaced("interface/z_logo.png"), ms,
                logoX + 1, logoY + 1, logoSz - 2, logoSz - 2,
                ColorUtil.replAlpha(ColorUtil.getColor(255), (int)(ai * 0.75f)));

        // название
        Fonts.MONTSERRAT_BOLD.draw(ms, "Zetrix", logoX + logoSz + 5, logoY + 3,
                ColorUtil.replAlpha(ColorUtil.getColor(225), ai), 6.5F);
        Fonts.MONTSERRAT_BOLD.draw(ms, "Client", logoX + logoSz + 5, logoY + 11,
                ColorUtil.replAlpha(ColorUtil.fade(), ai), 5.5F);

        // категории
        float catStartY = gy + 42;
        float catH      = 22f;
        float catSpacing = 2f;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            float cy  = catStartY + i * (catH + catSpacing);
            boolean sel = cat == currentCategory;
            boolean hov = isHover(mx, my, gx + 4, cy, LEFT_W - 8, catH);

            catHoverAnims[i].update();
            catHoverAnims[i].run(hov ? 1 : 0, 0.2, Easings.QUAD_OUT, true);
            float hv = (float) Math.min(1.0, catHoverAnims[i].get());

            if (sel) {
                RenderUtil.Rounded.smooth(ms, gx + 4, cy, LEFT_W - 8, catH,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.2F), ai), Round.of(5));
                RenderUtil.Rounded.smooth(ms, gx + 4, cy + 4, 3, catH - 8,
                        ColorUtil.replAlpha(ColorUtil.fade(), ai), Round.of(1.5F));
            } else if (hv > 0.01f) {
                // цвет hover = тема с низкой прозрачностью
                RenderUtil.Rounded.smooth(ms, gx + 4, cy, LEFT_W - 8, catH,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.15F),
                                (int)(ai * 0.5f * hv)), Round.of(5));
            }

            int iconCol = sel
                    ? ColorUtil.replAlpha(ColorUtil.fade(), ai)
                    : hv > 0.01f
                        ? ColorUtil.replAlpha(ColorUtil.fade(), (int)(ai * (0.5f + 0.5f * hv)))
                        : ColorUtil.replAlpha(ColorUtil.getColor(90), ai);
            Fonts.ICON_DESHUX.draw(ms, cat.getIcon(), gx + 10, cy + 6.5F, iconCol, 8);

            int textCol = sel
                    ? ColorUtil.replAlpha(ColorUtil.getColor(230), ai)
                    : hv > 0.01f
                        ? ColorUtil.overCol(
                            ColorUtil.replAlpha(ColorUtil.getColor(140), ai),
                            ColorUtil.replAlpha(ColorUtil.getColor(210), ai),
                            hv)
                        : ColorUtil.replAlpha(ColorUtil.getColor(140), ai);
            Fonts.MONTSERRAT_BOLD.draw(ms, cat.getName(), gx + 22, cy + 7F, textCol, 6.5F);
        }

        // поиск
        float searchY = gy + GUI_H - 42;
        boolean typing = clickGui.isSearchFieldSelected();
        RenderUtil.Rounded.smooth(ms, gx + 4, searchY, LEFT_W - 8, 14,
                ColorUtil.replAlpha(ColorUtil.getColor(typing ? 20 : 15, typing ? 0.12F : 0.06F), ai), Round.of(4));
        // обводка только когда активен поиск, иначе убираем
        if (typing) {
            RenderUtil.Rounded.roundedOutline(ms, gx + 4, searchY, LEFT_W - 8, 14, 0.5F,
                    ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.5F), ai), Round.of(4));
        }
        String searchTxt = clickGui.isSearching()
                ? clickGui.getSearchText() + (typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "")
                : (typing ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : "Search");
        int searchCol = !clickGui.isSearching() && !typing
                ? ColorUtil.replAlpha(ColorUtil.getColor(70), ai)
                : ColorUtil.replAlpha(ColorUtil.getColor(210), ai);
        Fonts.MONTSERRAT_BOLD.draw(ms, searchTxt, gx + 9, searchY + 3.5F, searchCol, 5.5F);

        // профиль — тёмный фон, тонкий разделитель
        float profileY = gy + GUI_H - 22;
        String username = mc.player != null ? mc.player.getName().getString() : "User";
        // тёмный фон блока профиля
        RenderUtil.Rounded.smooth(ms, gx + 4, profileY, LEFT_W - 8, 18,
                ColorUtil.replAlpha(ColorUtil.getColor(18, 18, 22), ai), Round.of(4));
        // тонкий тёмный разделитель над профилем
        RenderUtil.Rounded.smooth(ms, gx + 4, profileY - 1, LEFT_W - 8, 0.5F,
                ColorUtil.replAlpha(ColorUtil.getColor(30, 30, 36), ai), Round.of(0));
        // аватар — тёмный
        RenderUtil.Rounded.smooth(ms, gx + 7, profileY + 2, 14, 14,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.35F), ai), Round.of(3));
        Fonts.MONTSERRAT_BOLD.drawCenter(ms, String.valueOf(username.charAt(0)).toUpperCase(),
                gx + 14, profileY + 5.5F, ColorUtil.replAlpha(ColorUtil.getColor(180), ai), 5.5F);
        // ник и подпись — приглушённые цвета
        Fonts.MONTSERRAT_BOLD.draw(ms, username, gx + 25, profileY + 3.5F,
                ColorUtil.replAlpha(ColorUtil.getColor(160), ai), 5.5F);
        Fonts.MONTSERRAT_BOLD.draw(ms, "Zetrix Client", gx + 25, profileY + 10.5F,
                ColorUtil.replAlpha(ColorUtil.fade(), ai), 4.5F);
    }

    // ── breadcrumbs ───────────────────────────────────────────────────────────

    private void drawBreadcrumbs(MatrixStack ms, float gx, float gy, int ai) {
        float ty = gy + TOP_H / 2f - 2.5F;
        float bx = gx + LEFT_W + 6;
        Fonts.MONTSERRAT_BOLD.draw(ms, "Zetrix Client", bx, ty,
                ColorUtil.replAlpha(ColorUtil.getColor(90), ai), 5.5F);
        float arrowX = bx + Fonts.MONTSERRAT_BOLD.getWidth("Zetrix Client", 5.5F) + 4;
        Fonts.MONTSERRAT_BOLD.draw(ms, ">", arrowX, ty,
                ColorUtil.replAlpha(ColorUtil.getColor(60), ai), 5.5F);
        Fonts.MONTSERRAT_BOLD.draw(ms, currentCategory.getName(),
                arrowX + Fonts.MONTSERRAT_BOLD.getWidth("> ", 5.5F) + 2, ty,
                ColorUtil.replAlpha(ColorUtil.getColor(215), ai), 5.5F);
    }

    // ── scrollbar ─────────────────────────────────────────────────────────────

    private void drawScrollbar(MatrixStack ms, float gx, float gy, float contentH, int ai) {
        float modH = currentModH();
        if (modH <= contentH) return;

        float barTrackH = contentH - 4;
        float barH = Math.max(14, barTrackH * (contentH / modH));
        float scrolled = Math.max(0, -(float) scrollAnimation.getValue());
        float maxS = modH - contentH;
        float scrollFrac = maxS > 0 ? scrolled / maxS : 0;
        float barY = gy + TOP_H + 3 + 2 + scrollFrac * (barTrackH - barH);

        StencilUtil.enable();
        RenderUtil.Rounded.smooth(ms, gx + GUI_W - 4, gy + TOP_H + 3, 2, contentH,
                ColorUtil.replAlpha(ColorUtil.getColor(255, 0.06F), ai), Round.of(1));
        StencilUtil.read(1);
        RenderUtil.Rounded.smooth(ms, gx + GUI_W - 4, barY, 2, barH,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.7F), ai), Round.of(1));
        StencilUtil.disable();
    }

    // ── mouse / keyboard ──────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float gx = getGuiX();
        float gy = getGuiY();

        if (button == 0) {
            // перетаскивание — клик по верхней полосе или левой панели (верх)
            boolean onTopBar = isHover(mouseX, mouseY, gx + LEFT_W, gy, GUI_W - LEFT_W, TOP_H);
            boolean onLogo   = isHover(mouseX, mouseY, gx, gy, LEFT_W, 40);
            if (onTopBar || onLogo) {
                dragging = true;
                dragOffX = (float) mouseX - gx;
                dragOffY = (float) mouseY - gy;
                return false;
            }
        }

        // клик по категориям
        float catStartY = gy + 42;
        float catH = 22f, catSpacing = 2f;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            float cy = catStartY + i * (catH + catSpacing);
            if (isHover(mouseX, mouseY, gx + 4, cy, LEFT_W - 8, catH)) {
                if (currentCategory != cats[i]) {
                    currentCategory = cats[i];
                    scrollTarget = 0;
                    scrollAnimation.set(0);
                }
            }
        }

        // клик по поиску
        float searchY = gy + GUI_H - 42;
        if (isHover(mouseX, mouseY, gx + 4, searchY, LEFT_W - 8, 14)) {
            clickGui.searchField().setSelected(!clickGui.searchField().isSelected());
        }

        // клик по модулям
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            comp.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            comp.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    /** Вызывается из ClickGuiScreen при mouseMoved/drag */
    public void mouseDragged(double mouseX, double mouseY) {
        if (!dragging) return;
        float sw = scaled().x, sh = scaled().y;
        guiX = (float) Math.max(0, Math.min(mouseX - dragOffX, sw - GUI_W));
        guiY = (float) Math.max(0, Math.min(mouseY - dragOffY, sh - GUI_H));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollTarget = Math.min(Math.max(scrollTarget + (float)(delta * 12), maxScroll()), 0);
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            if (comp.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            comp.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            comp.keyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (CategoryComponent comp : categoryComponents) {
            if (comp.getCategory() != currentCategory) continue;
            comp.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public void onClose() {
        for (CategoryComponent comp : categoryComponents) comp.onClose();
    }
}
