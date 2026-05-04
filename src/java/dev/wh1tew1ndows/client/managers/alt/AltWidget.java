package dev.wh1tew1ndows.client.managers.alt;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Namespaced;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Изменения:
 * - УБРАН верхний бар/чипы избранных.
 * - Список алтов теперь отображается так: сначала избранные, затем остальные (порядок внутри групп сохранён).
 * - Звезда всегда "★": серая по умолчанию, жёлтая если в избранном.
 * - Правая колонка (★ и ×) выровнена по правому краю.
 * Персист: "alts":[], "last":"", "favorites":[]
 */
public class AltWidget extends Screen implements IMinecraft {

    public final List<Alt> alts = new ArrayList<>();
    private final Set<String> favorites = new LinkedHashSet<>();

    // ---- стиль/цвета
    private static final int FAV_YELLOW = ColorUtil.getColor(255, 213, 74);
    private static final int STAR_GRAY = ColorUtil.getColor(150, 150, 150);
    private static final int STAR_YELLOW = FAV_YELLOW;

    // правая колонка геометрия (всё ровно)
    private static final float RHS_PAD = 17f;   // отступ от правого края строки
    private static final float ICON_SIZE = 12f; // диаметр круглой кнопки
    private static final float ICON_GAP = 18f; // расстояние между центрами ★ и ×

    // анимации
    private final Animation openAlpha = new Animation();
    private final Animation openScale = new Animation();
    private final Animation heightAnimation = new Animation();

    private final Animation bgAlpha = new Animation();
    private final Animation headerGlow = new Animation();
    private final Animation headerPulse = new Animation();
    private final Animation inputFocus = new Animation();
    private final Animation addHover = new Animation();
    private final Animation randomHover = new Animation();
    private final Animation scrollbarAlpha = new Animation();

    private static class RowAnims {
        final Animation hover = new Animation();
        final Animation select = new Animation();
        final Animation enter = new Animation();

        RowAnims() {
            enter.set(0F);
        }
    }

    private final Map<String, RowAnims> rowAnims = new HashMap<>();

    private static class AltAnimation {
        final Alt alt;
        final Animation animation = new Animation(); // 1 -> 0

        AltAnimation(Alt alt) {
            this.alt = alt;
            this.animation.set(1F);
            this.animation.run(0F, 0.35F, Easings.CUBIC_IN, false);
        }
    }

    private final List<AltAnimation> altAnimations = new ArrayList<>();

    // Геометрия
    private float x;
    private float y = 0;
    private float targetHeight = 20;
    private final float panelWidth = 260F;
    private float panelHeight = 350F;
    private final float rowHeight = 22F;

    // Видимая область списка
    private int visibleRows = 0;
    private float listX, listY, listW, listH;

    // Состояния
    public boolean open;
    private String altName = "";
    private boolean typing;
    private float scrollPre;
    private float scroll;

    // housekeeping
    private boolean loadedOnce = false;
    private long lastScrollTime = 0L;

    public AltWidget() {
        super(new StringTextComponent("Alt Manager"));
        open = true;
    }

    // пол-пиксельный снап для чётких границ
    private static float px(float v) {
        return Math.round(v * 2f) / 2f;
    }

    @Override
    protected void init() {
        super.init();
        bgAlpha.set(1);
        openAlpha.set(1);
        openScale.set(1);
        heightAnimation.set(20F);

        bgAlpha.run(1.0, 0.25, Easings.CUBIC_OUT, true);
        openAlpha.run(1.0, 0.5);
        openScale.run(1.0, 0.45, Easings.CUBIC_OUT, true);
        heightAnimation.run(panelHeight, 0.45F, Easings.CUBIC_OUT, true);

        headerGlow.set(0F);
        headerPulse.set(0F);
        inputFocus.set(typing ? 1F : 0F);
        scrollbarAlpha.set(0F);
        open = true;

        if (!loadedOnce) {
            loadAlts();
            loadedOnce = true;
        }
    }

    private Path altsPath() {
        File gameDir = mc != null && mc.gameDir != null ? mc.gameDir : new File(".");
        return gameDir.toPath().resolve("zetrix").resolve("account").resolve("alts.night");
    }

