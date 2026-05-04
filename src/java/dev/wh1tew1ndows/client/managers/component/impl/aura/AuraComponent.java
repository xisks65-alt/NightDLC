package dev.wh1tew1ndows.client.managers.component.impl.aura;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.ModuleManager;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.animation.Direction;
import dev.wh1tew1ndows.client.utils.animation.animation.impl.EaseInOutQuad;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.*;
import dev.wh1tew1ndows.client.utils.rotation.AuraUtil;
import dev.wh1tew1ndows.client.utils.time.StopWatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.ArrayList;

import static dev.wh1tew1ndows.client.api.interfaces.IRender.BUFFER;
import static dev.wh1tew1ndows.client.api.interfaces.IRender.TESSELLATOR;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP;
import static net.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.mojang.blaze3d.systems.RenderSystem.disableBlend;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_GREATER;
import static org.lwjgl.opengl.GL11C.GL_ONE;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;


public class AuraComponent extends Component {

    public LivingEntity target;
    public Animation alpha = new Animation();
    public Animation sizeAnim = new Animation();
    private final Animation rhombHit = new Animation();
    private final long startTime = System.currentTimeMillis();
    private final boolean animationsInitialized = false;
    private final double smoothDirX = 0;
    private final double smoothDirY = 0;
    private final double smoothDirZ = 0;
    private final float smoothMotionSpeed = 0;
    private final float sizeProgress = 0f;
    private final long lastTime = System.currentTimeMillis();


