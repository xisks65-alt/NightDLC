package dev.wh1tew1ndows.client.utils.render.shader.impl;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.StencilUtil;
import dev.wh1tew1ndows.client.utils.render.framebuffer.CustomFramebuffer;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import dev.wh1tew1ndows.client.utils.render.shader.glsl.KawaseBlurDown;
import dev.wh1tew1ndows.client.utils.render.shader.glsl.KawaseBlurUp;
import lombok.Getter;
import net.minecraft.client.shader.Framebuffer;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public enum BlurShader implements IMinecraft {
    INSTANCE;

    @Getter
    private final CustomFramebuffer buffer = new CustomFramebuffer(false).setLinear();
    @Getter
    private final CustomFramebuffer cache = new CustomFramebuffer(false).setLinear();

    private final ShaderManager kawaseUp = ShaderManager.create(new KawaseBlurUp());
    private final ShaderManager kawaseDown = ShaderManager.create(new KawaseBlurDown());
    private final FrameLimiter frameLimiter = new FrameLimiter(true);

    public void render(Runnable run) {

        StencilUtil.enable();
        run.run();
        StencilUtil.read(1);
        GlStateManager.bindTexture(buffer.framebufferTexture);
        CustomFramebuffer.drawTexture();
        StencilUtil.disable();
    }

    public void updateBlur(float offset, int steps) {
        Framebuffer framebuffer = mc.getFramebuffer();

        boolean blur = true;
        if (!blur) {
            buffer.setFramebufferColor(0f, 0f, 0f, 1f);
            buffer.setup();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0f);
            framebuffer.bindFramebufferTexture();
            CustomFramebuffer.flipQuads();
            framebuffer.unbindFramebufferTexture();
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            framebuffer.bindFramebuffer(true);
            buffer.stop();

            GL13.glActiveTexture(GL20.GL_TEXTURE5);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.framebufferTexture);
            GL13.glActiveTexture(GL20.GL_TEXTURE0);
            return;
        }

        float saturation = 1;
        float tintIntensity = 0f;
        float[] tintColor = ColorUtil.getRGBf(-1);
        RenderUtil.start();

        frameLimiter.execute(70, () -> {
            cache.setup();
            framebuffer.bindFramebufferTexture();

            setupBlur(offset, saturation, tintIntensity, tintColor, kawaseDown);
            CustomFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
            cache.stop();
            CustomFramebuffer[] buffers = {this.cache, this.buffer};

            for (int i = 1; i < steps; ++i) {
                int step = i % 2;
                buffers[step].setup();
                buffers[(step + 1) % 2].bindFramebufferTexture();
                CustomFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
                buffers[(step + 1) % 2].unbindFramebufferTexture();
                buffers[step].stop();
            }

            setupBlur(offset, saturation, tintIntensity, tintColor, kawaseUp);

            for (int i = 0; i < steps; ++i) {
                int step = i % 2;
                buffers[(step + 1) % 2].setup();
                buffers[step].bindFramebufferTexture();
                CustomFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
                buffers[step].unbindFramebuffer();
                buffers[step].stop();
            }
            framebuffer.unbindFramebufferTexture();

            kawaseUp.unload();
            kawaseDown.unload();
        });
        GL13.glActiveTexture(GL20.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.framebufferTexture);
        GL13.glActiveTexture(GL20.GL_TEXTURE0);
        framebuffer.bindFramebuffer(true);

        RenderUtil.stop();
    }

    private void setupBlur(float offset, float saturation, float tintIntensity, float[] tintColor, ShaderManager shader) {
        shader.load();
        shader.setUniformi("image", 0);
        shader.setUniformf("offset", offset);
        shader.setUniformf("resolution", 1f / buffer.framebufferWidth, 1f / buffer.framebufferHeight);
        shader.setUniformf("saturation", saturation);
        shader.setUniformf("tintIntensity", tintIntensity);
        shader.setUniformf("tintColor", tintColor);
    }
}