    private void loadAlts() {

    }

    private void saveAlts() {

    }

    private RowAnims ra(String key) {
        return rowAnims.computeIfAbsent(key, k -> new RowAnims());
    }

    private float removeCoef(Alt alt) {
        for (AltAnimation aa : altAnimations) {
            if (aa.alt == alt) {
                aa.animation.update();
                return (float) aa.animation.getValue();
            }
        }
        return 1F;
    }

    private float effectiveRowHeight(Alt alt) {
        float k = removeCoef(alt);
        return rowHeight * Math.max(0.0001F, k);
    }

    public void updateScroll(int mouseX, int mouseY, float delta) {
        float h = heightAnimation.get();
        if (Mathf.isHovered(mouseX, mouseY, this.x, this.y, panelWidth, h) && open) {
            float step = rowHeight;
            scrollPre += delta * step;
            lastScrollTime = System.currentTimeMillis();
            scrollbarAlpha.run(1.0, 0.15, Easings.SINE_OUT, true);
        }
    }

    /**
     * Без фав-бара: просто рассчитываем список
     */
    private void recalcLayout(float scale, float width, float curH, float panelX, float panelY) {
        listX = panelX + 2F * scale;
        listY = panelY + (25F * scale);
        listW = width * scale - 4F * scale;
        listH = (curH - 90F + 25F) * scale;

        visibleRows = Math.max(0, (int) Math.floor(listH / (rowHeight * scale)));

        float maxH = mc.getMainWindow().getScaledHeight() - 40F;
        panelHeight = Math.min(350F, Math.max(220F, maxH));
    }

    private void toggleFavorite(String name) {
        if (favorites.contains(name)) favorites.remove(name);
        else favorites.add(name);
        saveAlts();
    }

    /**
     * Представление списка: сначала избранные, затем остальные, порядок внутри групп сохранён
     */
    private List<Alt> orderedAlts() {
        List<Alt> fav = new ArrayList<>();
        List<Alt> other = new ArrayList<>();
        for (Alt a : alts) {
            if (favorites.contains(a.name)) fav.add(a);
            else other.add(a);
        }
        fav.addAll(other);
        return fav;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);

        // позиция/апдейты
        float width = panelWidth;
        float height = panelHeight;
        this.x = mc.getMainWindow().getScaledWidth() / 2F - width / 2F;
        this.y = mc.getMainWindow().getScaledHeight() / 2F - height / 2F;

        bgAlpha.update();
        openAlpha.update();
        openScale.update();
        heightAnimation.update();
        headerGlow.update();
        headerPulse.update();
        inputFocus.update();
        addHover.update();
        randomHover.update();
        scrollbarAlpha.update();

        if (System.currentTimeMillis() - lastScrollTime > 900) {
            scrollbarAlpha.run(0.0, 0.35, Easings.SINE_IN, true);
        }

        targetHeight = panelHeight;
        if (heightAnimation.getValue() != targetHeight) {
            heightAnimation.run(targetHeight, 0.45F, Easings.CUBIC_OUT, true);
        }

        // фон/эффекты
        RenderUtil.drawImage(new Namespaced("texture/mainmenu.png"), stack, 0, 0,
                mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(),
                ColorUtil.replAlpha(-1, openAlpha.get()));

        RectUtil.drawRect(stack, 0, 0,
                mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(),
                ColorUtil.getColor(0, openAlpha.get() * 0.9F));


        RenderUtil.drawImage(new Namespaced("texture/top_glow.png"), stack, 0, 0,
                mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(),
                ColorUtil.multAlpha(InterFace.getInstance().clientColor(), openAlpha.get()));
        RenderUtil.drawImage(new Namespaced("texture/circles_effect.png"), stack, 0, 0,
                mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(),
                ColorUtil.multAlpha(InterFace.getInstance().clientColor(), openAlpha.get()));

        float scale = (float) openScale.getValue();
        float panelX = this.x + (1F - scale) * width / 2F + 70;
        float panelY = this.y + (1F - scale) * height / 2F;
        float curH = heightAnimation.get();