    @EventHandler
    public void onRender(Render3DPosedEvent e) {
        AttackAura aura = AttackAura.getInstance();
        alpha.update();
        sizeAnim.update();
        if (aura.target != null) {
            target = aura.target;
        }
        alpha.run(aura.target == null ? 0 : 1, 0.1F);
        if (aura.targetType.is("Ромб")) {
            alpha.run(aura.target == null ? 0 : 1, 0.2F);
        }

        sizeAnim.run(aura.target == null ? 0 : 1, 0.3F);
        float alphaPC = alpha.get();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack matrix = e.getMatrix();
        rhombHit.update();
        boolean atts = target != null && target.hurtTime > 0;
        rhombHit.run(atts ? (double) 0.0F : (double) 1.0F, 0.2F, true);

        int hurtTicks = target != null ? target.hurtTime : 0;
        float hurtPC = (float) Math.sin((double) hurtTicks * (Math.PI / 10D));
        int redColor = ColorUtil.getColor(200, 70, 70, (int) (255.0F * alpha.get()));
        int colorSolo = ColorUtil.overCol(ColorUtil.multAlpha(InterFace.getInstance().clientColor(), alphaPC), redColor, hurtPC);
        int color1 = ColorUtil.overCol(ColorUtil.reAlphaInt(ColorUtil.fade(0, 0.7F), (int) (alphaPC * 255)), redColor, hurtPC);
        int color2 = ColorUtil.overCol(ColorUtil.reAlphaInt(ColorUtil.fade(90, 0.7F), (int) (alphaPC * 255)), redColor, hurtPC);
        int color3 = ColorUtil.overCol(ColorUtil.reAlphaInt(ColorUtil.fade(180, 0.7F), (int) (alphaPC * 255)), redColor, hurtPC);
        int color4 = ColorUtil.overCol(ColorUtil.reAlphaInt(ColorUtil.fade(270, 0.7F), (int) (alphaPC * 255)), redColor, hurtPC);

        double sin;
        if (aura.targetType.is("Ромб")) {

            if (target == null) return;

            sin = Math.sin((double) System.currentTimeMillis() / 1250);
            double targetX = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * mc.timer.renderPartialTicks;
            double targetY = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * mc.timer.renderPartialTicks + 0.45F;
            double targetZ = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * mc.timer.renderPartialTicks;

            double camX = mc.getRenderManager().info.getProjectedView().x;
            double camY = mc.getRenderManager().info.getProjectedView().y;
            double camZ = mc.getRenderManager().info.getProjectedView().z;

            ActiveRenderInfo cameraInfo = mc.gameRenderer.getActiveRenderInfo();
            Quaternion cameraRotation = cameraInfo.getRotation().copy();

            matrix.push();
            matrix.translate(-camX, -camY, -camZ);

            matrix.translate((float) targetX, (float) (targetY + target.getEyeHeight() / 2.0F - 0.2F), (float) targetZ);

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 0, 1);

            mc.getTextureManager().bindTexture(new Namespaced("texture/target.png"));



            float a = sizeAnim.get(); // 0-1
            float scale = MathHelper.clamp((float) (Math.sin(a * Math.PI) * 0.5F + 0.5F), 0.2F, 1.2F);

            long currentTimeMillis = System.currentTimeMillis();


            int color1S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor, hurtPC);
            int color2S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor, hurtPC);
            int color3S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor, hurtPC);
            int color4S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(270), alphaPC), redColor, hurtPC);

            float rotate = (float) Mathf.clamp(0, 360, ((Math.sin(currentTimeMillis / 750D) + 1F) / 2F) * 360);

            cameraRotation.multiply(Vector3f.ZP.rotationDegrees(rotate));

            matrix.rotate(cameraRotation);
            float dynSize =2 - (1F * a)  + 0.3F * rhombHit.get();


            float brightness = 1;
            float alha = 0.4F;
            RectUtil.drawRect(matrix, -dynSize / 2f, -dynSize / 2,
                    dynSize, dynSize,
                    color1S, color2S, color3S, color4S, true, true);
            RectUtil.drawRect(matrix, -dynSize / 2f, -dynSize / 2,
                    dynSize, dynSize,
                    ColorUtil.multAlpha(ColorUtil.multBright(color1S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color2S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color3S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color4S, brightness), alha * alphaPC), true, true);


            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.popMatrix();
            matrix.pop();
        }


        if (aura.targetType.is("Маркер")) {

            if (target == null) return;

            sin = Math.sin((double) System.currentTimeMillis() / 1250);
            double targetX = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * mc.timer.renderPartialTicks;
            double targetY = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * mc.timer.renderPartialTicks + 0.45F;
            double targetZ = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * mc.timer.renderPartialTicks;

            double camX = mc.getRenderManager().info.getProjectedView().x;
            double camY = mc.getRenderManager().info.getProjectedView().y;
            double camZ = mc.getRenderManager().info.getProjectedView().z;

            ActiveRenderInfo cameraInfo = mc.gameRenderer.getActiveRenderInfo();
            Quaternion cameraRotation = cameraInfo.getRotation().copy();

            matrix.push();
            matrix.translate(-camX, -camY, -camZ);

            matrix.translate((float) targetX, (float) (targetY + target.getEyeHeight() / 2.0F - 0.2F), (float) targetZ);

            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 0, 1);

            mc.getTextureManager().bindTexture(new Namespaced("texture/marker.png"));




            float a = sizeAnim.get(); // 0-1
            float scale = MathHelper.clamp((float) (Math.sin(a * Math.PI) * 0.5F + 0.5F), 0.2F, 1.2F);



            long currentTimeMillis = System.currentTimeMillis();


            int color1S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), redColor, hurtPC);
            int color2S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), redColor, hurtPC);
            int color3S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), redColor, hurtPC);
            int color4S = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(270), alphaPC), redColor, hurtPC);

            float rotate = (float) Mathf.clamp(0, 360, ((Math.sin(currentTimeMillis / 1000D) + 1F) / 2F) * 360);

            cameraRotation.multiply(Vector3f.ZP.rotationDegrees(rotate));

            matrix.rotate(cameraRotation);
                float dynSize =0.3F + (0.3F * a)  + 0.2F * rhombHit.get();

            float brightness = 1;
            float alha = 0.4F;
            RectUtil.drawRect(matrix, -dynSize / 2f, -dynSize / 2,
                    dynSize, dynSize,
                    color1S, color2S, color3S, color4S, true, true);
            RectUtil.drawRect(matrix, -dynSize / 2f, -dynSize / 2,
                    dynSize, dynSize,
                    ColorUtil.multAlpha(ColorUtil.multBright(color1S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color2S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color3S, brightness), alha * alphaPC),
                    ColorUtil.multAlpha(ColorUtil.multBright(color4S, brightness), alha * alphaPC), true, true);


            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.popMatrix();
            matrix.pop();
        }



        //switch (aura.targetType.getValue()) {
        if (aura.targetType.is("Кольцо")) {
            if (target == null) {
                return;
            }
            int i;
            EntityRendererManager rm = mc.getRenderManager();

            matrix.push();

            double x = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * (double) e.getPartialTicks() - rm.info.getProjectedView().getX();
            double y = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * (double) e.getPartialTicks() - rm.info.getProjectedView().getY();
            double z = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * (double) e.getPartialTicks() - rm.info.getProjectedView().getZ();

            matrix.translate(x, y, z);

            float height2 = target.getHeight();
            double duration = 1800;
            double elapsed = (double) System.currentTimeMillis() % duration;
            boolean side = elapsed > duration / 2.0;
            double progress = elapsed / (duration / 2.0);
            progress = side ? (progress -= 1.0) : 1.0 - progress;
            progress = progress < 0.5 ? 2.0 * progress * progress : 1.0 - Math.pow(-2.0 * progress + 2.0, 2.0) / 2.0;
            double eased = (double) (height2 / 1.7F) * (progress > 0.5 ? 1.0 - progress : progress) * (double) (side ? -1 : 1);

            GL11.glDepthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 0, 1);

            GL11.glDisable(GL_DEPTH_TEST);

            RenderSystem.lineWidth(1);
            RenderSystem.color4f(-1.0f, -1.0f, -1.0f, -1.0f);

            buffer.begin(8, DefaultVertexFormats.POSITION_COLOR);
            for (i = 0; i <= 360; ++i) {
                int c = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.gradient(ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.6F), ColorUtil.multDark(InterFace.getInstance().themeColor(), 1), i * 8, 1), alphaPC), redColor, hurtPC);
                buffer.pos(matrix.getLast().getMatrix(),
                                (float) (cos(Math.toRadians(i)) * (double) target.getWidth() * 1),
                                (float) ((double) height2 * progress),
                                (float) (sin(Math.toRadians(i)) * (double) target.getWidth() * 1))
                        .color(ColorUtil.replAlpha(c, (int) (200 * alpha.get()))).endVertex();

                buffer.pos(matrix.getLast().getMatrix(),
                                (float) (cos(Math.toRadians(i)) * (double) target.getWidth() * 1),
                                (float) ((double) height2 * progress + eased),
                                (float) (sin(Math.toRadians(i)) * (double) target.getWidth() * 1))
                        .color(ColorUtil.replAlpha(c, 0)).endVertex();
            }
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.color4f(-1.0f, -1.0f, -1.0f, -1.0f);
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            for (i = 0; i <= 360; ++i) {
                int c = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.gradient(ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.6F), ColorUtil.multDark(InterFace.getInstance().themeColor(), 1), i * 8, 1), alphaPC), redColor, hurtPC);
                buffer.pos(matrix.getLast().getMatrix(),
                                (float) (cos(Math.toRadians(i)) * (double) target.getWidth() * 1),
                                (float) ((double) height2 * progress),
                                (float) (sin(Math.toRadians(i)) * (double) target.getWidth() * 1))
                        .color(ColorUtil.replAlpha(c, (int) (230 * alpha.get()))).endVertex();
            }
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.enableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 0, 1);

            RenderSystem.enableCull();
            disableBlend();
            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
            GL11.glDepthMask(true);
            GL11.glDisable(2848);
            GL11.glHint(3154, 4354);
            RenderSystem.shadeModel(7424);

            GL11.glEnable(GL_DEPTH_TEST);

            matrix.pop();
        }

        if (aura.targetType.is("Духи")) {
            if (target == null) {
                return;
            }
            renderCelkaSkeed3D(target, matrix, e.getPartialTicks(), false);

        }


        if (aura.targetType.is("Призраки")) {
            if (target == null) {
                return;
            }
            renderCelkaSkeed3D4(target, matrix, e.getPartialTicks(), false);

          // matrix.push();
          // GL11.glDisable(GL_DEPTH_TEST);

          // RenderSystem.pushMatrix();
          // RenderSystem.disableLighting();
          // RenderSystem.depthMask(false);
          // RenderSystem.enableBlend();
          // RenderSystem.shadeModel(7425);
          // RenderSystem.disableCull();
          // RenderSystem.disableAlphaTest();
          // RenderSystem.blendFuncSeparate(770, 1, 0, 1);

          // double x = target.getPosX();
          // double y = target.getPosY() + target.getHeight() / 2f;
          // double z = target.getPosZ();
          // double radius = 0.75;
          // float speed = 25;
          // float size = 0.32f;
          // double distance = 16;
          // int lenght = 16;
          // int maxAlpha = (int) (255 * alpha.get());
          // int alphaFactor = 0;

          // ActiveRenderInfo camera = mc.getRenderManager().info;

          // matrix.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());

          // Vector3d interpolated = Mathf.interpolate(target.getPositionVec(), new Vector3d(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ), e.getPartialTicks());
          // interpolated.y += 0.85D;

          // matrix.translate(interpolated.x + 0.20000000298023224D, interpolated.y + (double) (target.getHeight() / 4.0F), interpolated.z);

          // mc.getTextureManager().bindTexture(new ResourceLocation("zetrix/texture/glow.png"));

          // float[] yLevels = new float[]{0.15F, +0.8f, -0.6f};
          // float pl = 0;
          // for (float yOff : yLevels) {
          //     for (int i = 0; i < lenght; i++) {

          //         Quaternion r = camera.getRotation().copy();
          //         buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

          //         // БАЗОВЫЙ угол
          //         double baseAngle = 0.15f * (System.currentTimeMillis() - Zetrix.startTime - (i * distance)) / speed;

          //         // если это МИД — инвертируем
          //         double angle = (yOff == 0.15F) ? -baseAngle : baseAngle;

          //         double s = Math.sin(angle) * radius;
          //         double c = Math.cos(angle) * radius;

          //         // + чуть колебания по Y
          //         double yOffsetWave = Math.sin((baseAngle) + (yOff)) * 0.35;
          //         double radians = Math.toRadians(i);
          //         double plY = Math.sin(radians * 1.4f) * 1.1F;

          //         ActiveRenderInfo cameraInfo = mc.gameRenderer.getActiveRenderInfo();
          //         Quaternion cameraRotation = cameraInfo.getRotation().copy();


          //         matrix.push();

          //         // РОВНЫЕ X Z
          //         matrix.translate(s, yOff- plY + yOffsetWave  , c);


          //         matrix.translate(-size / 2f, -size / 2f, 0);

          //         matrix.rotate(cameraRotation);

          //         matrix.translate(size / 2f, size / 2f, 0);

          //         int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);

          //         float shrinkFactor = 1.8f - (i / (float) lenght);
          //         float finsize = size  *  shrinkFactor  ;

          //         buffer.pos(matrix.getLast().getMatrix(), 0, -finsize, 0).color(ColorUtil.replAlpha(color1, alpha)).tex(0,0).endVertex();
          //         buffer.pos(matrix.getLast().getMatrix(), -finsize,-finsize, 0).color(ColorUtil.replAlpha(color2, alpha)).tex(0,1).endVertex();
          //         buffer.pos(matrix.getLast().getMatrix(), -finsize,0,0).color(ColorUtil.replAlpha(color3, alpha)).tex(1,1).endVertex();
          //         buffer.pos(matrix.getLast().getMatrix(), 0, 0, 0).color(ColorUtil.replAlpha(color4, alpha)).tex(1,0).endVertex();

          //         tessellator.draw();
          //         matrix.pop();
          //     }

          // }


          // matrix.translate(-x, -y, -z);
          // RenderSystem.defaultBlendFunc();
          // RenderSystem.disableBlend();
          // RenderSystem.enableCull();
          // RenderSystem.enableAlphaTest();
          // RenderSystem.depthMask(true);
          // RenderSystem.popMatrix();

          // GL11.glEnable(GL_DEPTH_TEST);

          // matrix.pop();
        }

        if (aura.targetType.is("Колбаски")) {
            if (target == null) {
            }

            /*MatrixStack ms = e.getMatrix();
            ActiveRenderInfo camera = mc.getRenderManager().info;
            Quaternion rotation = camera.getRotation().copy();
            EntityRendererManager rm = mc.getRenderManager();

            double x = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * e.getPartialTicks() - rm.info.getProjectedView().getX();
            double y = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * e.getPartialTicks() - rm.info.getProjectedView().getY();
            double z = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * e.getPartialTicks() - rm.info.getProjectedView().getZ();

            float baseHeight = (float) (0.32);
            boolean fa = true;

            float radius = 0.7f;
            float speed = 25f;
            float distance = 16;
            long time = System.currentTimeMillis();
            float currentSize = 0.5f;

            for (int b = 0; b < 3; b++) {
                for (int i = 0; i < 16; i++) {
                    double angle = 0.15f * (time - startTime - (i * distance)) / speed;
                    double rawX = Math.sin(angle) * radius;
                    double rawZ = Math.cos(angle) * radius;
                    double tiltAngle = Math.toRadians(15 + b * 10);
                    double s = rawX;
                    double vertical = baseHeight * Math.sin(tiltAngle) - Math.sin(-i * 0.13f - b) * 0.35f;
                    double c = rawZ * Math.cos(tiltAngle);

                    //    double vertical = baseHeight - Math.sin(-i * 0.14f - b) * 0.16f;


                    float factor = 1.0f - (i / 16f);
                    float dynamicSize = currentSize * (0.4f + 0.6f * factor);
                    int alphaFade = (int) (this.alpha.get() * (0.4f + 0.7f * factor));
                    int color = ColorUtil.replAlpha(ColorUtil.fade(), alphaPC);

                    ms.push();
                    ms.translate(x, y, z);
                    ms.translate(s, vertical, c);
                    ms.translate(-dynamicSize / 12, -dynamicSize / 12, 0);
                    ms.rotate(rotation);
                    ms.translate(dynamicSize / 12, dynamicSize / 12, 0);

                    GL11.glDepthMask(false);
                    drawImage(ms,
                            new ResourceLocation("zetrix/texture/glow.png"),
                            -dynamicSize / 2f, -dynamicSize / 12f,
                            0,
                            dynamicSize, dynamicSize,
                            color, color, color, color);

                    depthMask(true);
                    ms.pop();
                }

                radius *= -1.0f;
                fa = !fa;
                baseHeight += 0.55f;
            } */
            renderGhosts(matrix);


        }
    }


    private void renderCelkaSkeed3D2(LivingEntity target, MatrixStack ms, float partialTicks, boolean fadeEnabled) {
        final float speed = 4;
        final float verticalSpeed = 2;
        final float baseSizePx = 0.5F;
        final int brightness = 1;
        final int trailLength = 35;
        final float radiusConst = 0.65F;
        final float upperPosition = 1.6F; // голова
        final float lowerPosition = 0.5F; // ноги

        final EntityRendererManager rm = mc.getRenderManager();
        final ActiveRenderInfo camera = rm.info;
        final Quaternion rotation = camera.getRotation().copy();

        // позиция цели относительно камеры
        final double camX = camera.getProjectedView().x;
        final double camY = camera.getProjectedView().y;
        final double camZ = camera.getProjectedView().z;

        final double bx = MathHelper.lerp(partialTicks, target.lastTickPosX, target.getPosX()) - camX;
        final double by = MathHelper.lerp(partialTicks, target.lastTickPosY, target.getPosY()) - camY;
        final double bz = MathHelper.lerp(partialTicks, target.lastTickPosZ, target.getPosZ()) - camZ;

        // время/фазы
        final double t = System.currentTimeMillis() / (700.0 / speed);
        final double tv = System.currentTimeMillis() / (950.0 / speed);

        // цвета/альфа как в других призраках
        final float aPC = this.alpha.get();
        final float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));
        final int red = ColorUtil.getColor(190, 100, 100, (int) (255 * aPC));
        final int baseCol = InterFace.getInstance().clientColor();

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        float radius = radiusConst;

        // высоты для 3 призраков
        final float midPosition = (upperPosition + lowerPosition) / 2.0f;
        final float[] fixedY = new float[]{upperPosition, midPosition, lowerPosition};

        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < trailLength; j++) {
                float kf = j / (float) trailLength;
                float ease = 1.0f - kf;
                ease *= ease;
                ease *= ease;

                double tj = t - j * 0.05;
                double tvj = tv + j * 0.2;

                double tiltAngle = Math.toRadians(5 + k);

                float fo4s = 0.1f;
                double cyc = (Math.sin(tvj) + 1.0) * 0.6F + +Math.sin(tiltAngle) + Math.sin(-j * 0.06F) * 0.3F;

                // сохраняем вращение по X/Z (не меняем X/Z), Y будет немного колебаться
                double baseAngle = Math.toRadians(k * 120.0 + (tj * 50.0 % 360.0));
                double offX = Math.cos(baseAngle) * radius;
                double offZ = Math.sin(baseAngle) * radius;

                // Y фиксирован для каждой "души", плюс небольшое дыхание и лёгкий вертикальный дрейф
                double breathRange = 0.5F; // чуть больше "дыхание"
                double slowDriftAmp = 0.08; // амплитуда дополнительного вертикального движения (чуть)
                // drift: разные фазы для каждой души и небольшой лаг для трейла (уменьшается с j)
                double drift = Math.sin(t * 0.18 + k * (2.0) + j * 0.12) * slowDriftAmp * (1.0 - kf);
                double offY = fixedY[k] + (cyc - 0.5) * breathRange;

                // размер и прозрачность
                kf = j / (float) trailLength;
                float sizeFactor = 1.0f - (kf * 0.7f);
                float dynSize = baseSizePx * sizeFactor;
                int dynAlpha = (int) (255 * aPC);

                int color = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(0), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));
                int color2 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(90), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));
                int color3 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(180), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));
                int color4 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(270), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                // рендер
                ms.push();
                ms.translate(bx + offX, by + offY, bz + offZ);
                ms.translate(-dynSize / 12f, -dynSize / 12f, 0f);
                ms.rotate(rotation);
                ms.translate(dynSize / 12f, dynSize / 12f, 0f);
                RenderUtil.bindTexture(new Namespaced("texture/glow.png"));
                RectUtil.drawRect(ms, -dynSize / 2f, -dynSize / 12f,
                        dynSize, dynSize,
                        color, color2, color3, color4, true, true);


                ms.pop();
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
    }


    public static void drawImage(MatrixStack stack, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        boolean blend = glIsEnabled(GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        BUFFER.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        BUFFER.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUFFER.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        BUFFER.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        BUFFER.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();
        TESSELLATOR.draw();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        glShadeModel(GL_FLAT);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ZERO);
        if (!blend)
            GlStateManager.disableBlend();
    }


    public static void drawImage(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        mc.getTextureManager().bindTexture(image);
        drawImage(stack, x, y, z, width, height, color1, color2, color3, color4);
    }

    public static void drawImageLocal(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        Minecraft minecraft = Minecraft.getInstance();
        GlStateManager.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL_QUADS, POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();

        tessellator.draw();
        RenderSystem.defaultBlendFunc();
        disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();


    }

    public ArrayList<Particle> particles = new ArrayList<>();
    public static long lifeTime = 1000L;
    public static float deltaTime = 0.0f;
    private static final int PARTICLES_PER_SPAWN = 2;
    private static final float SPAWN_INTERVAL = 0.05f;
    private final float spawnAccumulator = 0f;

  /*  @EventHandler
    public void onRender(EventRenderer3D e) {
        if (AttackAura.getInstance().targetType.is("Кубики")) {
            long currentTime = System.currentTimeMillis();
            deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;


            target = AttackAura.getInstance().getTarget();
            boolean show = target != null;
            if (show) {
                spawnAccumulator += deltaTime;

                while (spawnAccumulator >= SPAWN_INTERVAL) {
                    spawnAccumulator -= SPAWN_INTERVAL;

                    for (int i = 0; i < PARTICLES_PER_SPAWN; i++) {
                        double rand = MathHelper.getRandomNumberBetween(0, 360);

                        double x = Math.cos(rand * Math.PI / 180) * 0.6f;
                        double y = MathHelper.getRandomNumberBetween(-0.05f, 0.2f);
                        double z = Math.sin(rand * Math.PI / 180) * 0.6f;
                        this.particles.add(new Particle(target, x, y, z));
                    }
                }
            }

            if (!particles.isEmpty()) {
                for (Particle particle : particles) {
                    particle.render();
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        //if (AttackAura.getInstance().targetType.is("Кубики")) {
        //    this.particles.removeIf(particle -> particle.animation.getDirection() != Direction.FORWARDS && particle.animation.getOutput() == 0);
        //}
    }*/


    public static class Particle {
        double x, y, z;
        double posX, posY, posZ;
        double motionX, motionY, motionZ;
        long time;
        LivingEntity entity;
        dev.wh1tew1ndows.client.utils.animation.animation.Animation animation = new EaseInOutQuad(500, 1);

        public Particle(LivingEntity entity, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entity = entity;
            this.time = System.currentTimeMillis();
        }

        public long getTime() {
            return time;
        }

        public void update() {
            animation.setDirection(((System.currentTimeMillis() - this.getTime()) <= lifeTime - 400L) ? Direction.FORWARDS : Direction.BACKWARDS);
            this.y += MathHelper.getRandomNumberBetween(0.01f, 0.04f) * (deltaTime * 60);
            if (entity != null) {
                this.motionX = x + entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks();
                this.motionY = y + entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks();
                this.motionZ = z + entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks();
            }
        }

        public void render() {
            this.update();

            double rotation = (System.currentTimeMillis() - this.getTime()) / 5f;
            posX = MathHelper.interpolate(posX, this.motionX - mc.getRenderManager().info.getProjectedView().getX(), 0.2f);
            posY = MathHelper.interpolate(posY, this.motionY - mc.getRenderManager().info.getProjectedView().getY(), 0.2f);
            posZ = MathHelper.interpolate(posZ, this.motionZ - mc.getRenderManager().info.getProjectedView().getZ(), 0.2f);
            float pitch = mc.getRenderManager().info.getPitch();
            float yaw = mc.getRenderManager().info.getYaw();
            float scale = 0.1f;

            int color = RenderUtil.Colors.swapAlpha(InterFace.getInstance().clientColor(), animation.getOutput() * 255);

            int hurtTicks = AttackAura.getInstance().target != null ? AttackAura.getInstance().target.hurtTime : 0;
            float hurtPC = (float) Math.sin((double) hurtTicks * (Math.PI / 10D));
            int redColor = ColorUtil.getColor(200, 90, 90, (int) (255.0F * animation.getOutput()));
            int colorSolo = ColorUtil.overCol(ColorUtil.multAlpha(InterFace.getInstance().clientColor(), animation.getOutput()), redColor, hurtPC);
            int colorGlow1 = RenderUtil.Colors.swapAlpha(InterFace.getInstance().clientColor(), animation.getOutput() * 120);
            int colorGlow2 = RenderUtil.Colors.swapAlpha(InterFace.getInstance().clientColor(), animation.getOutput() * 50);

            float lineWidth = 1;
            boolean glShadeGradient = false;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glShadeModel(glShadeGradient ? GL11.GL_SMOOTH : GL11.GL_FLAT);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_LIGHTING);
            LightTexture.disableLightmap();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(lineWidth);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
            GL11.glPushMatrix();
            GL11.glTranslated(posX, posY, posZ);
            GL11.glRotated(rotation, 1, 1, 1);
            GL11.glScaled(scale, scale, scale);
            GL11.glDisable(GL_DEPTH_TEST);
            drawAxisBox(new AxisAlignedBB(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f), RenderUtil.Colors.swapAlpha(color, (float) RenderUtil.Colors.getAlphaFromColor(color) / 4.0F), RenderUtil.Colors.swapAlpha(color, (float) RenderUtil.Colors.getAlphaFromColor(color) / 12.0F));
            GL11.glPopMatrix();
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glLineWidth(1.0F);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (glShadeGradient) GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glDepthMask(false);
            GL11.glTranslated(posX, posY, posZ);
            GL11.glRotatef(-yaw, 0, 1, 0);
            GL11.glRotatef(pitch, 1, 0, 0);
            GL11.glScalef(-scale, -scale, scale);
            RenderUtil.Images.drawImage(new MatrixStack(), new ResourceLocation("zetrix" + "/texture/dashbloom.png"), -2.5f, -2.5f, 0, 5, 5, colorGlow2, colorGlow2, colorGlow2, colorGlow2, true);
            RenderUtil.Images.drawImage(new MatrixStack(), new ResourceLocation("zetrix" + "/texture/dashbloomsample.png"), -1, -1, 0, 2, 2, colorGlow1, colorGlow1, colorGlow1, colorGlow1, true);
            GL11.glDepthMask(true);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL_DEPTH_TEST);
            GL11.glPopMatrix();
        }

    }


    public static Tessellator tessellator = Tessellator.getInstance();
    public static BufferBuilder buffer = tessellator.getBuffer();

    public static void drawAxisBox(AxisAlignedBB aabb, int colorOut, int colorFill) {
        if (aabb != null && (RenderUtil.Colors.getAlphaFromColor(colorOut) != 0 || RenderUtil.Colors.getAlphaFromColor(colorFill) != 0)) {
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorOut).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorOut).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.minX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(colorFill).endVertex();
            buffer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(colorFill).endVertex();
            tessellator.draw();
        }
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }





    private final Namespaced ghostTexture = new Namespaced("particle/glow.png");
    private final GParticle[] gParticles = new GParticle[3];
    private float orbitAngle = 0;
    private long lastFrameTime = System.nanoTime();


    private void renderGhosts(MatrixStack matrix) {
        long now = System.nanoTime();
        float deltaTime = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;

        orbitAngle += 3 * deltaTime;


        Vector3d targetPos = target != null ? RenderUtil3D.interpolate(target, mc.getRenderPartialTicks()) : null;

        float alphaPC = alpha.get();

        if (targetPos != null && gParticles[0] == null) {
            for (int i = 0; i < 3; i++) {
                float angleOffset = (float) (i * 2 * Math.PI / 3);

                float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));

                int red = ColorUtil.getColor(255, 0, 0, alphaPC);

                int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(i * 100), alphaPC), red, hurtPC);
                gParticles[i] = new GParticle(targetPos, angleOffset, i, color, 0.3f);
            }
        }

        setupRenderState();

        matrix.push();
        for (GParticle particle : gParticles) {
            if (particle != null) {
                particle.update(targetPos, orbitAngle, alpha.get());
                particle.tailAnimation.update();


                if (target == null) {
                    particle.tailAnimation.run(0, 0.5, Easings.CUBIC_OUT, true);
                } else {
                    particle.tailAnimation.run(1.0, 0.5, Easings.CUBIC_OUT, true);
                }

                renderTail(matrix, particle);
            }
        }
        matrix.pop();

        resetRenderState();
    }


    private void renderTail(MatrixStack matrix, GParticle particle) {
        if (particle.targetPos == null) return;
        float alphaPC = 1 * this.alpha.get();

        float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));

        int red = ColorUtil.getColor(180, 80, 80, alphaPC);

        int color = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(), alphaPC), red, hurtPC);

        float startAngle = particle.currentAngle;
        float endAngle = startAngle - (float) (Math.PI / 6 + Math.PI / 6);
        for (float angle = startAngle; angle > endAngle; angle -= 0.035f) {
            int tailColor = ColorUtil.multAlpha(color,(this.alpha.get() * 0.7F) * (0.8f - (startAngle - angle) / 1.4F));
            float tailSize = (float) (0.08f * (1.6f - (startAngle - angle) / 1.7F) );

            float radius = 0.7f;
            float x = (float) (particle.targetPos.x + Math.cos(angle) * radius);


            float y = (float) ((float) ((float) ((float) (particle.targetPos.y + 1.0f +  Math.sin(2 + System.currentTimeMillis() / 700.0) * 0.6f) ) + Math.sin(angle + System.currentTimeMillis() / 400.0) * 0.2f)  + Math.sin(angle * 1.2F) * 0.2f);
            float z = (float) (particle.targetPos.z + Math.sin(angle) * radius);

            matrix.push();
            RenderUtil3D.setupOrientationMatrix(matrix, x, y, z);
            matrix.rotate(mc.getRenderManager().getCameraOrientation());
            matrix.rotate(Vector3f.ZP.rotationDegrees(180));
            matrix.translate(0, -tailSize, 0);

            GL11.glDisable(GL_DEPTH_TEST);
            RenderUtil.bindTexture(ghostTexture);
            RectUtil.drawRect(matrix, -tailSize * 3, -tailSize * 3, tailSize * 6, tailSize * 6, ColorUtil.multAlpha(tailColor, 0.3f), true, true);

            RectUtil.drawRect(matrix, -tailSize * 2, -tailSize * 2, tailSize * 4, tailSize * 4, ColorUtil.multAlpha(tailColor, 0.6f), true, true);

            RectUtil.drawRect(matrix, -tailSize, -tailSize, tailSize * 2, tailSize * 2, tailColor, true, true);
            GL11.glEnable(GL_DEPTH_TEST);
            matrix.pop();
        }
    }


    public static class GParticle {
        private Vector3d position;
        public Vector3d targetPos;
        public float currentAngle;
        private final float angleOffset;
        private final int index;
        private final int color;
        private final float size;
        private final StopWatch time = new StopWatch();
        private final Animation animation = new Animation();
        public final Animation tailAnimation = new Animation();

        public GParticle(Vector3d position, float angleOffset, int index, int color, float size) {
            this.position = position;
            this.targetPos = position;
            this.angleOffset = angleOffset;
            this.index = index;
            this.color = color;
            this.size = size;
            this.time.reset();
        }

        public void update(Vector3d targetPos, float orbitAngle, float markerAlpha) {
            if (targetPos != null) {
                this.targetPos = targetPos;
                float radius = 0.8f;
                currentAngle = orbitAngle + angleOffset;
                float x = (float) (targetPos.x + Math.cos(currentAngle) * radius);
                float y = (float) (targetPos.y + 1.0f + Math.sin(currentAngle * 0.5f) * 0.2f);
                float z = (float) (targetPos.z + Math.sin(currentAngle) * radius);
                position = new Vector3d(x, y, z);
            }
        }

        public Vector3d position() {
            return position;
        }

        public int color() {
            return color;
        }

        public float size() {
            return size;
        }

        public StopWatch time() {
            return time;
        }

        public Animation animation() {
            return animation;
        }

        public Animation tailAnimation() {
            return tailAnimation;
        }
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }

    private void resetRenderState() {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.clearCurrentColor();
        RenderSystem.enableCull();
        depthMask(true);
        RenderSystem.enableAlphaTest();
    }


    private void renderCelkaSkeed3D4(LivingEntity target, MatrixStack ms, float partialTicks, boolean fadeEnabled) {
        final float speed = 3;
        final float verticalSpeed = 6;
        final float baseSizePx = 0.6F;
        final int brightness = 1;
        final int trailLength = 25;
        final float radiusConst = 0.65F;
        final float upperPosition = 1.8F;
        final float lowerPosition = 0.1F;

        final EntityRendererManager rm = mc.getRenderManager();
        final ActiveRenderInfo camera = rm.info;
        final Quaternion rotation = camera.getRotation().copy();

        // позиция цели относительно камеры
        final double camX = camera.getProjectedView().x;
        final double camY = camera.getProjectedView().y;
        final double camZ = camera.getProjectedView().z;

        final double bx = MathHelper.lerp(partialTicks, target.lastTickPosX, target.getPosX()) - camX;
        final double by = MathHelper.lerp(partialTicks, target.lastTickPosY, target.getPosY()) - camY;
        final double bz = MathHelper.lerp(partialTicks, target.lastTickPosZ, target.getPosZ()) - camZ;

        // время/фазы
        final double t = System.currentTimeMillis() / (500.0 / speed);
        final double tv = System.currentTimeMillis() / (1000.0 / verticalSpeed);

        // цвета/альфа как в других призраках
        final float aPC = this.alpha.get();
        final float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));
        final int red = ColorUtil.getColor(190, 100, 100, (int) (255 * aPC));
        final int baseCol = InterFace.getInstance().clientColor();

        // GL-состояния — как у "Призраки кастом"
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        float radius = radiusConst;
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < trailLength; j++) {
                float kf = j / (float) trailLength;
                float ease = 1.0f - kf;
                ease *= ease;
                ease *= ease;     // мягкое затухание хвоста

                double tj = t - j * 0.05;
                double tvj = tv - j * 0.05;
                double cyc = (Math.sin(tvj) + 1.0) * 0.5;               // 0..1 «дыхание»

                // угол для конкретной души + вращение
                double baseAngle = Math.toRadians(k * 90.0 + (tj * 50.0 % 360.0));

                double offX = Math.cos(baseAngle) * radius;
                double offZ = Math.sin(baseAngle) * radius;
                double offY = (k % 2 == 0)
                        ? lowerPosition + (upperPosition - lowerPosition) * cyc
                        : upperPosition - (upperPosition - lowerPosition) * cyc;

                // размер хвоста и альфа
                // размер хвоста и альфа
                kf = j / (float) trailLength;     // 0 (начало) → 1 (конец)
                float sizeFactor = 1.0f - (kf * 0.6f); // чем дальше, тем меньше (на ~60%)
                float dynSize = baseSizePx * sizeFactor ;
                int dynAlpha = (int) (200 * alpha.getValue() * sizeFactor);


                int color = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 2), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));
                int color2 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 4), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                int color3 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 8), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                int color4 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 16), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                // как у других: translate -> offset -> rotate(camera) -> drawImage
                ms.push();
                ms.translate(bx + offX, by + offY, bz + offZ);
                ms.translate(-dynSize / 12f, -dynSize / 12f, 0f);
                ms.rotate(rotation);
                ms.translate(dynSize / 12f, dynSize / 12f, 0f);

                RenderUtil.bindTexture(new Namespaced("texture/glow.png"));
                RectUtil.drawRect(ms, -dynSize / 2f, -dynSize / 12f,
                        dynSize, dynSize,
                        color, color2, color3, color4, true, true);
                // РЕНДЕР ТОЛЬКО ЧЕРЕЗ drawImage
                // drawImage(ms, new Namespaced("texture/glow.png"),
                //         -dynSize / 2f, -dynSize / 12f, 0f,
                //         dynSize, dynSize,
                //         color, color2, color3, color4);

                ms.pop();
            }
            // симметрия пары, как в твоём кастоме
            radius *= -1.0f;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
    }

    // --- "Тест": только drawImage, 3D биллборд как у других, фиксированные константы ---
    private void renderCelkaSkeed3D(LivingEntity target, MatrixStack ms, float partialTicks, boolean fadeEnabled) {
        final float speed = 1.3F;
        final float verticalSpeed = 1.5F;
        final float baseSizePx = 0.6F;
        final int brightness = 1;
        final int trailLength = 20;
        final float radiusConst = 0.65F;
        final float upperPosition = 1.8F;
        final float lowerPosition = 0.1F;

        final EntityRendererManager rm = mc.getRenderManager();
        final ActiveRenderInfo camera = rm.info;
        final Quaternion rotation = camera.getRotation().copy();

        // позиция цели относительно камеры
        final double camX = camera.getProjectedView().x;
        final double camY = camera.getProjectedView().y;
        final double camZ = camera.getProjectedView().z;

        final double bx = MathHelper.lerp(partialTicks, target.lastTickPosX, target.getPosX()) - camX;
        final double by = MathHelper.lerp(partialTicks, target.lastTickPosY, target.getPosY()) - camY;
        final double bz = MathHelper.lerp(partialTicks, target.lastTickPosZ, target.getPosZ()) - camZ;

        // время/фазы
        final double t = System.currentTimeMillis() / (500.0 / speed);
        final double tv = System.currentTimeMillis() / (1000.0 / verticalSpeed);

        // цвета/альфа как в других призраках
        final float aPC = this.alpha.get();
        final float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));
        final int red = ColorUtil.getColor(190, 100, 100, (int) (255 * aPC));
        final int baseCol = InterFace.getInstance().clientColor();

        // GL-состояния — как у "Призраки кастом"
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        float radius = radiusConst;
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < trailLength; j++) {
                float kf = j / (float) trailLength;
                float ease = 1.0f - kf;
                ease *= ease;
                ease *= ease;     // мягкое затухание хвоста

                double tj = t - j * 0.05;
                double tvj = tv - j * 0.05;
                double cyc = (Math.sin(tvj) + 1.0) * 0.5;               // 0..1 «дыхание»

                // угол для конкретной души + вращение
                double baseAngle = Math.toRadians(k * 90.0 + (tj * 50.0 % 360.0));

                double offX = Math.cos(baseAngle) * radius;
                double offZ = Math.sin(baseAngle) * radius;
                double offY = (k % 2 == 0)
                        ? lowerPosition + (upperPosition - lowerPosition) * cyc
                        : upperPosition - (upperPosition - lowerPosition) * cyc;

                // размер хвоста и альфа
                // размер хвоста и альфа
                kf = j / (float) trailLength;     // 0 (начало) → 1 (конец)
                float sizeFactor = 1.0f - (kf * 0.6f); // чем дальше, тем меньше (на ~60%)
                float dynSize = baseSizePx * sizeFactor ;
                int dynAlpha = (int) (200 * alpha.getValue() * sizeFactor);


                int color = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 2), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));
                int color2 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 4), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                int color3 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 8), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                int color4 = ColorUtil.replAlpha(ColorUtil.overCol(
                        ColorUtil.multAlpha(ColorUtil.fade(j * 16), aPC),
                        red,
                        hurtPC
                ), MathHelper.clamp(dynAlpha, 0, 255));

                // как у других: translate -> offset -> rotate(camera) -> drawImage
                ms.push();
                ms.translate(bx + offX, by + offY, bz + offZ);
                ms.translate(-dynSize / 12f, -dynSize / 12f, 0f);
                ms.rotate(rotation);
                ms.translate(dynSize / 12f, dynSize / 12f, 0f);

                RenderUtil.bindTexture(new Namespaced("texture/glow.png"));
                RectUtil.drawRect(ms, -dynSize / 2f, -dynSize / 12f,
                        dynSize, dynSize,
                        color, color2, color3, color4, true, true);
                // РЕНДЕР ТОЛЬКО ЧЕРЕЗ drawImage
                // drawImage(ms, new Namespaced("texture/glow.png"),
                //         -dynSize / 2f, -dynSize / 12f, 0f,
                //         dynSize, dynSize,
                //         color, color2, color3, color4);

                ms.pop();
            }
            // симметрия пары, как в твоём кастоме
            radius *= -1.0f;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
    }

