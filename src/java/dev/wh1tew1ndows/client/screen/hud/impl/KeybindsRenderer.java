package dev.wh1tew1ndows.client.screen.hud.impl;

import com.google.common.collect.Lists;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.screen.hud.AbstractHud;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.ПенисУтилита;
import net.minecraft.client.gui.screen.ChatScreen;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class KeybindsRenderer extends AbstractHud implements IRenderer {
    private final List<Module> bindings = Lists.newArrayList();
    private final DragSetting drag;

    // размеры шрифтов/отступы
    private static final float TITLE_SIZE = 7.0F;
    private static final float ROW_SIZE = 7F;
    private static final float ICON_TITLE_SIZE = 6.0F; // иконка в шапке ("B")
    private static final float ICON_ROW_SIZE = 6.7F;   // иконка категории в строке
    private static final float MARGIN = 5f;
    private static final float NAME_LEFT_GAP = 10f;      // x + margin + 10 для имени
    private static final float BETWEEN_TEXT_GAP = 8f;    // зазор между именем и ключом

    // === FPS-independent сглаживание размеров панели ===
    private static final float SIZE_SMOOTH_SPEED = 12f; // скорость подтягивания к цели (в 1/сек)
    private float smoothWidth = -1f;
    private float smoothHeight = -1f;
    private float smoothMaxKeyWidth = -1f; // плавное сглаживание максимальной ширины клавиш
    private long lastTimeNs = 0L;

    public KeybindsRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void render(Render2DEvent event) {

        float dt = computeDtSeconds();


        Zetrix.inst().moduleManager().values().forEach(m -> m.getAnimation().update());
        sortModules();

      //  if (InterFace.getInstance().sicretNastriokaEbana.getValue()) {


            MatrixStack matrix = event.getMatrix();

            final String title = "Key Binds";
            final boolean isEmpty = bindings.isEmpty();

            final float headerH = MARGIN + ROW_SIZE + MARGIN;


            float targetWidth = computeTargetWidth(title);


            if (smoothWidth < 0f) smoothWidth = targetWidth;
            if (smoothHeight < 0f) smoothHeight = headerH;


            smoothWidth = smoothTowards(smoothWidth, targetWidth + 4, dt, SIZE_SMOOTH_SPEED);


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


            //InterFace.getInstance().drawClientRect(matrix, x, y, width, 16, animation.get(), 4);


            float rad = 5;


            //RenderUtil.Rounded.smooth(matrix, x, y, width, drag.size.y, ColorUtil.replAlpha(new Color(0x222627).getRGB(), animation.get()), Round.of(6));


            ПенисУтилита.КрутойПенаРект(matrix, x, y, width, drag.size.y, animation.get());

            RenderUtil.Rounded.smooth(matrix, x, y, width, 16, ColorUtil.replAlpha(new Color(0xFF000000, true).getRGB(), animation.get() * 0.15F), Round.of(0, 6, 0, 6));

            RectUtil.drawRect(matrix, x + 1, y + 16, width - 2, 0.8F, ColorUtil.getColor(255, (int) (4 * animation.getValue())));


            Fonts.MONTSERRAT_BOLD.draw(
                    matrix,
                    title,
                    x + MARGIN + 4 + Fonts.ICON_DESHUX.getWidth("g", 6),
                    y + (headerH / 2F) - (7.0F / 2F) - 0.6F,
                    ColorUtil.multAlpha(ColorUtil.getColor(160), animValue()),
                    6.5F
            );

            float finalX = x;
            float finalY = y;


            Fonts.ICON_DESHUX.draw(
                    matrix,
                    "g",
                    x + MARGIN + 1,
                    y + (headerH / 2F) - (7.0F / 2F) + 0.55F,
                    ColorUtil.multAlpha(ColorUtil.fade(), animValue()),
                    6
            );


            //RectUtil.drawRect(matrix, x + MARGIN + 13, y + 4, 0.5F, 8, ColorUtil.getColor(255, 0.1F * animation.get()));


            // Находим максимальную ширину среди всех клавиш
            float targetMaxKeyWidth = 0f;
            for (Module binding : bindings) {
                String keyName = Keyboard.keyName(binding.getKey());
                float keyWidth = Fonts.MONTSERRAT_BOLD.getWidth(keyName, 7.0F);
                targetMaxKeyWidth = Math.max(targetMaxKeyWidth, keyWidth);
            }

            // Плавно интерполируем максимальную ширину клавиш
            if (smoothMaxKeyWidth < 0f) smoothMaxKeyWidth = targetMaxKeyWidth;
            smoothMaxKeyWidth = smoothTowards(smoothMaxKeyWidth, targetMaxKeyWidth, dt, SIZE_SMOOTH_SPEED);

            float offsetY = 0f;
            float ys = -2.4F;
            for (Module binding : bindings) {
                float animPC = binding.getAnimation().get();
                float xanim = 0;

                float baseY = y + ys + headerH + MARGIN + offsetY;

                int colorName = ColorUtil.multAlpha(ColorUtil.getColor(180), animPC);

                String keyName = Keyboard.keyName(binding.getKey());

                matrix.push();


                //InterFace.getInstance().drawClientRect(matrix, x + xanim, baseY + ys, width, 12, animation.get() * animPC, 4);


                // Позиция разделителя: от правого края отступаем на плавную максимальную ширину клавиш + отступы
                float separatorX = x + xanim + width - smoothMaxKeyWidth - 10;


                // Рисуем разделитель симметрично (по центру высоты текста)
                // RectUtil.drawRect(matrix, separatorX, baseY + ys + 3, 0.5F, 6, ColorUtil.getColor(255, 0.1F * animation.get() * animPC));


                // Имя модуля слева
                float finalX1 = x;

                Fonts.ICON_DESHUX.draw(
                        matrix,
                        binding.getCategory().getIcon(),
                        x + xanim + xanim + 5,
                        baseY + ys - MARGIN + 9.35F,
                        ColorUtil.multAlpha(ColorUtil.fade(), animPC),
                        6.5F
                );


                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        binding.getName(),
                        x + xanim + xanim + 12,
                        baseY + ys - MARGIN + 8.6F,
                        colorName,
                        6.5F
                );

                // Клавиша справа (выравнивание по центру относительно максимальной ширины)
                float currentKeyWidth = Fonts.MONTSERRAT_BOLD.getWidth(keyName, 6.5F);
                float keyX = x + xanim + width - 5;

                Fonts.MONTSERRAT_BOLD.drawRight(
                        matrix,
                        keyName,
                        keyX,
                        baseY + ys - MARGIN + 8.6F,
                        ColorUtil.multAlpha(ColorUtil.fade(), animPC), 6.5F
                );


                matrix.pop();

                offsetY += 9 * animPC;
            }


            float targetHeight = headerH + offsetY + (MARGIN * 2F) - 3;


            drag.size.y = targetHeight;

            matrix.pop();


       /* } else {

            MatrixStack matrix = event.getMatrix();

            final String title = "HotKeys";
            final boolean isEmpty = bindings.isEmpty();

            final float headerH = MARGIN + ROW_SIZE + MARGIN;


            float targetWidth = computeTargetWidth(title);


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
                    x + MARGIN + 8 + 8,
                    y + (headerH / 2F) - (7.0F / 2F) - 0.6F,
                    textWhite(),
                    7.7F
            );

            RectUtil.drawRect(matrix, x + MARGIN + 13, y + 4, 0.5F, 8, ColorUtil.getColor(255, 0.1F * animation.get()));

            Fonts.ICON_NURIK.draw(
                    matrix,
                    "C",
                    x + MARGIN,
                    y + MARGIN - 0.6F,
                    textWhite(),
                    9
            );


            // Находим максимальную ширину среди всех клавиш
            float targetMaxKeyWidth = 0f;
            for (Module binding : bindings) {
                String keyName = Keyboard.keyName(binding.getKey());
                float keyWidth = Fonts.MONTSERRAT_BOLD.getWidth(keyName, 6.7F);
                targetMaxKeyWidth = Math.max(targetMaxKeyWidth, keyWidth);
            }

            // Плавно интерполируем максимальную ширину клавиш
            if (smoothMaxKeyWidth < 0f) smoothMaxKeyWidth = targetMaxKeyWidth;
            smoothMaxKeyWidth = smoothTowards(smoothMaxKeyWidth, targetMaxKeyWidth, dt, SIZE_SMOOTH_SPEED);

            float offsetY = 0f;
            float ys = -2.5F;
            for (Module binding : bindings) {
                float animPC = binding.getAnimation().get();
                float xanim = 0;

                float baseY = y + ys + headerH + MARGIN + offsetY;

                int colorName = ColorUtil.multAlpha(textWhite(), animPC);

                String keyName = Keyboard.keyName(binding.getKey());

                matrix.push();


                InterFace.getInstance().drawClientRect(matrix, x + xanim, baseY + ys, width, 12, animation.get() * animPC, 4);

                // Позиция разделителя: от правого края отступаем на плавную максимальную ширину клавиш + отступы
                float separatorX = x + xanim + width - smoothMaxKeyWidth - 10;

                // Рисуем разделитель симметрично (по центру высоты текста)
                RectUtil.drawRect(matrix, separatorX, baseY + ys + 3, 0.5F, 6, ColorUtil.getColor(255, 0.1F * animation.get() * animPC));

                // Имя модуля слева
                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        binding.getName(),
                        x + xanim + xanim + 4,
                        baseY + ys - MARGIN + 7.8F,
                        colorName,
                        6.7F
                );

                // Клавиша справа (выравнивание по центру относительно максимальной ширины)
                float currentKeyWidth = Fonts.MONTSERRAT_BOLD.getWidth(keyName, 6.7F);
                float keyX = x + xanim + width - 4 - smoothMaxKeyWidth + (smoothMaxKeyWidth - currentKeyWidth) / 2f;

                Fonts.MONTSERRAT_BOLD.draw(
                        matrix,
                        keyName,
                        keyX,
                        baseY + ys - MARGIN + 7.8F,
                        colorName,
                        6.7F
                );


                matrix.pop();

                offsetY += (12.5F) * animPC;
            }


            float targetHeight = headerH + offsetY + (MARGIN * 2F) - 5.5F;

            smoothHeight = smoothTowards(smoothHeight, targetHeight, dt, SIZE_SMOOTH_SPEED);
            drag.size.y = (float) Mathf.step(smoothHeight, 0.5);

            matrix.pop();
        } */

    }

    /**
     * Считает требуемую ширину панели под заголовок и все строки.
     */
    private float computeTargetWidth(String title) {
        float maxW = 0f;

        // ширина заголовка + правой иконки
        float titleW = Fonts.MONTSERRAT_MEDIUM.getWidth(title, TITLE_SIZE);
        float titleIconW = Fonts.ICON_DIMAS.getWidth("B", ICON_TITLE_SIZE);
        float headerWidth = MARGIN + 1f + titleW + MARGIN /*зазор*/ + titleIconW + MARGIN;
        maxW = Math.max(maxW, headerWidth);

        // строки: [margin] [icon] [gap] [name] [gap] [key (right)] [margin]
        float iconRowW = Fonts.ICON_DIMAS.getWidth("A", ICON_ROW_SIZE); // любая буква для метрики
        for (Module m : bindings) {
            String key = Keyboard.keyName(m.getKey());
            float nameW = Fonts.MONTSERRAT_MEDIUM.getWidth(m.getName(), ROW_SIZE);
            float keyW = Fonts.MONTSERRAT_MEDIUM.getWidth(key, ROW_SIZE);

            float rowWidth = MARGIN
                    + iconRowW
                    + NAME_LEFT_GAP
                    + nameW
                    + BETWEEN_TEXT_GAP
                    + keyW
                    + MARGIN;

            maxW = Math.max(maxW, rowWidth);
        }

        // минималка


        maxW = Math.max(maxW, 75);


        // снап к 0.5px для crisp
        return (float) Mathf.step(maxW, 0.5);
    }

    private void sortModules() {
        bindings.clear();
        bindings.addAll(Zetrix.inst().moduleManager().values().stream()
                .filter(this::shouldDisplay)
                .collect(Collectors.toList()));
    }

    private boolean shouldDisplay(Module module) {
        return module.isAllowDisable()
                && (module.isEnabled() || module.getAnimation().getValue() != 0.0)
                && module.getKey() != Keyboard.KEY_NONE.getKey();
    }

    // === helpers ===

    // экспоненциальное сглаживание к цели с учётом dt (FPS-independent)
    private float smoothTowards(float current, float target, float dt, float speedPerSec) {
        if (!Float.isFinite(dt) || dt <= 0f) return target; // на всякий
        float k = 1f - (float) Math.exp(-speedPerSec * dt); // 0..1
        return current + (target - current) * k;
    }

    private float computeDtSeconds() {
        long now = System.nanoTime();
        if (lastTimeNs == 0L) {
            lastTimeNs = now;
            return 1f / 60f; // первый кадр — условные ~16.7мс
        }
        long d = now - lastTimeNs;
        lastTimeNs = now;
        // clamp dt, чтобы при фризах не прыгало
        double dt = Math.min(Math.max(d / 1_000_000_000.0, 0.0), 0.1); // [0 .. 100мс]
        return (float) dt;
    }
}
