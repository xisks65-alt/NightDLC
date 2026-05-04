package dev.wh1tew1ndows.client.managers.component.impl.drag;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IMouse;
import dev.wh1tew1ndows.client.api.interfaces.IWindow;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.input.MousePressEvent;
import dev.wh1tew1ndows.client.managers.events.input.MouseReleaseEvent;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.ScaleMath;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Getter
public class DragComponent extends Component implements IWindow, IMouse {
    private DragSetting selected;
    private final Vector2f offset = new Vector2f();
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    public static List<Line> lines = new CopyOnWriteArrayList<>();
    private final Script script = new Script();

    // прогресс анимации hover для каждого DragSetting
    private final Map<DragSetting, Float> hoverProgress = new WeakHashMap<>();

    public void post(MatrixStack matrix, float partialTicks) {
        script.update();

        final int width = mw.getScaledWidth();
        final int height = mw.getScaledHeight();

        int color = ColorUtil.getColor(255, 64); // базовый цвет для линий привязки

        boolean shouldRender = mc.currentScreen instanceof ChatScreen;

        if (!shouldRender) {
            selected = null;
        }

        initModules();
        handleAnimation();

        // РАМКА ПРИ НАВЕДЕНИИ + ПУЛЬСИРУЮЩАЯ ЗАЛИВКА (с градиентом)
        //  renderHoverOutline(matrix, partialTicks);

        if (selected != null) {
            if (!selected.active) return;
            final float mouseX = (float) (mc.mouseHelper.getMouseX() / mw.getScaleFactor());
            final float mouseY = (float) (mc.mouseHelper.getMouseY() / mw.getScaleFactor());
            final float positionX = mouseX + offset.x;
            final float positionY = mouseY + offset.y;

            selected.targetPosition.set(positionX, positionY);

            lines.clear();

            initSnaps(width, height);
            handleSnaps(matrix, color);
            handleDrags(width, height);
        }
    }

    private void handleDrags(int width, int height) {
        for (Module module : modules) {
            List<Setting<?>> positionSettings = module.getSettings().stream().filter(setting -> setting instanceof DragSetting).toList();
            for (Setting<?> setting : positionSettings) {
                if (setting instanceof DragSetting dragSetting) {
                    if (!dragSetting.active) continue;

                    dragSetting.position.x = (float) dragSetting.animationX.getValue();
                    dragSetting.position.y = (float) dragSetting.animationY.getValue();

                    dragSetting.position.x = Math.max(0, dragSetting.position.x);
                    dragSetting.position.x = Math.min(width - dragSetting.size.x, dragSetting.position.x);

                    dragSetting.position.y = Math.max(0, dragSetting.position.y);
                    dragSetting.position.y = Math.min(height - dragSetting.size.y, dragSetting.position.y);

                    dragSetting.targetPosition.x = Math.max(0, dragSetting.targetPosition.x);
                    dragSetting.targetPosition.x = Math.min(width - dragSetting.size.x, dragSetting.targetPosition.x);

                    dragSetting.targetPosition.y = Math.max(0, dragSetting.targetPosition.y);
                    dragSetting.targetPosition.y = Math.min(height - dragSetting.size.y, dragSetting.targetPosition.y);
                }
            }
        }
    }

    private void handleSnaps(MatrixStack matrix, int color) {
        double closest;

        for (Line snap : lines) {
            switch (snap.direction) {
                case VERTICAL:
                    closest = Double.MAX_VALUE;
                    for (float y = -selected.size.y; y <= 0; y += selected.size.y / 2F) {
                        if ((y == -selected.size.y / 2F && !snap.center) || (y == -selected.size.y && !snap.left) || (y == 0 && !snap.right)) {
                            continue;
                        }
                        double distance = Math.abs(selected.targetPosition.y - (snap.position + y));
                        if (distance < snap.distance && distance < closest) {
                            closest = distance;
                            selected.targetPosition.y = snap.position + y;
                            RectUtil.drawRect(matrix, 0, snap.position - 0.5F, scaled().x, 1F, color);
                        }
                    }
                    break;

                case HORIZONTAL:
                    closest = Double.MAX_VALUE;
                    for (float x = -selected.size.x; x <= 0; x += selected.size.x / 2F) {
                        if ((x == -selected.size.x / 2F && !snap.center) || (x == -selected.size.x && !snap.left) || (x == 0 && !snap.right)) {
                            continue;
                        }
                        float distance = Math.abs(selected.targetPosition.x - (snap.position + x));
                        if (distance < snap.distance && distance < closest) {
                            closest = distance;
                            selected.targetPosition.x = snap.position + x;
                            RectUtil.drawRect(matrix, snap.position - 0.5F, 0, 1F, scaled().y, color);
                        }
                    }
                    break;
            }
        }
    }

