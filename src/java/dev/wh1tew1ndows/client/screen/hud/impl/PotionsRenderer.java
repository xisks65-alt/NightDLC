package dev.wh1tew1ndows.client.screen.hud.impl;

import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.screen.hud.AbstractHud;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.ПенисУтилита;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Namespaced;
import net.minecraft.util.StringUtils;
import net.minecraft.util.registry.Registry;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.Collection;

public class PotionsRenderer extends AbstractHud implements IRenderer {

    private final DragSetting drag;

    // ---- размеры/отступы под стиль KeybindsRenderer ----
    private static final float MARGIN = 5f;
    private static final float TITLE_SIZE = 7.5f;
    private static final float HDR_ICON_SIZE = 6.0f;     // иконка в шапке (шрифт ICON_DIMAS)
    private static final float ROW_FONT = 7.0f;
    private static final float ROW_GAP = 3.0f;           // межстрочный зазор как в кейбиндах
    private static final float RADIUS = 6f;
    private static final float BETWEEN_TEXT_GAP = 8f;    // зазор между именем и длительностью
    private static final float EFFECT_ICON_W = 8f;       // слот под иконку эффекта справа

    // ---- FPS-independent сглаживание размеров панели ----
    private static final float SIZE_SMOOTH_SPEED = 12f; // скорость подтягивания к цели (1/сек)
    private float smoothWidth = -1f;
    private float smoothHeight = -1f;
    private long lastTimeNs = 0L;

