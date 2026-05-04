package dev.wh1tew1ndows.client.screen.hud.impl;

import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.screen.hud.AbstractHud;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.font.StripFont;
import dev.wh1tew1ndows.client.utils.ПенисУтилита;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CooldownRenderer extends AbstractHud implements IRenderer {

    private final DragSetting drag;

    public CooldownRenderer(DragSetting drag) {
        this.drag = drag;
    }

    // — стильный триммер (на будущее; сейчас рендерим без обрезки, чтобы не ловить NPE от сторонних шрифтов)
    private final StripFont stripFont = new StripFont();

    // — анимации появления по айтему
    private final Map<Item, Long> appearStart = new HashMap<>();
    private static final long APPEAR_DUR_MS = 220L;

    // — размеры/отступы под стиль KeybindsRenderer
    private static final float TITLE_SIZE = 7.5F;
    private static final float ROW_SIZE = 7.0F;
    private static final float ICON_TITLE_SIZE = 6.0F; // иконка в шапке
    private static final float ICON_ROW_SCALE = 0.5F;  // масштаб иконки предмета в строке
    private static final float ICON_ROW_CELL = 10f;    // «ячейка» под иконку слева
    private static final float MARGIN = 5f;
    private static final float BETWEEN_TEXT_GAP = 8f;  // зазор перед таймером справа

    // — FPS-независимое сглаживание размеров (как в KeybindsRenderer)
    private static final float SIZE_SMOOTH_SPEED = 12f;
    private float smoothWidth = -1f;
    private float smoothHeight = -1f;
    private long lastTimeNs = 0L;

    // — внутренняя метрика времени
    private long lastNsForWidth = System.nanoTime();
    private float smoothWidthLegacy = 100f; // чтобы мягко стартануть даже при пустом dt на первом кадре

    @Override
    public void render(Render2DEvent event) {
        MatrixStack matrix = event.getMatrix();

        CooldownTracker tracker = mc.player.getCooldownTracker();
        Map<Item, CooldownTracker.Cooldown> cooldowns = tracker.cooldowns;

        //if (InterFace.getInstance().sicretNastriokaEbana.getValue()) {
            final String title = "Cooldowns";
            final boolean isEmpty = cooldowns.isEmpty();

            // — высота хэдера 1в1 как в Keybinds
            final float headerH = MARGIN + ROW_SIZE + MARGIN;

            // — вход/выход и скрытие при пустоте (как у Keybinds)
            boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
            update(closeCondition ? 0 : 1);
            drag.active = !closeCondition;
            if (closeCondition && animValue() == 0.0F) return;

            // — авто-ширина: считаем целевую ширину под шапку и все строки
            float targetWidth = computeTargetWidth(title, cooldowns);

            // — инициализируем сглаженные размеры
            if (smoothWidth < 0f) smoothWidth = targetWidth;
            if (smoothHeight < 0f) smoothHeight = headerH;

            // — FPS-независимое подтягивание к целям
            float dt = computeDtSeconds();
            smoothWidth = smoothTowards(smoothWidth, targetWidth, dt, SIZE_SMOOTH_SPEED);

            // (доп. мягкость старта на первом кадре — эквивалентно твоей экспоненте)
            float dtLegacy = computeDtLegacy();
            smoothWidthLegacy += (targetWidth - smoothWidthLegacy) * (1f - (float) Math.exp(-5 * Math.max(0f, dtLegacy)));
            // берём «лучшее» из обеих метрик
            float widthSmoothed = (smoothWidth + smoothWidthLegacy) * 0.55f;

            // — позиция и снап к 0.5 пикселя
            float x = drag.position.x;
            float y = drag.position.y;

            x = (float) Mathf.step(x, 0.5);
            y = (float) Mathf.step(y, 0.5);
            float width = (float) Mathf.step(widthSmoothed, 0.5);
            drag.size.x = width;

            matrix.push();
            float scale = this.animation.get();
            matrix.translate((x + width / 2F), (y + drag.size.y / 2F), 0);
            matrix.scale(scale, scale, 0);
            matrix.translate(-(x + width / 2F), -(y + drag.size.y / 2F), 0);

            // === ШАПКА (как в KeybindsRenderer) ===

            ПенисУтилита.КрутойПенаРект(matrix, x, y, width, drag.size.y, animation.get());

            RenderUtil.Rounded.smooth(matrix, x, y, width, 16, ColorUtil.replAlpha(new Color(0xFF000000, true).getRGB(), animation.get() * 0.15F), Round.of(0, 6, 0, 6));

            RectUtil.drawRect(matrix, x + 1, y + 16, width - 2, 0.8F, ColorUtil.getColor(255, (int) (4 * animation.getValue())));


            Fonts.MONTSERRAT_BOLD.draw(
                    matrix,
                    title,
                    x + MARGIN + 4 + Fonts.ICON_DESHUX.getWidth("i", 6),
                    y + (headerH / 2F) - (7.0F / 2F) - 0.6F,
                    ColorUtil.multAlpha(ColorUtil.getColor(160), animValue()),
                    6.5F
            );

            Fonts.ICON_DESHUX.draw(
                    matrix,
                    "i",
                    x + MARGIN + 1,
                    y + (headerH / 2F) - (7.0F / 2F) + 0.55F,
                    ColorUtil.multAlpha(ColorUtil.fade(), animValue()),
                    6
            );

            // === СТРОКИ ===
            float offset = 0f;
            long nowMs = Util.milliTime();
            float ys = -2.35F;


            // поддержка появлений
            appearStart.keySet().retainAll(cooldowns.keySet());
            for (Item it : cooldowns.keySet()) {
                appearStart.putIfAbsent(it, nowMs);
            }

            Iterator<Map.Entry<Item, CooldownTracker.Cooldown>> it = cooldowns.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Item, CooldownTracker.Cooldown> entry = it.next();
                Item item = entry.getKey();

                long currentTick = mc.player.getCooldownTracker().getTicks();
                long expireTick = entry.getValue().getExpireTicks();
                long remaining = expireTick - currentTick;

                if (remaining <= 0) {
                    it.remove();
                    appearStart.remove(item);
                    continue;
                }

                // 0..1
                float appear = easeOutCubic((Util.milliTime() - appearStart.getOrDefault(item, nowMs)) / (float) APPEAR_DUR_MS);
                if (appear <= 0f) continue;

                float rowBaseY = y + ys + headerH + MARGIN + offset;
                float xanim = 0;

                matrix.push();
                //InterFace.getInstance().drawClientRect(matrix, x + xanim, rowBaseY + ys, width, 12, animation.get() * appear, 4);
                String timeStr = formatTime(remaining / 20.0f);

                // подложка строки (тем же клиент-стилем, мягко)
                //RenderUtil.clientStyledRect(matrix, x + slideX, rowBaseY - MARGIN + 1f, width, 12, appear, 4.5F);

                // иконка предмета слева
                drawItemStack(
                        new ItemStack(item),
                        x + 5,
                        rowBaseY + ys - MARGIN + 7.7F,
                        false,
                        true,
                        ICON_ROW_SCALE
                );

                // имя предмета
                String itemName = item.getName().getString();
                int leftColor = ColorUtil.multAlpha(textWhite(), appear);
                float nameX = x + 4.5F;

                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        itemName,
                        nameX + 11,
                        rowBaseY + ys - MARGIN + 8.6F,
                        ColorUtil.multAlpha(ColorUtil.getColor(180), appear),
                        6.5F
                );

                // таймер справа (правое выравнивание, как keyName)

                int rightColor = ColorUtil.multAlpha(textColor(), appear);
                float rightX = x + width - MARGIN;


                Fonts.MONTSERRAT_BOLD.drawRight(
                        matrix,
                        timeStr,
                        rightX,
                        rowBaseY + ys - MARGIN + 8.6F,
                        ColorUtil.multAlpha(ColorUtil.fade(), appear),
                        6.5F
                );

                matrix.pop();

                offset += (9) * appear;
            }

            // — целевая высота панели, плавно тянем
            float targetHeight = headerH + offset + (MARGIN * 2F) - 3;
            smoothHeight = smoothTowards(smoothHeight, targetHeight, dt, SIZE_SMOOTH_SPEED);
            drag.size.y = (float) Mathf.step(smoothHeight, 0.5);

            matrix.pop();
      /*  } else {

            final String title = "Cooldowns";
            final boolean isEmpty = cooldowns.isEmpty();

            // — высота хэдера 1в1 как в Keybinds
            final float headerH = MARGIN + ROW_SIZE + MARGIN;

            // — вход/выход и скрытие при пустоте (как у Keybinds)
            boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
            update(closeCondition ? 0 : 1);
            drag.active = !closeCondition;
            if (closeCondition && animValue() == 0.0F) return;

            // — авто-ширина: считаем целевую ширину под шапку и все строки
            float targetWidth = computeTargetWidth(title, cooldowns);

            // — инициализируем сглаженные размеры
            if (smoothWidth < 0f) smoothWidth = targetWidth;
            if (smoothHeight < 0f) smoothHeight = headerH;

            // — FPS-независимое подтягивание к целям
            float dt = computeDtSeconds();
            smoothWidth = smoothTowards(smoothWidth, targetWidth, dt, SIZE_SMOOTH_SPEED);

            // (доп. мягкость старта на первом кадре — эквивалентно твоей экспоненте)
            float dtLegacy = computeDtLegacy();
            smoothWidthLegacy += (targetWidth - smoothWidthLegacy) * (1f - (float) Math.exp(-5 * Math.max(0f, dtLegacy)));
            // берём «лучшее» из обеих метрик
            float widthSmoothed = (smoothWidth + smoothWidthLegacy) * 0.55f;

            // — позиция и снап к 0.5 пикселя
            float x = drag.position.x;
            float y = drag.position.y;

            x = (float) Mathf.step(x, 0.5);
            y = (float) Mathf.step(y, 0.5);
            float width = (float) Mathf.step(widthSmoothed, 0.5);
            drag.size.x = width;

            matrix.push();

            // === ШАПКА (как в KeybindsRenderer) ===

            InterFace.getInstance().drawClientRect(matrix, x, y, width, 16, animation.get(), 4);


            Fonts.MONTSERRAT_MEDIUM.draw(
                    matrix,
                    title,
                    x + MARGIN + 8 + 8,
                    y + (headerH / 2F) - (7.5F / 2F) - 0.6F,
                    textWhite(),
                    7.7F
            );

            RectUtil.drawRect(matrix, x + MARGIN + 13, y + 3.85F, 0.5F, 8, ColorUtil.getColor(255, 0.1F * animation.get()));

            Fonts.ICON_NURIK.draw(
                    matrix,
                    "T",
                    x + MARGIN,
                    y + MARGIN - 0.6F,
                    textWhite(),
                    9
            );

            // === СТРОКИ ===
            float offset = 0f;
            long nowMs = Util.milliTime();
            float ys = -2.5F;


            // поддержка появлений
            appearStart.keySet().retainAll(cooldowns.keySet());
            for (Item it : cooldowns.keySet()) {
                appearStart.putIfAbsent(it, nowMs);
            }

            Iterator<Map.Entry<Item, CooldownTracker.Cooldown>> it = cooldowns.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Item, CooldownTracker.Cooldown> entry = it.next();
                Item item = entry.getKey();

                long currentTick = mc.player.getCooldownTracker().getTicks();
                long expireTick = entry.getValue().getExpireTicks();
                long remaining = expireTick - currentTick;

                if (remaining <= 0) {
                    it.remove();
                    appearStart.remove(item);
                    continue;
                }

                // 0..1
                float appear = easeOutCubic((Util.milliTime() - appearStart.getOrDefault(item, nowMs)) / (float) APPEAR_DUR_MS);
                if (appear <= 0f) continue;

                float rowBaseY = y + ys + headerH + MARGIN + offset;
                float xanim = 0;

                matrix.push();
                InterFace.getInstance().drawClientRect(matrix, x + xanim, rowBaseY + ys, width, 12, animation.get() * appear, 4);
                String timeStr = formatTime(remaining / 20.0f);
                RectUtil.drawRect(matrix, x + width - MARGIN - 7, rowBaseY + ys + 3, 0.5F, 6, ColorUtil.getColor(255, 0.1F * animation.get() * appear));

                // подложка строки (тем же клиент-стилем, мягко)
                //RenderUtil.clientStyledRect(matrix, x + slideX, rowBaseY - MARGIN + 1f, width, 12, appear, 4.5F);

                // иконка предмета слева
                drawItemStack(
                        new ItemStack(item),
                        x + width - MARGIN - 5.5F + xanim,
                        rowBaseY + ys - MARGIN + 6.7F,
                        false,
                        true,
                        ICON_ROW_SCALE
                );

                // имя предмета
                String itemName = item.getName().getString();
                int leftColor = ColorUtil.multAlpha(textWhite(), appear);
                float nameX = x + 4.5F;

                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        itemName,
                        nameX - 1 + xanim,
                        rowBaseY + ys - MARGIN + 7.8F,
                        leftColor,
                        6.7F
                );

                // таймер справа (правое выравнивание, как keyName)

                int rightColor = ColorUtil.multAlpha(textColor(), appear);
                float rightX = x + width - MARGIN + (3 - (3 * appear));


                Fonts.MONTSERRAT_BOLD.drawRight(
                        matrix,
                        timeStr,
                        rightX + xanim - 9,
                        rowBaseY + ys - MARGIN + 7.8F,
                        leftColor,
                        6.7F
                );

                matrix.pop();

                offset += (12.5F) * appear;
            }

            // — целевая высота панели, плавно тянем
            float targetHeight = headerH + offset + (MARGIN * 2F) - 5.5F;
            smoothHeight = smoothTowards(smoothHeight, targetHeight, dt, SIZE_SMOOTH_SPEED);
            drag.size.y = (float) Mathf.step(smoothHeight, 0.5);

            matrix.pop();
        } */

    }

    // === РЕНДЕР ИКОНКИ ПРЕДМЕТА ===
    public static void drawItemStack(ItemStack stack, float x, float y, boolean overlay, boolean scale, float scaleValue) {
        RenderSystem.enableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();

        RenderSystem.translatef(x, y, 0);
        if (scale) GL11.glScaled(scaleValue, scaleValue, scaleValue);

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (overlay) mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        RenderSystem.disableDepthTest();
    }

    // === ВСПОМОГАТЕЛЬНОЕ ===
    private float computeDtSeconds() {
        long now = System.nanoTime();
        if (lastTimeNs == 0L) {
            lastTimeNs = now;
            return 1f / 60f;
        }
        long d = now - lastTimeNs;
        lastTimeNs = now;
        double dt = Math.min(Math.max(d / 1_000_000_000.0, 0.0), 0.1); // clamp [0..100мс]
        return (float) dt;
    }

    private float computeDtLegacy() {
        long now = System.nanoTime();
        float dt = (now - lastNsForWidth) / 1_000_000_000f;
        lastNsForWidth = now;
        return dt;
    }

    // экспоненциальное сглаживание к цели
    private float smoothTowards(float current, float target, float dt, float speedPerSec) {
        if (!Float.isFinite(dt) || dt <= 0f) return target;
        float k = 1f - (float) Math.exp(-speedPerSec * dt);
        return current + (target - current) * k;
    }

    private static float easeOutCubic(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private String formatTime(float seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", mins, secs);
    }

    public static String getDuration(EffectInstance potion) {
        if (potion.getIsPotionDurationMax()) {
            return "**:**";
        } else {
            return StringUtils.ticksToElapsedTime(potion.getDuration());
        }
    }

    // — расчёт целевой ширины под стиль Keybinds
    private float computeTargetWidth(String title, Map<Item, CooldownTracker.Cooldown> cooldowns) {
        float maxW = 0f;

        // ширина заголовка + левая иконка
        float titleW = Fonts.MONTSERRAT_MEDIUM.getWidth(title, TITLE_SIZE);
        float titleIconW = Fonts.ICON_DIMAS.getWidth("T", ICON_TITLE_SIZE);
        float headerWidth = MARGIN + 1f + titleW + MARGIN /* зазор */ + titleIconW + MARGIN;
        maxW = Math.max(maxW, headerWidth);

        // ширина примерного таймера
        float timerSampleW = Fonts.MONTSERRAT_MEDIUM.getWidth("00:00", ROW_SIZE);

        // строки: [margin] [itemIconCell] [name] [gap] [timer(right)] [margin]
        for (Map.Entry<Item, CooldownTracker.Cooldown> e : cooldowns.entrySet()) {
            String name = e.getKey().getName().getString();
            float nameW = Fonts.MONTSERRAT_MEDIUM.getWidth(name, ROW_SIZE);
            float rowWidth = MARGIN
                    + ICON_ROW_CELL
                    + nameW
                    + BETWEEN_TEXT_GAP
                    + timerSampleW
                    + MARGIN;
            maxW = Math.max(maxW, rowWidth);
        }

        // минималка и снап
        maxW = Math.max(maxW, 86f);
        return (float) Mathf.step(maxW, 0.5);
    }
}
