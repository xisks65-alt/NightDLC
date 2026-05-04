package dev.wh1tew1ndows.client.utils.render.shader.impl;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.utils.render.draw.StencilUtil;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static net.mojang.blaze3d.systems.RenderSystem.glUniform1;

public class GaussianBlur implements IMinecraft {

    private static final ShaderManager gaussianBlur = ShaderManager.blur;

    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniformi("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(), 1.0F / (float) mc.getMainWindow().getHeight());
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur() {
        StencilUtil.enable();
    }

    public static void blur(Runnable data, float blur, int com) {
        if (blur > 0) {
            StencilUtil.enable();
            data.run();
            StencilUtil.read(1);
            blur(blur, com);
            StencilUtil.disable();
        }

    }

    public static void endBlur(float radius, float compression) {
        if (!Minecraft.isScreenMinimized() && radius > 0) {
            StencilUtil.read(1);

            framebuffer = createFrameBuffer(framebuffer);

            framebuffer.framebufferClear(false);
            framebuffer.bindFramebuffer(false);
            gaussianBlur.load();
            setupUniforms(compression, 0, radius);

            GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
            ShaderManager.drawQuads();
            framebuffer.unbindFramebuffer();
            gaussianBlur.unload();

            mc.getFramebuffer().bindFramebuffer(false);
            gaussianBlur.load();
            gaussianBlur.setUniformf("direction", 0, compression);

            GlStateManager.bindTexture(framebuffer.framebufferTexture);
            ShaderManager.drawQuads();
            gaussianBlur.unload();

            StencilUtil.disable();
            GlStateManager.color4f(-1, -1, 1, -1);
            GlStateManager.bindTexture(0);
        }
    }

    public static void blur(float radius, float compression) {
        framebuffer = createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.load();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderManager.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.unload();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.load();
        setupUniforms(0, compression, radius);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderManager.drawQuads();
        gaussianBlur.unload();

        GlStateManager.color4f(-1, -1, 1, -1);
        GlStateManager.bindTexture(0);

    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getWidth() || framebuffer.framebufferHeight != mc.getMainWindow().getHeight()) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(Math.max(mc.getMainWindow().getWidth(), 1), Math.max(mc.getMainWindow().getHeight(), 1), false, false);
        }
        return framebuffer;
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }


}