    public PotionsRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void render(Render2DEvent event) {
        Collection<EffectInstance> active = mc.player.getActivePotionEffects();


        for (EffectInstance effect : active) {
            effect.getAnimation().update();
            effect.getAnimation().run((effect.getDuration() / 20F) <= 1F ? 0 : 1, 0.2F, Easings.SINE_OUT, false);
        }

        MatrixStack matrix = event.getMatrix();

        //if (InterFace.getInstance().sicretNastriokaEbana.getValue()) {

            final String title = "Active potions";
            final boolean isEmpty = active.isEmpty();


            float dt = computeDtSeconds();


            final float headerH = MARGIN + ROW_FONT + MARGIN;


            float titleW = Fonts.MONTSERRAT_MEDIUM.getWidth(title, TITLE_SIZE);
            float titleIconW = Fonts.ICON_DIMAS.getWidth("B", HDR_ICON_SIZE); // метрика любой иконки
            float headerWidth = MARGIN + 1f + titleW + MARGIN + titleIconW + MARGIN;

            float maxRowW = 0f;
            for (EffectInstance potion : active) {
                String leftText = potion.getPotion().getDisplayName().getString() + " " + (potion.getAmplifier() + 1);
                String rightText = getDuration(potion);

                float leftW = Fonts.MONTSERRAT_MEDIUM.getWidth(leftText, ROW_FONT);
                float rightW = Fonts.MONTSERRAT_MEDIUM.getWidth(rightText, ROW_FONT);


                float rowW = MARGIN + leftW + BETWEEN_TEXT_GAP + rightW + 8f + MARGIN;
                if (rowW > maxRowW) maxRowW = rowW;
            }

            float targetWidth = Math.max(90, Math.max(headerWidth, maxRowW));


            if (smoothWidth < 0f) smoothWidth = targetWidth;
            if (smoothHeight < 0f) smoothHeight = headerH;


            smoothWidth = smoothTowards(smoothWidth, targetWidth, dt, SIZE_SMOOTH_SPEED);
            drag.size.x = (float) Mathf.step(smoothWidth, 0.5);


            float x = drag.position.x;
            float y = drag.position.y;

            boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
            update(closeCondition ? 0 : 1);
            drag.active = !closeCondition;
            if (closeCondition && animValue() == 0.0F) return;


            x = (float) Mathf.step(x, 0.5);
            y = (float) Mathf.step(y, 0.5);
            float width = (float) Mathf.step(drag.size.x, 0.5);

            matrix.push();
            float scale = this.animation.get();
            matrix.translate((x + width / 2F), (y + drag.size.y / 2F), 0);
            matrix.scale(scale, scale, 0);
            matrix.translate(-(x + width / 2F), -(y + drag.size.y / 2F), 0);

            // InterFace.getInstance().drawClientRect(matrix, x, y, width, 16, animation.get(), 4);

            ПенисУтилита.КрутойПенаРект(matrix, x, y, width, drag.size.y, animation.get());

            RenderUtil.Rounded.smooth(matrix, x, y, width, 16, ColorUtil.replAlpha(new Color(0xFF000000, true).getRGB(), animation.get() * 0.15F), Round.of(0, 6, 0, 6));

            RectUtil.drawRect(matrix, x + 1, y + 16, width - 2, 0.8F, ColorUtil.getColor(255, (int) (4 * animation.getValue())));

            Fonts.MONTSERRAT_BOLD.draw(
                    matrix,
                    title,
                    x + MARGIN + 4 + Fonts.ICON_DIMAS.getWidth("O", 5.5F),
                    y + (headerH / 2F) - (7.0F / 2F) - 0.6F,
                    ColorUtil.multAlpha(ColorUtil.getColor(160), animValue()),
                    6.5F
            );

            Fonts.ICON_DIMAS.draw(
                    matrix,
                    "O",
                    x + MARGIN + 1.5F,
                    y + (headerH / 2F) - (7.0F / 2F) + 0.2F,
                    ColorUtil.multAlpha(ColorUtil.fade(), animValue()),
                    5.5F
            );


            //RectU

            float offsetY = 0f;
            float ys = -2.4F;

            for (EffectInstance potion : active) {
                float animPC = potion.getAnimation().get();
                if (animPC <= 0f) continue;

                float baseY = y + ys + headerH + MARGIN + offsetY;

                long time = System.currentTimeMillis();
                float frequency = 2.0f;
                float alphaWave = (float) ((Math.sin(time / 1000.0 * frequency * Math.PI) + 1) / 2);

                int alpha = 0;
                if (potion.getDuration() < 100) {
                    alpha = (int) ((100 + 155 * alphaWave) * animPC);
                } else {
                    alpha = (int) (255 * animPC);
                }
                boolean bad = isBad(potion);

                int textClrBase = bad ? ColorUtil.getColor(200, 105, 105, alpha) : ColorUtil.replAlpha(ColorUtil.getColor(180), alpha);
                int textClr = ColorUtil.replAlpha(textClrBase, alpha);

                int textClrBase2 = bad ? ColorUtil.getColor(200, 105, 105, alpha) : ColorUtil.replAlpha(ColorUtil.fade(), alpha);
                int textClr2 = ColorUtil.replAlpha(textClrBase, alpha);


                String leftText = potion.getPotion().getDisplayName().getString() + " " + ColorFormatting.getColor(textClr) + String.valueOf((potion.getAmplifier() + 1)).replace("1", "");


                String rightText = getDuration(potion);

                float xanim = 0;

                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        leftText,
                        x + 12.5F,
                        baseY + ys - MARGIN + 8.6F,
                        textClr,
                        6.5F
                );

                Fonts.MONTSERRAT_BOLD.drawRight(
                        matrix,
                        rightText,
                        x + width - MARGIN + 0.5F,
                        baseY + ys - MARGIN + 8.6F,
                        textClrBase2,
                        6.5F
                );


                try {
                    String effectKey = Registry.EFFECTS.getKey(potion.getPotion()).getPath();
                    String iconPath = "effect/" + effectKey + ".png";
                    mc.getTextureManager().bindTexture(new Namespaced(iconPath));
                    RectUtil.drawRect(
                            matrix,
                            x + 5,
                            baseY - MARGIN + 6.5F,
                            6, 6,
                            ColorUtil.replAlpha(ColorUtil.getColor(255), alpha),
                            false, true
                    );
                } catch (Throwable ignored) {

                }

                offsetY += (9) * animPC;
            }


            float targetHeight = headerH + offsetY + (MARGIN * 2F) - 3;
            smoothHeight = smoothTowards(smoothHeight, targetHeight, dt, SIZE_SMOOTH_SPEED);
            drag.size.y = (float) Mathf.step(targetHeight, 0.5);