        // стекло/кап/футер
        // RenderUtil.styleRec2(stack, panelX, panelY, width * scale, curH * scale, openAlpha.get(), 8);
        int headerBg = ColorUtil.multAlpha(ColorUtil.multDark(-1, 0), openAlpha.get() / 6);
        int footerBg = ColorUtil.multAlpha(ColorUtil.multDark(-1, 0), openAlpha.get() / 6);
        //RenderUtil.Rounded.smooth(stack, panelX + 1, panelY, width * scale - 2, 20 * scale, headerBg, Round.of(0, 8, 0, 8));
        //RenderUtil.Rounded.smooth(stack, panelX + 1, panelY + (curH - 40) * scale, width * scale - 2, 40 * scale, footerBg, Round.of(8, 0, 8, 0));

        // Заголовок
        String title = "Аккаунт Менеджер";
        boolean headerHover = Mathf.isHovered(mouseX, mouseY, panelX, panelY, width * scale, 40F * scale);
        headerGlow.run(headerHover ? 1.0 : 0.0, 0.25, Easings.SINE_OUT, true);
        float pulse = (float) (0.5F + 0.5F * Math.sin((System.currentTimeMillis() % 2000) / 2000.0 * Math.PI * 2.0));
        headerPulse.set(pulse);

        Fonts.SF_BOLD.drawGRS(stack, title,
                px(panelX - 140), px(panelY + 28 * scale),
                ColorUtil.multAlpha(InterFace.getInstance().textColor(), openAlpha.get()),
                ColorUtil.multAlpha(InterFace.getInstance().themeColor2(), openAlpha.get()), 8.5F * scale);

        Fonts.SF_BOLD.draw(stack, "Всего аккаунтов" + ColorFormatting.getColor(ColorUtil.getColor(80)) + " - " + ColorFormatting.getColor(InterFace.getInstance().textColor()) + alts.size(),
                px(panelX - 149), px(panelY + 38 * scale),
                ColorUtil.multAlpha(-1, openAlpha.get()), 7 * scale);

        Fonts.ICON_V1.draw(stack, "y",
                px(panelX - 150),
                px(panelY + 28.7F * scale),
                ColorUtil.multAlpha(InterFace.getInstance().themeColor(), openAlpha.get()), 8 * scale);

        // ===== Геометрия списка =====
        recalcLayout(scale, width, curH, panelX, panelY);

        // сформировать представление списка
        List<Alt> view = orderedAlts();

        // ==== Список ====
        dev.wh1tew1ndows.client.managers.alt.Scissor.push();
        dev.wh1tew1ndows.client.managers.alt.Scissor.setFromComponentCoordinates(listX, listY, listW, listH);

        float totalContentHeightUnits = 0F;
        for (Alt alt : view) totalContentHeightUnits += effectiveRowHeight(alt);

        float accUnits = 0F;
        int startIndex = 0;
        for (int i = 0; i < view.size(); i++) {
            float ehUnits = effectiveRowHeight(view.get(i));
            if (accUnits + ehUnits >= -scroll / scale) {
                startIndex = i;
                break;
            }
            accUnits += ehUnits;
        }

        int maxToDraw = Math.min(view.size() - startIndex, Math.max(visibleRows + 2, 8));
        float yCursorUnits = accUnits;

        // правый край для кнопок (абсолютное положение — ровно)
        float rightEdgeAbs = px(panelX + width * scale - RHS_PAD * scale);