// ================= FPS-INDEPENDENT TRAILS (для «Призраки 2») =================

    @SuppressWarnings("unchecked")
    private final ArrayDeque<Vector3d>[] orbTrails = new ArrayDeque[4];
    private boolean orbTrailsInit = false;

    // фиксированная частота семплинга трейлов 60 Гц (1 точка каждые ~16.67 мс)
    private static final double TRAIL_SAMPLE_DT = 0.0000000005F;
    private final double[] trailLastSampleSec = new double[4];

    // длина следа и визуальные параметры
    private static final int TRAIL_POINTS_MAX = 75;
    private static final float TRAIL_BASE_PX = 0.22f;
    private static final float TRAIL_TAPER = 0.65f;
    private static final float TRAIL_ALPHA = 0.85f;

    // стабильное время, полностью независимое от FPS и partialTicks
    private double lastRealTimeSec = System.nanoTime() / 1_000_000_000.0;
    private double totalStableTimeSec = 0.0;

    private double getStableTimeSec() {
        double now = System.nanoTime() / 1_000_000_000.0;
        double dt = now - lastRealTimeSec;
        lastRealTimeSec = now;
        // ограничение на случай лагов (более 100 мс пропуск)
        dt = Math.min(dt, 0.1);
        totalStableTimeSec += dt;
        return totalStableTimeSec;
    }

    private void resetTrails() {
        for (int i = 0; i < orbTrails.length; i++) {
            if (orbTrails[i] != null) orbTrails[i].clear();
            trailLastSampleSec[i] = 0d;
        }
        totalStableTimeSec = 0.0;
        lastRealTimeSec = System.nanoTime() / 1_000_000_000.0;
    }

    private void pushOrbPoint(int orbIndex, double worldX, double worldY, double worldZ) {
        ArrayDeque<Vector3d> q = orbTrails[orbIndex];
        q.addFirst(new Vector3d(worldX, worldY, worldZ));
        while (q.size() > TRAIL_POINTS_MAX) q.removeLast();
    }

    // === «Призраки 2»: FPS-независимая анимация и трейлы ===
    private void renderCelkaSkeed3D3(LivingEntity target, MatrixStack ms, float partialTicks, boolean fadeEnabled) {
        final float baseSizePx = 0.25F;
        final int trailLength = 90;
        final float radiusConst = 0.6F;
        final float upperPosition = 1.85F;
        final float lowerPosition = 0.2F;

        final float headScale = 1.6f;
        final float tailScale = 0.6F;

        final float sizeEasePow = 1.1f;
        final float alphaEasePow = 0.5F;
        final float stepFactor = 0.035f;

        final EntityRendererManager rm = mc.getRenderManager();
        final ActiveRenderInfo camera = rm.info;
        final Quaternion rotation = camera.getRotation().copy();

        final double camX = camera.getProjectedView().x;
        final double camY = camera.getProjectedView().y;
        final double camZ = camera.getProjectedView().z;

        final double bx = MathHelper.lerp(partialTicks, target.lastTickPosX, target.getPosX()) - camX;
        final double by = MathHelper.lerp(partialTicks, target.lastTickPosY, target.getPosY()) - camY;
        final double bz = MathHelper.lerp(partialTicks, target.lastTickPosZ, target.getPosZ()) - camZ;

        // === стабильное реальное время (секунды), не зависит от FPS ===
        final double nowSec = getStableTimeSec();

        // фиксированные частоты в Гц
        final double rotHz = 1.2F;
        final double vertHz = 1.4F;

        final double rotPhase = nowSec * rotHz;
        final double vertPhase = nowSec * vertHz;

        final float aPC = this.alpha.get(); // 0..1
        final float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));
        final int red = ColorUtil.getColor(190, 100, 100, (int) (255 * aPC));
        final int baseCol = InterFace.getInstance().clientColor();

        // инициализация буферов следов
        if (!orbTrailsInit) {
            for (int i = 0; i < orbTrails.length; i++) orbTrails[i] = new ArrayDeque<>();
            orbTrailsInit = true;
        }

        GL11.glPushMatrix();
        GL11.glDisable(GL_DEPTH_TEST);

        float radius = radiusConst;
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < trailLength; j++) {
                float kf = j;
                float s = kf * kf * (3f - 2f * kf);
                float ease = 1f - s;

                float sizeScale = tailScale + (float) Math.pow(ease, sizeEasePow);
                float dynSize = baseSizePx * sizeScale;

                float alphaMul = (float) Math.pow(ease, alphaEasePow);
                float noise = 0.9f + 0.1f * (float) Math.sin(j * 18.9898 + k * 78.233);

                int finalAlpha = (int) (255f * aPC * alphaMul * noise);
                finalAlpha = MathHelper.clamp(finalAlpha, 0, 255);

                // фазовый сдвиг между сегментами
                final double segPhaseSec = 0.035;

                final double tj = (rotPhase - j * segPhaseSec) % 1.0;
                final double tvj = (vertPhase - j * segPhaseSec);

                final double cyc = (Math.sin(2.0 * Math.PI * tvj) + 1.0) * 0.5;

                float dynRadius = radius * (0.85f + 0.15f * ease);

                final double baseAngleDeg = k * 90.0 + (tj * 360.0);
                final double baseAngleRad = Math.toRadians(baseAngleDeg);

                double offX = Math.cos(baseAngleRad) * dynRadius;
                double offZ = Math.sin(baseAngleRad) * dynRadius;
                double offY = (k % 2 == 0)
                        ? lowerPosition + (upperPosition - lowerPosition) * cyc
                        : upperPosition - (upperPosition - lowerPosition) * cyc;

                int mixed = ColorUtil.overCol(ColorUtil.multAlpha(baseCol, aPC), red, hurtPC);
                int color = ColorUtil.replAlpha(mixed, finalAlpha);
                int haloA = (int) (finalAlpha * 0.45f);
                int haloCol = ColorUtil.replAlpha(mixed, haloA);

                double px = bx + offX;
                double py = by + offY;
                double pz = bz + offZ;

                // стабильный семплинг трейла — только голова (j == 0)
                if (j == 0) {
                    double tNow = nowSec;
                    if ((tNow - trailLastSampleSec[k]) >= 0.0000000005F) {
                        pushOrbPoint(k, px + camX, py + camY, pz + camZ);
                        trailLastSampleSec[k] = tNow;
                    }
                }
            }
            radius *= -1.0f;
        }

        // дорисовка шлейфа
        final float TRAIL_MATCH_SCALE = 1f;
        final float TRAIL_NOISE_K = 0;

        for (int k = 0; k < 3; k++) {
            ArrayDeque<Vector3d> q = orbTrails[k];
            if (q == null || q.isEmpty()) continue;

            int idx = 0;
            for (Vector3d p : q) {
                float f = idx / (float) (TRAIL_POINTS_MAX - 1);
                float inv = 1f - f;

                float noise = 0.9f + 0.1f * (float) Math.sin(idx * 18.9898 + k * 28.233);
                noise = 1.0f + (noise - 1.0f) * TRAIL_NOISE_K;

                float sizeScale = tailScale + (float) Math.pow(inv, sizeEasePow);
                float dynSize = baseSizePx * sizeScale * TRAIL_MATCH_SCALE;

                int finalAlpha = (int) (255f * aPC * Math.pow(inv, alphaEasePow) * noise);
                finalAlpha = MathHelper.clamp(finalAlpha, 0, 255);

                int mixed = ColorUtil.overCol(ColorUtil.multAlpha(baseCol, aPC), red, hurtPC);
                int color = ColorUtil.replAlpha(mixed, finalAlpha);
                int haloA = (int) (finalAlpha * 0.45f);
                int haloCol = ColorUtil.replAlpha(mixed, haloA);

                double x = p.x - camX, y = p.y - camY, z = p.z - camZ;

                ms.push();
                ms.translate(x, y, z);
                ms.rotate(rotation);
                RenderUtil.bindTexture(new Namespaced("texture/glow.png"));
                RectUtil.drawRect(ms,
                        -dynSize * 0.65f, -dynSize * 0.65f,
                        dynSize * 1.30f, dynSize * 1.30f,
                        haloCol, haloCol, haloCol, haloCol, true, true);

                RectUtil.drawRect(ms,
                        -dynSize * 0.5f, -dynSize * 0.5f,
                        dynSize, dynSize,
                        color, color, color, color, true, true);

                ms.pop();

                idx++;
                if (idx >= TRAIL_POINTS_MAX) break;
            }
        }

        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glPopMatrix();

    }

    // стандартный метод рендера текстуры
    public static void drawImage2(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        Minecraft minecraft = Minecraft.getInstance();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL_QUADS, POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();
        tessellator.draw();
        disableBlend();
    }


}