    private void initSnaps(int width, int height) {
        float edgeSnap = 5F;
        float distance = 5F;
        lines.add(new Line(width / 4f, distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height / 4f, distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(width - (width / 4f), distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height - (height / 4f), distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(width / 2f, distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height / 2f, distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(height - edgeSnap, distance, Direction.VERTICAL, false, false, true));
        lines.add(new Line(edgeSnap, distance, Direction.VERTICAL, false, true, false));

        lines.add(new Line(width - edgeSnap, distance, Direction.HORIZONTAL, false, false, true));
        lines.add(new Line(edgeSnap, distance, Direction.HORIZONTAL, false, true, false));

        for (Module module : modules) {
            Stream<Setting<?>> positionSettings = module.getSettings()
                    .stream()
                    .filter(setting -> setting instanceof DragSetting);
            positionSettings.forEach(positionSetting -> {
                if (positionSetting instanceof DragSetting dragSetting && dragSetting != selected) {
                    lines.add(new Line(dragSetting.position.x + dragSetting.size.x + edgeSnap, distance, Direction.HORIZONTAL, false, true, false));
                    lines.add(new Line(dragSetting.position.x - edgeSnap, distance, Direction.HORIZONTAL, false, false, true));

                    lines.add(new Line(dragSetting.position.y - edgeSnap, distance, Direction.VERTICAL, false, false, true));
                    lines.add(new Line(dragSetting.position.y + dragSetting.size.y + edgeSnap, distance, Direction.VERTICAL, false, true, false));
                }
            });
        }
    }

    private void handleAnimation() {
        modules.forEach(module -> module.getSettings().forEach(setting -> {
            if (setting instanceof DragSetting dragSetting) {
                dragSetting.animationX.update();
                dragSetting.animationY.update();
                if (script.isFinished()) {
                    dragSetting.animationX.run(dragSetting.targetPosition.x, 0.25, Easings.CUBIC_OUT, true);
                    dragSetting.animationY.run(dragSetting.targetPosition.y, 0.25, Easings.CUBIC_OUT, true);
                }
            }
        }));
    }

    private void initModules() {
        modules.clear();
        Zetrix.inst().moduleManager().values().stream()
                .filter(module -> module.isEnabled() && module.getSettings().stream()
                        .anyMatch(setting -> setting instanceof DragSetting))
                .forEach(modules::add);
    }

    @EventHandler
    public void onEvent(MousePressEvent event) {
        if (event.getKey() != 0) {
            return;
        }
        if (event.getScreen() instanceof ChatScreen) {
            for (final Module module : modules) {
                for (final Setting<?> setting : module.getSettings()) {
                    if (setting instanceof final DragSetting dragSetting) {
                        if (!dragSetting.active) continue;
                        final Vector2f position = dragSetting.position;
                        final Vector2f scale = dragSetting.size;
                        final Vector2f mouse = ScaleMath.getMouse(event.getMouseX(), event.getMouseY());
                        final double mouseX = mouse.x;
                        final double mouseY = mouse.y;
                        if (!dragSetting.active) return;
                        if (!dragSetting.structure && isHover(mouseX, mouseY, position.x, position.y, scale.x, scale.y)) {
                            selected = dragSetting;
                            offset.set(position.x - mouseX, position.y - mouseY);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEvent(MouseReleaseEvent event) {
        if (selected != null) {
            script.cleanup()
                    .addTickStep(0, () -> {
                        selected.targetPosition.set(selected.position);
                        selected = null;
                    }, () -> selected != null && selected.animationX.isFinished() && selected.animationY.isFinished());
        }
    }

    /**
     * Плавная обводка + белая пульсирующая заливка с вертикальным градиентом.
     * Пик раз в 5 секунд. Альфа занижена.
     */
    private void renderHoverOutline(MatrixStack matrix, float partialTicks) {
        if (!(mc.currentScreen instanceof ChatScreen)) return;

        final float mouseX = (float) (mc.mouseHelper.getMouseX() / mw.getScaleFactor());
        final float mouseY = (float) (mc.mouseHelper.getMouseY() / mw.getScaleFactor());

        // параметры обводки
        final int baseColorRGB = ColorUtil.getColor(225, 255);
        final float baseThickness = 1.0F;
        final float baseInflate = 3.0F;
        final float baseRadius = 4.0F;

        // пульс и прозрачность заливки
        final long periodMs = 5000L;  // раз в 5 сек
        final int maxFillAlpha = 72;     // было больше — сделал ниже

        for (Module module : modules) {
            for (Setting<?> setting : module.getSettings()) {
                if (!(setting instanceof DragSetting dragSetting)) continue;
                if (!dragSetting.active) continue;

                Vector2f pos = dragSetting.position;
                Vector2f size = dragSetting.size;

                boolean hovered = isHover(mouseX, mouseY, pos.x, pos.y, size.x, size.y);

                // прогресс 0..1
                float cur = hoverProgress.getOrDefault(dragSetting, 0f);
                float tgt = hovered ? 1f : 0f;

                float speed = hovered ? 0.18f : 0.10f;
                cur = lerpTo(cur, tgt, speed);

                if (cur <= 0.001f && !hovered) {
                    hoverProgress.remove(dragSetting);
                    continue;
                } else {
                    hoverProgress.put(dragSetting, cur);
                }

                float t = easeOutCubic(cur);

                // ===== ОБВОДКА =====
                int outlineAlpha = clamp255((int) (220f * t)); // тоже чуть мягче
                int outlineColor = (outlineAlpha << 24) | (baseColorRGB & 0x00FFFFFF);

                float inflate = baseInflate * t;
                float radius = baseRadius + 2.0f * t;
                float thickness = baseThickness;

                RenderUtil.Rounded.roundedOutline(
                        matrix,
                        pos.x - inflate / 2f - thickness, pos.y - inflate / 2f - thickness,
                        size.x + inflate + thickness * 2f, size.y + inflate + thickness * 2f,
                        thickness,
                        outlineColor,
                        Round.of(radius)
                );

                // ===== ПУЛЬСИРУЮЩАЯ ЗАЛИВКА С ВЕРТИКАЛЬНЫМ ГРАДИЕНТОМ =====
                // Синусный пульс 0→1→0 и уменьшенная альфа
                float pulse01 = pulsing01(periodMs);
                int peakAlpha = clamp255((int) (maxFillAlpha * pulse01 * t));
                if (peakAlpha > 0) {
                    // рисуем мягкий вертикальный градиент (ярче в центре, к краям прозрачнее)
                    // drawVerticalSoftGradient(matrix, pos.x, pos.y, size.x, size.y, peakAlpha);
                }
            }
        }
    }

    /**
     * Вертикальный софт-градиент (белый): центр ярче, к краям прозрачнее.
     */
    private void drawVerticalSoftGradient(MatrixStack m, float x, float y, float w, float h, int peakAlpha) {
        final int slices = Math.max(8, (int) (h / 4f)); // адаптивно, но не меньше 8
        final float sliceH = h / slices;

        for (int i = 0; i < slices; i++) {
            float cyNorm = (i + 0.5f) / slices;        // [0..1]
            float weight = bell01(cyNorm);             // 0..1..0 (макс в центре)
            int a = clamp255((int) (peakAlpha * weight));

            if (a <= 0) continue;

            int c = (a << 24) | 0x00FFFFFF; // белый с текущей альфой
            RectUtil.drawRect(m, x, y + i * sliceH, w, sliceH + 0.5f, c);
        }
    }

    /**
     * колокол (bell) 0..1..0 на [0..1] через cos: мягкий спад к краям
     */
    private static float bell01(float t) {
        t = Math.max(0f, Math.min(1f, t));
        // 0.5*(1 - cos(2πt)) дает 0..1..0, но пик слишком плоский.
        // Подожмем степенью, чтобы центр был выразительнее:
        float v = 0.5f * (1f - (float) Math.cos(2f * Math.PI * t));
        return (float) Math.pow(v, 0.8f);
    }

    private static int clamp255(int v) {
        return (v < 0) ? 0 : (Math.min(v, 255));
    }

    // синусный пульс 0..1..0 за период
    private static float pulsing01(long periodMs) {
        long now = System.currentTimeMillis();
        float phase = (now % periodMs) / (float) periodMs; // [0..1)
        return 0.5f * (1f - (float) Math.cos(2f * Math.PI * phase));
    }

    private static float lerpTo(float current, float target, float speed) {
        float next = current + (target - current) * speed;
        if (Math.abs(target - next) < 0.001f) next = target;
        return next;
    }

    private static float easeOutCubic(float x) {
        x = Math.max(0f, Math.min(1f, x));
        return 1f - (float) Math.pow(1f - x, 3f);
    }

    @AllArgsConstructor
    public static class Line {
        public float position, distance;
        public DragComponent.Direction direction;
        public boolean center, right, left;
    }

    public enum Direction {
        VERTICAL,
        HORIZONTAL
    }
}
