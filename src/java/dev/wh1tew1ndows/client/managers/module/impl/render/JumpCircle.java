package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.managers.events.player.JumpEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.tenacity.Animation;
import dev.wh1tew1ndows.client.utils.tenacity.Direction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "JumpCircle", category = Category.RENDER, desc = "Круг при прыжке")
public class JumpCircle extends Module implements IRender {
    public static JumpCircle getInstance() {
        return Instance.get(JumpCircle.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Тип картинки", "1", "2");
    private final SliderSetting lifetime = new SliderSetting(this, "Скорость", 400, 200, 1000, 10);
    private final SliderSetting radius = new SliderSetting(this, "Радиус", 0.5F, 0.1F, 2, 0.1F);

    public List<Circle> circles = new ArrayList<>();

    @EventHandler
    public void onJump(JumpEvent event) {
        if (mc.player == null) return;
        circles.add(new Circle(mc.player.getPositionVec()));
    }

    @EventHandler
    public void worldRender(Render3DPosedEvent worldRenderEvent) {
        for (Circle circle : circles) {
            circle.render(worldRenderEvent.getMatrix());
        }
        circles.removeIf(circle -> circle.animation.isDone(Direction.BACKWARD));
    }

    @Getter
    @RequiredArgsConstructor
    public static class Circle {
        private final Animation animation = new Animation(255,
                Duration.ofMillis(JumpCircle.getInstance().lifetime.getValue().longValue()));
        private final Vector3d pos;

        public void render(MatrixStack stack) {
            double x = pos.x - mc.getRenderManager().renderPosX(),
                    y = pos.y - mc.getRenderManager().renderPosY() + 0.15f,
                    z = pos.z - mc.getRenderManager().renderPosZ();

            int base = InterFace.getInstance().themeColor();
            int color = ColorUtil.applyOpacity(base, animation.get());

            // Плавный scale при спавне/исчезновении
            float progress = animation.get() * 0.001F; // 0 → 1 → 0
            float half = JumpCircle.getInstance().radius.getValue();

            stack.push();
            stack.translate(x, y, z);
            stack.rotate(new Quaternion(new Vector3f(1, 0, 0), 90, true));

            long cicleTime = 2000L;
            float degRotate = (System.currentTimeMillis() % cicleTime) / (float) cicleTime * 360.F;
            //  stack.rotate(Vector3f.ZP.rotationDegrees(degRotate));

            Namespaced tex = new Namespaced("texture/jump.png");
            if (getInstance().mode.is("2")) {
                tex = new Namespaced("texture/circle.png");
            }

            mc.getTextureManager().bindTexture(tex);
            mc.getTextureManager().getTexture(tex).setBlurMipmap(true, true);

            Matrix4f m = stack.getLast().getMatrix();
            BufferBuilder bb = Tessellator.getInstance().getBuffer();

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );

            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            bb.pos(m, -half, -half, 0).tex(0f, 0f).color(ColorUtil.applyOpacity(ColorUtil.fade(0, 0F), animation.get())).endVertex();
            bb.pos(m, half, -half, 0).tex(1f, 0f).color(ColorUtil.applyOpacity(ColorUtil.fade(90, 0F), animation.get())).endVertex();
            bb.pos(m, half, half, 0).tex(1f, 1f).color(ColorUtil.applyOpacity(ColorUtil.fade(180, 0F), animation.get())).endVertex();
            bb.pos(m, -half, half, 0).tex(0f, 1f).color(ColorUtil.applyOpacity(ColorUtil.fade(270, 0F), animation.get())).endVertex();
            bb.pos(m, -half, -half, 0).tex(0f, 0f).color(ColorUtil.applyOpacity(ColorUtil.fade(0, 0F), animation.get())).endVertex();
            bb.pos(m, half, -half, 0).tex(1f, 0f).color(ColorUtil.applyOpacity(ColorUtil.fade(90, 0F), animation.get())).endVertex();
            bb.pos(m, half, half, 0).tex(1f, 1f).color(ColorUtil.applyOpacity(ColorUtil.fade(180, 0F), animation.get())).endVertex();
            bb.pos(m, -half, half, 0).tex(0f, 1f).color(ColorUtil.applyOpacity(ColorUtil.fade(270, 0F), animation.get())).endVertex();
            Tessellator.getInstance().draw();

            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.popMatrix();

            stack.rotate(Vector3f.ZN.rotationDegrees(degRotate));
            stack.pop();

            // переключение на исчезновение
            if (animation.isDone(Direction.FORWARD)) {
                animation.switchDirection(Direction.BACKWARD);
            }
        }
    }
}