        for (int i = 0; i < maxToDraw; i++) {
            Alt alt = view.get(startIndex + i);

            RowAnims a = ra(alt.name);
            a.enter.update();
            a.hover.update();
            a.select.update();

            boolean isCurrent = mc.session.getUsername().equals(alt.name);
            a.select.run(isCurrent ? 1.0 : 0.0, 0.35, Easings.CUBIC_OUT, true);

            boolean isFav = favorites.contains(alt.name);

            float rowK = removeCoef(alt);
            float rowY = px(listY + yCursorUnits * scale + scroll);
            float rowW = width * scale - 8F * scale;
            float rowH = (rowHeight - 2F) * scale * Math.max(0.0001F, rowK);

            boolean hovered = Mathf.isHovered(mouseX, mouseY, listX + 4F * scale, rowY, rowW - 22F * scale, rowH + 2F * scale);
            a.hover.run(hovered ? 1.0 : 0.0, 0.2, Easings.SINE_OUT, true);
            if (a.enter.getValue() == 0F) a.enter.run(1.0, 0.45, Easings.CUBIC_OUT, true);

            float enterK = (float) a.enter.getValue();
            float slideOffset = (1F - enterK) * 10F * scale;

            float baseAlpha = openAlpha.get() * rowK * enterK;
            int baseBg = ColorUtil.multDark(-1, 0.06F);

            int mixed = ColorUtil.overCol(
                    baseBg,
                    ColorUtil.fadeBetween(
                            ColorUtil.multDark(InterFace.getInstance().clientColor(), 1),
                            baseBg,
                            (float) (1F - a.select.getValue())),
                    0.12F + (float) a.hover.getValue() * 0.2F + (float) a.select.getValue() * 0.4F
            );

            // фон строки
            RenderUtil.Rounded.smooth(
                    stack,
                    px(listX + 4F * scale + slideOffset),
                    rowY,
                    rowW * scale,
                    Math.max(0.0001F, rowH),
                    ColorUtil.multAlpha(mixed, baseAlpha / 4),
                    ColorUtil.multAlpha(mixed, baseAlpha / 7),
                    Round.of(4)
            );

            // левая полоса (жёлтая для избранных)
            int stripeCol = isFav ? FAV_YELLOW : InterFace.getInstance().clientColor();
            float stripe = (float) (4.5F * scale + 1 * a.hover.getValue() + 2 * a.select.getValue());
            RenderUtil.Rounded.smooth(
                    stack,
                    px(listX + 4F * scale + slideOffset),
                    rowY,
                    stripe,
                    Math.max(0.0001F, rowH),
                    ColorUtil.multAlpha(stripeCol, baseAlpha * 0.7F),
                    Round.of(4, 4, 0, 0)
            );

            // имя
            float textX = px(listX + 4F * scale + slideOffset + stripe + 10F * scale);
            int textColor = ColorUtil.multAlpha(isCurrent ? InterFace.getInstance().clientColor() : ColorUtil.getColor(200), baseAlpha);
            Fonts.SF_BOLD.draw(stack, alt.name, textX, px(rowY + 5.6F * scale), textColor, 7.5F * scale);

            // ===== Правая колонка (★ и ×) =====
            float centerY = px(rowY + rowH / 2F);

            // ★
            float starSize = ICON_SIZE * scale;
            float starCenterX = px(rightEdgeAbs - ICON_GAP * scale);
            float starTLx = px(starCenterX - starSize / 2F);
            float starTLy = px(centerY - starSize / 2F);
            boolean starHovered = Mathf.isHovered(mouseX, mouseY, starTLx - 2F * scale, starTLy - 2F * scale, starSize + 4F * scale, starSize + 4F * scale);
            if (starHovered) {
                RenderUtil.Rounded.smooth(stack, starTLx - 2F * scale, starTLy - 2F * scale, starSize + 4F * scale, starSize + 4F * scale,
                        ColorUtil.multAlpha(ColorUtil.getColor(255, 255, 255), baseAlpha * 0.06F), Round.of((starSize + 4F * scale) / 2F));
            }
            int starCol = isFav ? STAR_YELLOW : STAR_GRAY;
            Fonts.SFP_MEDIUM.drawCenter(stack, "★", px(starTLx + starSize / 2F), px(starTLy + scale - 0.7F),
                    ColorUtil.multAlpha(starCol, baseAlpha), 11F * scale);

            // ×
            float delSize = ICON_SIZE * scale;
            float delCenterX = rightEdgeAbs;
            float delTLx = px(delCenterX - delSize / 2F);
            float delTLy = px(centerY - delSize / 2F);
            boolean deleteHovered = Mathf.isHovered(mouseX, mouseY, delTLx - 2F * scale, delTLy - 2F * scale, delSize + 4F * scale, delSize + 4F * scale);

            RenderUtil.Rounded.smooth(stack,
                    delTLx - scale, delTLy - scale,
                    delSize + 2F * scale, delSize + 2F * scale,
                    ColorUtil.multAlpha(deleteHovered ? ColorUtil.getColor(255, 85, 85) : ColorUtil.getColor(120, 120, 120),
                            baseAlpha * (deleteHovered ? 0.18F : 0.10F)),
                    Round.of((delSize + 2F * scale) / 2F)
            );
            int delIcon = deleteHovered ? ColorUtil.getColor(255, 120, 120) : ColorUtil.getColor(170, 170, 170);
            Fonts.SFP_REGULAR.drawCenter(stack, "×",
                    px(delTLx + delSize / 2F - 1 + 1.15F * scale),
                    px(delTLy + scale - 1.76F),
                    ColorUtil.multAlpha(delIcon, baseAlpha), 12F * scale);

            yCursorUnits += effectiveRowHeight(alt);
        }