            matrix.pop();
      /*  } else {

            final String title = "Active potions";
            final boolean isEmpty = active.isEmpty();


            float dt = computeDtSeconds();


            final float headerH = MARGIN + ROW_FONT + MARGIN;


            float titleW = Fonts.MONTSERRAT_MEDIUM.getWidth(title, TITLE_SIZE);
            float titleIconW = Fonts.ICON_DIMAS.getWidth("B", HDR_ICON_SIZE); // метрика любой иконки
            float headerWidth = MARGIN + 1f + titleW + MARGIN + titleIconW + MARGIN;

            float maxRowW = 0f;
            for (EffectInstance potion : active) {
                String leftText = potion.getPotion().getDisplayName().getString() + " " + (potion.getAmplifier() + 1);
                String rightText = getDuration(potion);

                float leftW = Fonts.MONTSERRAT_MEDIUM.getWidth(leftText, ROW_FONT);
                float rightW = Fonts.MONTSERRAT_MEDIUM.getWidth(rightText, ROW_FONT);


                float rowW = MARGIN + leftW + BETWEEN_TEXT_GAP + rightW + 8f + MARGIN;
                if (rowW > maxRowW) maxRowW = rowW;
            }

            float targetWidth = Math.max(90, Math.max(headerWidth, maxRowW));


            if (smoothWidth < 0f) smoothWidth = targetWidth;
            if (smoothHeight < 0f) smoothHeight = headerH;


            smoothWidth = smoothTowards(smoothWidth, targetWidth, dt, SIZE_SMOOTH_SPEED);
            drag.size.x = (float) Mathf.step(smoothWidth, 0.5);


            float x = drag.position.x;
            float y = drag.position.y;

            boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
            update(closeCondition ? 0 : 1);
            drag.active = !closeCondition;
            if (closeCondition && animValue() == 0.0F) return;


            x = (float) Mathf.step(x, 0.5);
            y = (float) Mathf.step(y, 0.5);
            float width = (float) Mathf.step(drag.size.x, 0.5);

            matrix.push();

            InterFace.getInstance().drawClientRect(matrix, x, y, width, 16, animation.get(), 4);


            Fonts.MONTSERRAT_MEDIUM.draw(
                    matrix,
                    title,
                    x + MARGIN + 8 + 7,
                    y + (headerH / 2F) - (7.5F / 2F) - 0.6F,
                    textWhite(),
                    7.7F
            );

            RectUtil.drawRect(matrix, x + MARGIN + 11, y + 3.85F, 0.5F, 8, ColorUtil.getColor(255, 0.1F * animation.get()));

            Fonts.ICON_NURIK.draw(
                    matrix,
                    "E",
                    x + MARGIN,
                    y + MARGIN - 0.6F,
                    textWhite(),
                    8
            );


            float offsetY = 0f;
            float ys = -2.5F;

            for (EffectInstance potion : active) {
                float animPC = potion.getAnimation().get();
                if (animPC <= 0f) continue;

                float baseY = y + ys + headerH + MARGIN + offsetY;

                long time = System.currentTimeMillis();
                float frequency = 2.0f;
                float alphaWave = (float) ((Math.sin(time / 1000.0 * frequency * Math.PI) + 1) / 2);

                int alpha = 0;
                if (potion.getDuration() < 100) {
                    alpha = (int) ((100 + 155 * alphaWave) * animPC);
                } else {
                    alpha = (int) (255 * animPC);
                }
                boolean bad = isBad(potion);

                int textClrBase = bad ? ColorUtil.getColor(200, 105, 105, alpha) : ColorUtil.replAlpha(textWhite(), alpha);
                int textClr = ColorUtil.replAlpha(textClrBase, alpha);


                String leftText = potion.getPotion().getDisplayName().getString() + " " + ColorFormatting.getColor(textClr) + String.valueOf((potion.getAmplifier() + 1)).replace("1", "");


                String rightText = getDuration(potion);

                float xanim = 0;

                InterFace.getInstance().drawClientRect(matrix, x + xanim, baseY + ys, width, 12, animation.get() * animPC, 4);


                RectUtil.drawRect(matrix, x + width - MARGIN - 7, baseY + ys + 3, 0.5F, 6, ColorUtil.getColor(255, 0.1F * animation.get() * animPC));


                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        leftText,
                        x + 4 + xanim,
                        baseY + ys - MARGIN + 7.8F,
                        textClr,
                        6.7F
                );

                Fonts.MONTSERRAT_BOLD.drawRight(
                        matrix,
                        rightText,
                        x + width - MARGIN + xanim - 9,
                        baseY + ys - MARGIN + 7.8F,
                        textClr,
                        6.7F
                );


                try {
                    String effectKey = Registry.EFFECTS.getKey(potion.getPotion()).getPath();
                    String iconPath = "effect/" + effectKey + ".png";
                    mc.getTextureManager().bindTexture(new Namespaced(iconPath));
                    RectUtil.drawRect(
                            matrix,
                            x + width - MARGIN - 5 + xanim,
                            baseY - MARGIN + 4.5F,
                            EFFECT_ICON_W, EFFECT_ICON_W,
                            ColorUtil.replAlpha(ColorUtil.getColor(255), alpha),
                            false, true
                    );
                } catch (Throwable ignored) {

                }

                offsetY += (12.5F) * animPC;
            }


            float targetHeight = headerH + offsetY + (MARGIN * 2F) - 5.5F;
            smoothHeight = smoothTowards(smoothHeight, targetHeight, dt, SIZE_SMOOTH_SPEED);
            drag.size.y = (float) Mathf.step(smoothHeight, 0.5);

            matrix.pop();
        }*/

    }

    // ===== helpers =====

    private boolean isBad(EffectInstance potion) {
        return potion.getPotion().equals(Effects.SLOWNESS)
                || potion.getPotion().equals(Effects.BLINDNESS)
                || potion.getPotion().equals(Effects.POISON)
                || potion.getPotion().equals(Effects.WITHER)
                || potion.getPotion().equals(Effects.HUNGER)
                || potion.getPotion().equals(Effects.NAUSEA)
                || potion.getPotion().equals(Effects.WEAKNESS);
    }

    public static String getDuration(EffectInstance potion) {
        if (potion.getIsPotionDurationMax()) return "**:**";
        return StringUtils.ticksToElapsedTime(potion.getDuration());
    }

    /**
     * FPS-independent мигание, когда осталось ≤10 c.
     * Частота растёт к 0, альфа в диапазоне 0.35..1.0
     */
    // ===== FPS-independent мигание (исправлено) =====
    private float blinkTimer = 0f;

    private float blinkAlphaFor(EffectInstance potion) {
        float secondsLeft = potion.getDuration() / 20F;
        if (secondsLeft > 10F) return 1F;

        // вычисляем дельту времени (чтобы мигание не зависело от FPS)
        float dt = computeDtSeconds();

        // частота мигания (чем меньше времени — тем чаще мигает)
        float k = 1F - Math.min(secondsLeft, 10F) / 10F; // 0..1
        float baseHz = 1.2F, maxHz = 3.0F; // замедлил максимум, чтобы не мигало быстро
        float hz = baseHz + (maxHz - baseHz) * k;

        // обновляем "время фазы" на основе частоты и dt
        blinkTimer += hz * dt;
        if (blinkTimer > 1f) blinkTimer -= 1f; // цикл 0..1

        // плавная синусоида 0.35..1.0
        float wave = 0.5F + 0.5F * (float) Math.sin(blinkTimer * 2F * (float) Math.PI);
        float minA = 0.35F, maxA = 1.0F;
        return minA + (maxA - minA) * wave;
    }


    // экспоненциальное сглаживание к цели с учётом dt (FPS-independent)
    private float smoothTowards(float current, float target, float dt, float speedPerSec) {
        if (!Float.isFinite(dt) || dt <= 0f) return target;
        float k = 1f - (float) Math.exp(-speedPerSec * dt); // 0..1
        return current + (target - current) * k;
    }

    private float computeDtSeconds() {
        long now = System.nanoTime();
        if (lastTimeNs == 0L) {
            lastTimeNs = now;
            return 1f / 60f; // первый кадр — ~16.7мс
        }
        long d = now - lastTimeNs;
        lastTimeNs = now;
        double dt = Math.min(Math.max(d / 1_000_000_000.0, 0.0), 0.1); // [0 .. 100мс]
        return (float) dt;
    }
}
