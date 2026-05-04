package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.animation.AnimationMath;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderFactory;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import dev.wh1tew1ndows.client.utils.render.shader.impl.BloomShader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.opengl.GL11;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Пастер", category = Category.RENDER, desc = "Сандер пидр")
public class SanderModule extends Module {

    // Текущая интерполированная позиция для плавного перемещения
    double currentX = 0;
    double currentY = 0;
    double currentZ = 0;

    public final BooleanSetting glow = new BooleanSetting(this, "Глов", true);
    public final SliderSetting glowSize = new SliderSetting(this, "Сила глова", 4, 1, 5, 1).setVisible(() -> glow.getValue());
    public final SliderSetting saturationGlow = new SliderSetting(this, "Яркость глова", 1.0F, 0.0F, 4, 0.05F).setVisible(() -> glow.getValue());

    public final BloomShader BLOOM_SHADER = new BloomShader();

    @EventHandler
    public void ебаныйРендерТУПОГОСАНДЕРА(Render3DPosedEvent e) {
        if (mc.player == null) return;

        MatrixStack matrix = e.getMatrix();

        // Интерполированная позиция игрока (как в Esp.java)
        float partialTicks = mc.getRenderPartialTicks();
        double interpolatedX = MathHelper.lerp(partialTicks, mc.player.lastTickPosX, mc.player.getPosX());
        double interpolatedY = MathHelper.lerp(partialTicks, mc.player.lastTickPosY, mc.player.getPosY());
        double interpolatedZ = MathHelper.lerp(partialTicks, mc.player.lastTickPosZ, mc.player.getPosZ());

        // Целевая позиция (возле игрока)
        double targetX = interpolatedX + 3;
        double targetY = interpolatedY + mc.player.getEyeHeight() + 0.5;
        double targetZ = interpolatedZ + 3;

        // Плавная интерполяция позиции
        float speed = 90; // Скорость интерполяции (чем больше, тем быстрее)
        currentX = AnimationMath.lerp(targetX, currentX, speed);
        currentY = AnimationMath.lerp(targetY, currentY, speed);
        currentZ = AnimationMath.lerp(targetZ, currentZ, speed);

        float size = 3;
        int color = -1;

        matrix.push();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // Переводим координаты из мировых в координаты относительно камеры
        RenderUtil3D.setupOrientationMatrix(matrix, currentX, currentY, currentZ);

        // Поворачиваем к камере (биллборд)
        RenderUtil3D.rotateToCamera(matrix);

        // Переворачиваем картинку на 180 градусов
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));


        // Привязываем текстуру
        RenderUtil.bindTexture(new Namespaced("texture/images.png"));

        // Рисуем через RectUtil
        RectUtil.drawRect(matrix, -size / 2.0, -size / 2.0, size, size, color, false, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        matrix.pop();
    }

    private void renderSander(MatrixStack matrix) {
        if (mc.player == null) return;

        float size = 3;
        int color = -1;

        matrix.push();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // Переводим координаты из мировых в координаты относительно камеры
        RenderUtil3D.setupOrientationMatrix(matrix, currentX, currentY, currentZ);

        // Поворачиваем к камере (биллборд)
        RenderUtil3D.rotateToCamera(matrix);

        // Переворачиваем картинку на 180 градусов
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));

        // Привязываем текстуру
        RenderUtil.bindTexture(new Namespaced("texture/images.png"));

        // Рисуем через RectUtil
        RectUtil.drawRect(matrix, -size / 2.0, -size / 2.0, size, size, color, false, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        matrix.pop();
    }

    @EventHandler
    public void onRender3D(Render3DEvent.PostWorld event) {
        if (!glow.getValue()) return;
        if (mc.player == null) return;

        // Обновляем позицию для bloom рендеринга
        float partialTicks = event.getPartialTicks();
        double interpolatedX = MathHelper.lerp(partialTicks, mc.player.lastTickPosX, mc.player.getPosX());
        double interpolatedY = MathHelper.lerp(partialTicks, mc.player.lastTickPosY, mc.player.getPosY());
        double interpolatedZ = MathHelper.lerp(partialTicks, mc.player.lastTickPosZ, mc.player.getPosZ());

        double targetX = interpolatedX + 3;
        double targetY = interpolatedY + mc.player.getEyeHeight() + 0.5;
        double targetZ = interpolatedZ + 3;

        float speed = 90;
        currentX = AnimationMath.lerp(targetX, currentX, speed);
        currentY = AnimationMath.lerp(targetY, currentY, speed);
        currentZ = AnimationMath.lerp(targetZ, currentZ, speed);

        MatrixStack matrix = event.getMatrix();
        BLOOM_SHADER.addTask3D(() -> renderSander(matrix));
        BLOOM_SHADER.draw(glowSize.getValue().intValue(), glowSize.getValue(), BloomShader.RenderType.CAMERA, saturationGlow.getValue());
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!glow.getValue()) return;
        RenderFactory.addTask(() ->
                BLOOM_SHADER.draw(glowSize.getValue().intValue(), glowSize.getValue(), BloomShader.RenderType.DISPLAY, saturationGlow.getValue()));
    }

}