        dev.wh1tew1ndows.client.managers.alt.Scissor.unset();
        dev.wh1tew1ndows.client.managers.alt.Scissor.pop();

        // Кламп скролла
        float viewH = listH;
        float contentH = totalContentHeightUnits * scale;
        float maxScroll = 0F;
        float minScroll = Math.min(0F, viewH - contentH - 2F * scale);
        scrollPre = MathHelper.clamp(scrollPre, minScroll, maxScroll);
        scroll = Mathf.fast(scroll, scrollPre, 12F);

        // Скроллбар
        if (contentH > viewH) {
            float ratio = viewH / contentH;
            float thumbH = Math.max(18F * scale, viewH * ratio);
            float scrollNorm = (contentH - viewH) <= 0 ? 0 : (-scroll - 0F) / (contentH - viewH);
            float thumbY = listY + (viewH - thumbH) * MathHelper.clamp(scrollNorm, 0F, 1F);

            float sbAlpha = (float) scrollbarAlpha.getValue() * openAlpha.get();
            RenderUtil.Rounded.smooth(stack,
                    panelX + width * scale - 5F * scale, listY,
                    3F * scale, viewH,
                    ColorUtil.multAlpha(ColorUtil.toGray(-1, 0.7F), sbAlpha * 0.08F),
                    Round.of(2F * scale));
            RenderUtil.Rounded.smooth(stack,
                    panelX + width * scale - 5F * scale, thumbY,
                    3F * scale, thumbH,
                    ColorUtil.multAlpha(InterFace.getInstance().clientColor(), sbAlpha * 0.55F),
                    Round.of(2F * scale));
        }

        // Инпут + кнопки
        String textToDraw = typing || !altName.isEmpty() ? altName : "Введите имя аккаунта";

        float inputY = px(panelY + (curH - 60F) * scale);
        float inputX = px(panelX - 154 * scale);
        float inputW = width * scale - 160 * scale;
        float inputH = 20F * scale;

        RenderUtil.Rounded.smooth(stack, inputX - 1 * inputFocus.get(), inputY - 1 * inputFocus.get(),
                inputW + 2 * inputFocus.get(), inputH + 2 * inputFocus.get(),
                ColorUtil.multAlpha(-1, 0.1F * (float) inputFocus.getValue() * openAlpha.get()),
                Round.of(6));

        RenderUtil.Rounded.smooth(stack, inputX, inputY, inputW, inputH,
                ColorUtil.multAlpha(ColorUtil.toGray(-1, 0.3F), openAlpha.get() / 4), Round.of(4));

