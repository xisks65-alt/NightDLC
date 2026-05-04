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
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
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

        // ── тень/свечение вокруг GUI ──
        RenderUtil.Shadow.drawShadow(matrix, gx, gy, GUI_W, GUI_H, 18,
                ColorUtil.replAlpha(ColorUtil.getColor(0), (int)(ai * 0.6f)));
        RenderUtil.Shadow.drawShadow(matrix, gx, gy, GUI_W, GUI_H, 8,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.3F), (int)(ai * 0.25f)));

        // ── основной фон ──
        RenderUtil.Rounded.smooth(matrix, gx, gy, GUI_W, GUI_H,
                ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), ai), Round.of(8));

        // ── левая панель с градиентом ──
        RenderUtil.Rounded.smooth(matrix, gx, gy, LEFT_W, GUI_H,
                ColorUtil.replAlpha(ColorUtil.getColor(12, 12, 16), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(12, 12, 16), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(8, 8, 12), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(8, 8, 12), ai),
                Round.of(8, 8, 0, 0));
        // разделитель с градиентом
        int sepTop = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.25F), ai);
        int sepBot = ColorUtil.replAlpha(ColorUtil.getColor(30, 30, 38), ai);
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W, gy + 4, 0.5F, GUI_H - 8,
                sepTop, sepTop, sepBot, sepBot, Round.of(0));

        drawLeftPanel(matrix, gx, gy, mouseX, mouseY, ai);

        // ── верхняя полоса с закруглением справа ──
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W + 1, gy, GUI_W - LEFT_W - 1, TOP_H,
                ColorUtil.replAlpha(ColorUtil.getColor(12, 12, 16), ai), Round.of(0, 8, 0, 0));
        // акцентная линия под верхней полосой
        int accentLeft = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.5F), (int)(ai * 0.6f));
        int accentRight = ColorUtil.replAlpha(ColorUtil.getColor(30, 30, 38), (int)(ai * 0.3f));
        RenderUtil.Rounded.smooth(matrix, gx + LEFT_W + 1, gy + TOP_H, GUI_W - LEFT_W - 1, 0.5F,
                accentLeft, accentRight, accentRight, accentLeft, Round.of(0));
        drawBreadcrumbs(matrix, gx, gy, ai);

        // ── контент ──
        float contentX = gx + LEFT_W + 6;
        float contentY = gy + TOP_H + 3;
        float contentW = GUI_W - LEFT_W - 12;
        float contentH = GUI_H - TOP_H - 6;
        float colW = (contentW - 5f) / 2f;

        // контентная область с градиентом и закруглением
        RenderUtil.Rounded.smooth(matrix, contentX, contentY, contentW, contentH,
                ColorUtil.replAlpha(ColorUtil.getColor(18, 18, 22), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(18, 18, 22), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), ai),
                ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), ai),
                Round.of(0, 0, 8, 0));

        // декоративные элементы в контентной области
        drawContentDecorations(matrix, contentX, contentY, contentW, contentH, ai);

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
        RenderUtil.Shadow.drawShadow(ms, logoX, logoY, logoSz, logoSz, 6,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.3F), (int)(ai * 0.3f)));
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
                RenderUtil.Shadow.drawShadow(ms, gx + 4, cy, LEFT_W - 8, catH, 6,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.4F), (int)(ai * 0.3f)));
                RenderUtil.Rounded.smooth(ms, gx + 4, cy, LEFT_W - 8, catH,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.2F), ai), Round.of(5));
                RenderUtil.Rounded.roundedOutline(ms, gx + 4, cy, LEFT_W - 8, catH, 0.5F,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.6F), (int)(ai * 0.4f)), Round.of(5));
                RenderUtil.Rounded.smooth(ms, gx + 4, cy + 4, 3, catH - 8,
                        ColorUtil.replAlpha(ColorUtil.fade(), ai), Round.of(1.5F));
                RenderUtil.Shadow.drawShadow(ms, gx + 4, cy + 4, 3, catH - 8, 4,
                        ColorUtil.replAlpha(ColorUtil.fade(), (int)(ai * 0.4f)));
            } else if (hv > 0.01f) {
                RenderUtil.Rounded.smooth(ms, gx + 4, cy, LEFT_W - 8, catH,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.15F),
                                (int)(ai * 0.5f * hv)), Round.of(5));
                RenderUtil.Rounded.roundedOutline(ms, gx + 4, cy, LEFT_W - 8, catH, 0.5F,
                        ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.3F),
                                (int)(ai * 0.2f * hv)), Round.of(5));
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
        if (typing) {
            RenderUtil.Shadow.drawShadow(ms, gx + 4, searchY, LEFT_W - 8, 14, 5,
                    ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.4F), (int)(ai * 0.25f)));
        }
        RenderUtil.Rounded.smooth(ms, gx + 4, searchY, LEFT_W - 8, 14,
                ColorUtil.replAlpha(ColorUtil.getColor(typing ? 20 : 15, typing ? 0.12F : 0.06F), ai), Round.of(4));
        RenderUtil.Rounded.roundedOutline(ms, gx + 4, searchY, LEFT_W - 8, 14, 0.5F,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), typing ? 0.5F : 0.15F),
                        (int)(ai * (typing ? 1.0f : 0.3f))), Round.of(4));
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
        // градиентный разделитель над профилем
        int profSepLeft = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.3F), ai);
        int profSepRight = ColorUtil.replAlpha(ColorUtil.getColor(25, 25, 30), ai);
        RenderUtil.Rounded.smooth(ms, gx + 4, profileY - 1, LEFT_W - 8, 0.5F,
                profSepLeft, profSepRight, profSepRight, profSepLeft, Round.of(0));
        // аватар с свечением
        RenderUtil.Shadow.drawShadow(ms, gx + 7, profileY + 2, 14, 14, 4,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.4F), (int)(ai * 0.25f)));
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
        float catNameX = arrowX + Fonts.MONTSERRAT_BOLD.getWidth("> ", 5.5F) + 2;
        int catNameCol = ColorUtil.replAlpha(ColorUtil.multBright(InterFace.getInstance().themeColor(), 0.8F), ai);
        Fonts.MONTSERRAT_BOLD.draw(ms, currentCategory.getName(), catNameX, ty, catNameCol, 5.5F);
        float catNameW = Fonts.MONTSERRAT_BOLD.getWidth(currentCategory.getName(), 5.5F);
        RenderUtil.Shadow.drawShadow(ms, catNameX, ty - 1, catNameW, 8, 4,
                ColorUtil.replAlpha(InterFace.getInstance().themeColor(), (int)(ai * 0.1f)));
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
        int scrollCol = ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.7F), ai);
        RenderUtil.Shadow.drawShadow(ms, gx + GUI_W - 4, barY, 2, barH, 3,
                ColorUtil.replAlpha(ColorUtil.multDark(ColorUtil.fade(), 0.4F), (int)(ai * 0.3f)));
        RenderUtil.Rounded.smooth(ms, gx + GUI_W - 4, barY, 2, barH, scrollCol, Round.of(1));
        StencilUtil.disable();
    }

    // ── content decorations ──────────────────────────────────────────────────

    private void drawContentDecorations(MatrixStack ms, float cx, float cy, float cw, float ch, int ai) {
        int themeColor = InterFace.getInstance().themeColor();
        float time = (System.currentTimeMillis() - ClickGuiScreen.startTime) / 1000f;

        // угловые акценты — L-образные линии в углах контентной области
        float cornerLen = 16f;
        float cornerThick = 0.5f;
        float cornerAlpha = 0.12f + 0.04f * (float) Math.sin(time * 1.5);
        int cornerCol = ColorUtil.replAlpha(ColorUtil.multDark(themeColor, 0.6F), (int)(ai * cornerAlpha));

        // верхний левый
        RenderUtil.Rounded.smooth(ms, cx + 2, cy + 2, cornerLen, cornerThick, cornerCol, Round.of(0));
        RenderUtil.Rounded.smooth(ms, cx + 2, cy + 2, cornerThick, cornerLen, cornerCol, Round.of(0));
        // верхний правый
        RenderUtil.Rounded.smooth(ms, cx + cw - 2 - cornerLen, cy + 2, cornerLen, cornerThick, cornerCol, Round.of(0));
        RenderUtil.Rounded.smooth(ms, cx + cw - 2 - cornerThick, cy + 2, cornerThick, cornerLen, cornerCol, Round.of(0));
        // нижний левый
        RenderUtil.Rounded.smooth(ms, cx + 2, cy + ch - 2 - cornerThick, cornerLen, cornerThick, cornerCol, Round.of(0));
        RenderUtil.Rounded.smooth(ms, cx + 2, cy + ch - 2 - cornerLen, cornerThick, cornerLen, cornerCol, Round.of(0));
        // нижний правый
        RenderUtil.Rounded.smooth(ms, cx + cw - 2 - cornerLen, cy + ch - 2 - cornerThick, cornerLen, cornerThick, cornerCol, Round.of(0));
        RenderUtil.Rounded.smooth(ms, cx + cw - 2 - cornerThick, cy + ch - 2 - cornerLen, cornerThick, cornerLen, cornerCol, Round.of(0));

        // центральное акцентное свечение (пульсирующее) — большой мягкий градиент по центру
        float pulseAlpha = 0.03f + 0.015f * (float) Math.sin(time * 0.8);
        float glowW = cw * 0.5f;
        float glowH = ch * 0.4f;
        float glowX = cx + (cw - glowW) / 2f;
        float glowY = cy + (ch - glowH) / 2f;
        RenderUtil.Shadow.drawShadow(ms, glowX, glowY, glowW, glowH, 30,
                ColorUtil.replAlpha(ColorUtil.multDark(themeColor, 0.4F), (int)(ai * pulseAlpha)));

        // тонкие горизонтальные линии-сетка (декоративный паттерн)
        float gridSpacing = 32f;
        float gridAlpha = 0.025f;
        int gridCol = ColorUtil.replAlpha(ColorUtil.getColor(255), (int)(ai * gridAlpha));
        for (float ly = cy + gridSpacing; ly < cy + ch - gridSpacing; ly += gridSpacing) {
            RenderUtil.Rounded.smooth(ms, cx + 8, ly, cw - 16, 0.3f, gridCol, Round.of(0));
        }

        // вертикальная разделительная линия по центру (разделяет два столбца)
        float divX = cx + cw / 2f - 0.15f;
        int divTop = ColorUtil.replAlpha(ColorUtil.multDark(themeColor, 0.3F), (int)(ai * 0.06f));
        int divBot = ColorUtil.replAlpha(ColorUtil.multDark(themeColor, 0.15F), (int)(ai * 0.03f));
        RenderUtil.Rounded.smooth(ms, divX, cy + 8, 0.3f, ch - 16, divTop, divTop, divBot, divBot, Round.of(0));

        // нижний градиент (fade to dark) — виньетка внизу
        float fadeH = 40f;
        int fadeTop = ColorUtil.replAlpha(ColorUtil.getColor(0), 0);
        int fadeBot = ColorUtil.replAlpha(ColorUtil.getColor(14, 14, 18), (int)(ai * 0.7f));
        RenderUtil.Rounded.smooth(ms, cx, cy + ch - fadeH, cw, fadeH,
                fadeTop, fadeTop, fadeBot, fadeBot, Round.of(0, 0, 8, 0));

        // верхний градиент (fade from header)
        float topFadeH = 20f;
        int topFadeTop = ColorUtil.replAlpha(ColorUtil.getColor(12, 12, 16), (int)(ai * 0.5f));
        int topFadeBot = ColorUtil.replAlpha(ColorUtil.getColor(0), 0);
        RenderUtil.Rounded.smooth(ms, cx, cy, cw, topFadeH,
                topFadeTop, topFadeTop, topFadeBot, topFadeBot, Round.of(0));

        // плавающие точки-частицы (статические, но мерцающие)
        float[][] dots = {
            {0.15f, 0.25f, 1.2f}, {0.75f, 0.18f, 0.9f}, {0.4f, 0.65f, 1.5f},
            {0.85f, 0.55f, 0.7f}, {0.25f, 0.8f, 1.1f}, {0.6f, 0.4f, 1.3f},
            {0.9f, 0.85f, 0.8f}, {0.1f, 0.5f, 1.0f}, {0.5f, 0.1f, 1.4f}
        };
        for (float[] dot : dots) {
            float dx = cx + dot[0] * cw;
            float dy = cy + dot[1] * ch;
            float dotAlpha = 0.06f + 0.04f * (float) Math.sin(time * dot[2] + dot[0] * 10);
            float dotSize = 1.5f + 0.5f * (float) Math.sin(time * dot[2] * 0.5f + dot[1] * 5);
            int dotCol = ColorUtil.replAlpha(themeColor, (int)(ai * dotAlpha));
            RenderUtil.Rounded.smooth(ms, dx - dotSize / 2, dy - dotSize / 2, dotSize, dotSize, dotCol, Round.of(dotSize / 2));
            RenderUtil.Shadow.drawShadow(ms, dx - dotSize, dy - dotSize, dotSize * 2, dotSize * 2, 4,
                    ColorUtil.replAlpha(themeColor, (int)(ai * dotAlpha * 0.5f)));
        }
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
