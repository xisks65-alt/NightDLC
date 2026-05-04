package dev.wh1tew1ndows.client.screen.glowUtil;

import com.google.common.collect.Queues;
import dev.wh1tew1ndows.client.managers.module.impl.misc.Optimizer;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.IRenderCall;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mc;
import static dev.wh1tew1ndows.client.utils.render.shader.impl.GaussianBlur.calculateGaussianValue;
import static net.mojang.blaze3d.systems.RenderSystem.glUniform1;


public class BloomHelper {

    private static final ShaderManager bloom = ShaderManager.create(new BloomShader1());
    private static final ShaderManager bloomC = ShaderManager.create(new BloomShader2());
    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();

    private static final ConcurrentLinkedQueue<IRenderCall> renderQueueHand = Queues.newConcurrentLinkedQueue();

    private static final Framebuffer inFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final Framebuffer outFrameBuffer = new Framebuffer(1, 1, true, false);

    public static void registerRenderCall(IRenderCall rc) {
        renderQueue.add(rc);
    }

    public static void registerRenderCallHand(IRenderCall rc) {
        renderQueueHand.add(rc);
    }

    public static void draw(int radius, float exp, boolean fill) {
        if (renderQueue.isEmpty() || (Optimizer.getInstance().isEnabled() && Optimizer.getInstance().checks().getValue("Глов текста")))
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);
        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }
        inFrameBuffer.unbindFramebuffer();

        outFrameBuffer.bindFramebuffer(true);

        bloom.load();
        bloom.setUniformf("radius", radius);
        bloom.setUniformf("exposure", exp);
        bloom.setUniform("textureIn", 0);
        bloom.setUniform("textureToCheck", 20);
        bloom.setUniform("avoidTexture", fill ? 1 : 0);
        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(bloom.getUniform("weights"), weightBuffer);
        bloom.setUniformf("texelSize", 1.0F / (float) Minecraft.getInstance().getMainWindow().getWidth(), 1.0F / (float) Minecraft.getInstance().getMainWindow().getHeight());
        bloom.setUniformf("direction", 1.0F, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        ShaderManager.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        bloom.setUniformf("direction", 0.0F, 1.0F);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderManager.drawQuads();

        bloom.unload();
        outFrameBuffer.unbindFramebuffer();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
        mc.getFramebuffer().bindFramebuffer(false);
    }

    public static void draw(int radius, float exp, boolean fill, float direction) {
        if (renderQueue.isEmpty() || (Optimizer.getInstance().isEnabled() && Optimizer.getInstance().checks().getValue("Глов текста")))
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);
        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }
        inFrameBuffer.unbindFramebuffer();

        outFrameBuffer.bindFramebuffer(true);

        bloom.load();
        bloom.setUniformf("radius", radius);
        bloom.setUniformf("exposure", exp);
        bloom.setUniform("textureIn", 0);
        bloom.setUniform("textureToCheck", 20);
        bloom.setUniform("avoidTexture", fill ? 1 : 0);
        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(bloom.getUniform("weights"), weightBuffer);
        bloom.setUniformf("texelSize", 1.0F / (float) Minecraft.getInstance().getMainWindow().getWidth(), 1.0F / (float) Minecraft.getInstance().getMainWindow().getHeight());
        bloom.setUniformf("direction", direction, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        ShaderManager.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        bloom.setUniformf("direction", 0.0F, direction);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderManager.drawQuads();

        bloom.unload();
        outFrameBuffer.unbindFramebuffer();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
        mc.getFramebuffer().bindFramebuffer(false);
    }

    public static Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth() || frameBuffer.framebufferHeight != mc.getMainWindow().getHeight())
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getHeight()), false);
        else
            frameBuffer.framebufferClear(false);
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }

    public static void drawC(int radius, float exp, boolean fill, int color, float mult) {
        if (renderQueue.isEmpty() || (Optimizer.getInstance().isEnabled() && Optimizer.getInstance().checks().getValue("Глов текста")))
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);
        while (!renderQueueHand.isEmpty()) {
            renderQueueHand.poll().execute();
        }
        inFrameBuffer.unbindFramebuffer();

        outFrameBuffer.bindFramebuffer(true);

        bloomC.load();
        bloomC.setUniformf("radius", radius);
        bloomC.setUniformf("exposure", exp);
        bloomC.setUniform("textureIn", 0);
        bloomC.setUniform("textureToCheck", 20);
        bloomC.setUniform("avoidTexture", fill ? 1 : 0);
        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(bloomC.getUniform("weights"), weightBuffer);
        bloomC.setUniformf("texelSize", 1.0F / (float) Minecraft.getInstance().getMainWindow().getWidth(), 1.0F / (float) Minecraft.getInstance().getMainWindow().getHeight());
        bloomC.setUniformf("direction", mult, 0.0F);
        float[] triColor = RenderUtil.Colors.rgb(color);
        bloomC.setUniformf("color", triColor[0], triColor[1], triColor[2], triColor[3]);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        ShaderManager.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        bloomC.setUniformf("direction", 0.0F, mult);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderManager.drawQuads();

        bloomC.unload();
        outFrameBuffer.unbindFramebuffer();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }


}