        Fonts.SF_BOLD.draw(stack,
                textToDraw + (typing ? (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : ""),
                px(inputX + 7F * scale),
                px(inputY + 5.76F * scale),
                ColorUtil.multAlpha(ColorUtil.getColor(175, 175, 175), openAlpha.get()),
                7.5F * scale
        );

        // Add
        float addButtonX = px(panelX + width * scale - 309 * scale);
        float addButtonY = inputY;
        boolean addHoveredB = Mathf.isHovered(mouseX, mouseY, addButtonX, addButtonY, 35F * scale, 20F * scale);
        addHover.run(addHoveredB ? 1.0 : 0.0, 0.2, Easings.SINE_OUT, true);

        int addBase = ColorUtil.multAlpha(ColorUtil.toGray(-1, 0.3F), openAlpha.get() / 3);
        int addHoverCol = ColorUtil.overCol(addBase, InterFace.getInstance().clientColor(), 0.2F * (float) addHover.getValue());
        RenderUtil.Rounded.smooth(stack, addButtonX, addButtonY, 35F * scale, 20F * scale, addHoverCol, Round.of(4));
        Fonts.SF_BOLD.drawCenter(stack, "Add", addButtonX + 17.5F * scale, addButtonY + 5.7F * scale,
                ColorUtil.multAlpha(ColorUtil.getColor(210, 210, 210), openAlpha.get()), 7.5F * scale);

        // Random
        float randomButtonX = px(panelX + width * scale - 309 * scale);
        float randomButtonY = inputY - 25;
        boolean randomHoveredB = Mathf.isHovered(mouseX, mouseY, randomButtonX, randomButtonY, 35F * scale, 20F * scale);
        randomHover.run(randomHoveredB ? 1.0 : 0.0, 0.2, Easings.SINE_OUT, true);

        int randBase = ColorUtil.multAlpha(ColorUtil.toGray(-1, 0.3F), openAlpha.get() / 3);
        int randHoverCol = ColorUtil.overCol(randBase, InterFace.getInstance().clientColor(), 0.2F * (float) randomHover.getValue());
        RenderUtil.Rounded.smooth(stack, randomButtonX, randomButtonY, 35F * scale, 20F * scale, randHoverCol, Round.of(4));
        Fonts.SF_BOLD.drawCenter(stack, "Random", randomButtonX + 17.5F * scale, randomButtonY + 5.7F * scale,
                ColorUtil.multAlpha(ColorUtil.getColor(210, 210, 210), openAlpha.get()), 7.5F * scale);

        // чистим завершённые удаления
        altAnimations.removeIf(anim -> {
            anim.animation.update();
            if (anim.animation.getValue() == 0) {
                alts.remove(anim.alt);
                rowAnims.remove(anim.alt.name);
                return true;
            }
            return false;
        });
    }

    public void onChar(char typed) {
        if (typing) {
            // Разрешаем только a-z, A-Z, 0-9 и подчёркивание
            if ((typed >= 'a' && typed <= 'z') || (typed >= 'A' && typed <= 'Z') ||
                (typed >= '0' && typed <= '9') || typed == '_') {
                if (Fonts.SF_BOLD.getWidth(altName, 7) < panelWidth - 138 - 50) {
                    altName += typed;
                }
            }
        }
    }

    public void onKey(int key) {
        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (typing) {
            if (ctrlDown && key == GLFW.GLFW_KEY_V) {
                try {
                    String clip = GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
                    if (clip != null) altName += clip;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!altName.isEmpty()) altName = altName.substring(0, altName.length() - 1);
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                if (altName.length() >= 3) {
                    addAlt(new Alt(altName));
                    altName = "";
                }
                typing = false;
                inputFocus.run(0.0, 0.25, Easings.SINE_IN, true);
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                typing = false;
                inputFocus.run(0.0, 0.25, Easings.SINE_IN, true);
            }
        }
    }

    public void addAlt(Alt alt) {
        for (Alt existingAlt : alts) {
            if (existingAlt.name.equals(alt.name)) return;
        }
        alts.add(alt);
        RowAnims ra = this.ra(alt.name);
        ra.enter.set(0F);
        ra.enter.run(1.0, 0.45, Easings.CUBIC_OUT, true);
        saveAlts();
    }

