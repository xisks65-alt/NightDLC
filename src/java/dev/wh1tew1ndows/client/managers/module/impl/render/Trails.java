package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Trails", category = Category.RENDER, desc = "Следы за игроками  ")
public class Trails extends Module implements IRender {
    public static Trails getInstance() {
        return Instance.get(Trails.class);
    }

    private final ModeSetting trails = new ModeSetting(this, "Мод", "Простой");

    private final SliderSetting length = new SliderSetting(this, "Длинна", 250F, 150F, 350F, 5F).setVisible(() -> trails.is("Простой"));


    private final List<Point> points = new ArrayList<>();

    private void clear() {
        points.clear();
    }

    @Override
    public void toggle() {
        super.toggle();
        clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        clear();
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        clear();
    }

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        if (trails.is("Простой")) {
            final ClientPlayerEntity player = mc.player;

            final MatrixStack matrix = event.getMatrix();
            final boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
            RenderSystem.pushMatrix();
            matrix.push();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (light)
                RenderSystem.disableLighting();
            GL11.glShadeModel(GL11.GL_SMOOTH);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            matrix.push();

            if (!mc.gameSettings.getPointOfView().firstPerson()) {
                points.removeIf(point -> point.time.finished(length.getValue()));
                Vector3d playerPos = RenderUtil3D.interpolate(player, event.getPartialTicks());
                points.add(new Point(playerPos));
                drawTrail(matrix);
            }

            matrix.pop();

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.clearCurrentColor();
            GL11.glShadeModel(GL11.GL_FLAT);
            if (light)
                RenderSystem.enableLighting();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableAlphaTest();
            matrix.pop();
            RenderSystem.popMatrix();
        }
    }

    private void drawTrail(MatrixStack matrix) {
        startRendering();
        BUFFER.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        final Vector3d projectedView = mc.getRenderManager().info.getProjectedView();
        int index = 0;
        for (Point point : points) {
            final int color = ColorUtil.getColorGR(index);
            final float alpha = Math.min((float) index / (float) points.size(), 1F);
            final int finalColor = ColorUtil.replAlpha(color, alpha / 2F);
            final Vector3d pos = point.pos.subtract(projectedView);
            BUFFER.pos(matrix.getLast().getMatrix(), (float) pos.x, (float) (pos.y + mc.player.getHeight()), (float) pos.z).color(finalColor).endVertex();
            BUFFER.pos(matrix.getLast().getMatrix(), (float) pos.x, (float) pos.y + 0.0005F, (float) pos.z).color(finalColor).endVertex();
            index++;
        }
        TESSELLATOR.draw();
        RenderSystem.lineWidth(2);
        drawLine(matrix, points, true);
        drawLine(matrix, points, false);
        stopRendering();
    }

    private void drawLine(MatrixStack matrix, List<Point> points, boolean withHeight) {
        BUFFER.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        final Vector3d projectedView = mc.getRenderManager().info.getProjectedView();
        int index = 0;
        for (Point point : points) {
            final int color = ColorUtil.fade(index * 2);
            final float alpha = Math.min((float) index / (float) points.size(), 1F);
            final int finalColor = ColorUtil.replAlpha(color, alpha);
            final Vector3d pos = point.pos.subtract(projectedView);
            if (withHeight)
                BUFFER.pos(matrix.getLast().getMatrix(), (float) pos.x, (float) (pos.y + mc.player.getHeight()), (float) pos.z).color(finalColor).endVertex();
            else
                BUFFER.pos(matrix.getLast().getMatrix(), (float) pos.x, (float) pos.y + 0.0005F, (float) pos.z).color(finalColor).endVertex();
            index++;
        }
        TESSELLATOR.draw();
    }

    private static class Point {
        private final Vector3d pos;
        private final StopWatch time = new StopWatch();

        public Point(Vector3d pos) {
            this.pos = pos;
        }
    }

    private void startRendering() {
        RenderSystem.pushMatrix();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.color4f(0, 0, 0, 0.1f);

    }

    private void stopRendering() {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}