    public void removeAlt(Alt alt) {
        altAnimations.add(new AltAnimation(alt));
        favorites.remove(alt.name);
        saveAlts();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float width = panelWidth;
        float height = panelHeight;
        this.x = mc.getMainWindow().getScaledWidth() / 2F - width / 2F;
        this.y = mc.getMainWindow().getScaledHeight() / 2F - height / 2F;

        float scale = (float) openScale.getValue();
        float curH = heightAnimation.get();
        float panelX = this.x + (1F - scale) * width / 2F + 70;
        float panelY = this.y + (1F - scale) * height / 2F;
        float inputY = panelY + (curH - 60F) * scale;

        // Add
        float addButtonX = panelX + width * scale - 309 * scale;
        float addButtonY = inputY;
        if (Mathf.isHovered((int) mouseX, (int) mouseY, (int) addButtonX, (int) addButtonY, (int) (35F * scale), (int) (20F * scale))) {
            if (button == 0 && altName.length() >= 3) {
                addAlt(new Alt(altName));
                altName = "";
            }
        }

        // Random
        float randomButtonX = panelX + width * scale - 309 * scale;
        float randomButtonY = inputY - 25;
        if (Mathf.isHovered((int) mouseX, (int) mouseY, (int) randomButtonX, (int) randomButtonY, (int) (35F * scale), (int) (20F * scale))) {
            if (button == 0) {
                String rnd = Zetrix.inst().randomNickname();
                addAlt(new Alt(rnd));
            }
        }

        // ===== Список (клики) =====
        recalcLayout(scale, width, curH, panelX, panelY);

        List<Alt> view = orderedAlts();

        float accUnits = 0F;
        int startIndex = 0;
        for (int i = 0; i < view.size(); i++) {
            float ehUnits = effectiveRowHeight(view.get(i));
            if (accUnits + ehUnits >= -scroll / scale) {
                startIndex = i;
                break;
            }
            accUnits += ehUnits;
        }
        int maxToCheck = Math.min(view.size() - startIndex, Math.max(visibleRows + 2, 8));
        float yCursorUnits = accUnits;

        float rightEdgeAbs = panelX + width * scale - RHS_PAD * scale;
        for (int i = 0; i < maxToCheck; i++) {
            Alt alt = view.get(startIndex + i);

            float rowK = removeCoef(alt);
            float rowY = listY + yCursorUnits * scale + scroll;
            float rowW = width * scale - 8F * scale;
            float rowH = (rowHeight - 2F) * scale * Math.max(0.0001F, rowK);

            // клик по строке — выбрать
            if (Mathf.isHovered((int) mouseX, (int) mouseY,
                    (int) (listX + 4F * scale), (int) rowY, (int) (rowW - 22F * scale), (int) (rowH + 2F * scale))) {
                if (button == 0) {
                    mc.session = new Session(alt.name, UUID.randomUUID().toString(), "", "mojang");
                    saveAlts();
                    RowAnims a = ra(alt.name);
                    a.select.run(1.0, 0.35, Easings.CUBIC_OUT, true);
                }
            }

            // STAR toggle
            float starSize = ICON_SIZE * scale;
            float starCenterX = rightEdgeAbs - ICON_GAP * scale;
            float starTLx = starCenterX - starSize / 2F;
            float starTLy = rowY + (rowH - starSize) / 2F;
            if (Mathf.isHovered((int) mouseX, (int) mouseY,
                    (int) (starTLx - 2F * scale), (int) (starTLy - 2F * scale),
                    (int) (starSize + 4F * scale), (int) (starSize + 4F * scale))) {
                if (button == 0) {
                    toggleFavorite(alt.name);
                    return true;
                }
            }

            // DELETE
            float delSize = ICON_SIZE * scale;
            float delCenterX = rightEdgeAbs;
            float delTLx = delCenterX - delSize / 2F;
            float delTLy = rowY + (rowH - delSize) / 2F;
            if (Mathf.isHovered((int) mouseX, (int) mouseY,
                    (int) (delTLx - 2F * scale), (int) (delTLy - 2F * scale),
                    (int) (delSize + 4F * scale), (int) (delSize + 4F * scale))) {
                if (button == 0) removeAlt(alt);
            }

            yCursorUnits += effectiveRowHeight(alt);
        }

        // Поле ввода
        float inputX = panelX - 154 * scale;
        if (Mathf.isHovered((int) mouseX, (int) mouseY, (int) inputX, (int) inputY, (int) (width * scale - 160 * scale), (int) (20F * scale))) {
            typing = !typing;
            inputFocus.run(typing ? 1.0 : 0.0, 0.25, Easings.SINE_OUT, true);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        updateScroll((int) mouseX, (int) mouseY, (float) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onKey(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        onChar(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        saveAlts();
    }

    // Примечание: класс Alt должен иметь public final String name;
    // public class Alt { public final String name; public Alt(String n){ this.name=n; } }
}
