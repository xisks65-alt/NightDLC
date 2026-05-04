package dev.wh1tew1ndows.client.utils.render.draw;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.framebuffer.CustomFramebuffer;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import dev.wh1tew1ndows.client.utils.render.shader.impl.BlurShader;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2d;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.HashMap;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX;
import static net.mojang.blaze3d.systems.RenderSystem.disableBlend;
import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderUtil implements IRender, IMinecraft {

    public static void drawItemStack(MatrixStack var0, ItemStack var1, int var2, int var3, int var4) {
        if (!var1.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
            float[] var5 = ColorUtil.getRGBAf(var4);
            RenderSystem.color4f(var5[0], var5[1], var5[2], var5[3]);
            mc.getItemRenderer().renderItemAndEffectIntoGUI(var1, var2, var3);
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, var1, var2, var3);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }

    public static void setup3dForBlockPos(Runnable render, boolean bloom) {
        double glX = mc.getRenderManager().info.getProjectedView().x;
        double glY = mc.getRenderManager().info.getProjectedView().y;
        double glZ = mc.getRenderManager().info.getProjectedView().z;
        GL11.glPushMatrix();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        LightTexture.disableLightmap();
        GL11.glEnable(3042);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glShadeModel(7425);
        GL11.glTranslated(-glX, -glY, -glZ);
        render.run();
        GL11.glTranslated(glX, glY, glZ);
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.resetColor();
        GL11.glPopMatrix();
    }


    public static void drawMainMenuShader(float width, float height, int mouseX, int mouseY, float al) {


    }

    public void clientStyledRect(MatrixStack matrix, float x, float y, float width, float height, float alpha) {
        clientStyledRect(matrix, x, y, width, height, alpha, 5);
    }


    public void clientStyledRect(MatrixStack matrix, float x, float y, float width, float height, float alpha, float roundOf) {

        Round round = Round.of(roundOf);

        RenderUtil.Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.4F), Round.of(roundOf));

        Rounded.roundedOutline(matrix, x, y, width, height, 1, ColorUtil.replAlpha(ColorUtil.getColor(12), alpha * 1), round.sub(0.5F));
        // RenderUtil.Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(ColorUtil.multBright(InterFace.getInstance().themeColor(), 1), alpha * 0.05F), Round.of(roundOf));


        // Rounded.roundedOutline(matrix, x, y, width, height, 1, ColorUtil.replAlpha(ColorUtil.multDark(InterFace.getInstance().themeColor(), 1), alpha * 0.3F), Round.of(roundOf).sub(0.5F));
    }

    public void clientStyledRectDark(MatrixStack matrix, float x, float y, float width, float height, float alpha, float roundOf) {


        if (InterFace.getInstance().blur.getValue()) {

            RenderUtil.Shadow.drawShadow(matrix, x - roundOf / 2, y - roundOf / 2, width + roundOf, height + roundOf, 12, ColorUtil.replAlpha(ColorUtil.getColor(0), (float) Math.pow(alpha, 3)));

            RenderUtil.bindTexture(BlurShader.INSTANCE.getBuffer().framebufferTexture);
            RenderUtil.Texture.customRound(matrix, RenderUtil.Texture.ShaderType.BLUR, x, y, width, height, alpha, 0, 0, 0, 0, Round.of(roundOf));
        }

        RenderUtil.Rounded.smooth(matrix, x, y, width + 1, height, ColorUtil.getColor(15, alpha * 0.7F), Round.of(roundOf));

    }


    public void clientStyledRectNoteM(MatrixStack matrix, float x, float y, float width, float height, float alpha, float roundOf) {

        Round round = Round.of(roundOf);


        RenderUtil.Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.4F), Round.of(roundOf));

        Rounded.roundedOutline(matrix, x, y, width, height, 1, ColorUtil.replAlpha(ColorUtil.getColor(12), alpha * 1), round.sub(0.5F));
    }


    public void clientStyledRect(MatrixStack matrix, float x, float y, float width, float height, float alpha, Round roundOf) {


        // RenderUtil.Shadow.drawShadow(matrix, x, y, width, height, 24, ColorUtil.getColor(35, alpha * 0.8F));
        Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(new Color(0x121212).getRGB(), alpha * 0.9F), Round.of(roundOf));

    }


    public static class Images {
        public static void drawImage(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4, boolean bloom) {
            Minecraft minecraft = Minecraft.getInstance();
            GlStateManager.enableBlend();
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0);

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            minecraft.getTextureManager().bindTexture(image);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
            bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
            bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
            bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();
            tessellator.draw();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            disableBlend();
        }
    }


    public static class Colors {

        public static Color TwoColoreffect(final Color color, final Color color2, final double n) {
            final float clamp = MathHelper.clamp((float) Math.sin(18.84955592153876 * (n / 4.0 % 1.0)) / 2.0f + 0.5f, 0.0f, 1.0f);
            return new Color(MathHelper.lerp(color.getRed() / 255.0f, color2.getRed() / 255.0f, clamp), MathHelper.lerp(color.getGreen() / 255.0f, color2.getGreen() / 255.0f, clamp), MathHelper.lerp(color.getBlue() / 255.0f, color2.getBlue() / 255.0f, clamp), MathHelper.lerp(color.getAlpha() / 255.0f, color2.getAlpha() / 255.0f, clamp));
        }

        public static void setupColor(int color, float alpha) {
            float f = (float) (color >> 16 & 0xFF) / 255.0f;
            float f1 = (float) (color >> 8 & 0xFF) / 255.0f;
            float f2 = (float) (color & 0xFF) / 255.0f;
            GL11.glColor4f(f, f1, f2, alpha / 255.0f);
        }

        public Color[] genGradientForText(Color color1, Color color2, int length) {
            Color[] gradient = new Color[length];
            for (int i = 0; i < length; i++) {
                double pc = (double) i / (length - 1);
                gradient[i] = interpolate(color1, color2, pc);
            }
            return gradient;
        }

        public static int[] genGradientForText(int color1, int color2, int length) {
            int[] gradient = new int[length];
            for (int i = 0; i < length; i++) {
                double pc = (double) i / (length - 1);
                gradient[i] = interpolate(color1, color2, pc);
            }
            return gradient;
        }
        public static int getRedInt(int n) {
            return n >> 16 & 0xFF;
        }

        public static int getGreenInt(int n) {
            return n >> 8 & 0xFF;
        }

        public static int getBlueInt(int n) {
            return n & 0xFF;
        }

        public static int getAlphaInt(int n) {
            return n >> 24 & 0xFF;
        }


        public Color interpolate(Color color1, Color color2, double amount) {
            amount = 1F - amount;
            amount = (float) MathHelper.clamp(amount, 0, 1);
            return new Color(
                    Interpolator.lerp(color1.getRed(), color2.getRed(), amount),
                    Interpolator.lerp(color1.getGreen(), color2.getGreen(), amount),
                    Interpolator.lerp(color1.getBlue(), color2.getBlue(), amount),
                    Interpolator.lerp(color1.getAlpha(), color2.getAlpha(), amount)
            );
        }

        public static int interpolate(int color1, int color2, double amount) {
            amount = (float) MathHelper.clamp(amount, 0, 1);
            return getColor(
                    Interpolator.lerp(ColorUtil.red(color1), ColorUtil.red(color2), amount),
                    Interpolator.lerp(ColorUtil.green(color1), ColorUtil.green(color2), amount),
                    Interpolator.lerp(ColorUtil.blue(color1), ColorUtil.blue(color2), amount),
                    Interpolator.lerp(alpha(color1), alpha(color2), amount)
            );
        }

        public static int alpha(int c) {
            return c >> 24 & 0xFF;
        }

        public static int glColor(int color) {
            float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
            float red = (float) (color >> 16 & 0xFF) / 255.0f;
            float green = (float) (color >> 8 & 0xFF) / 255.0f;
            float blue = (float) (color & 0xFF) / 255.0f;
            GL11.glColor4f(red, green, blue, alpha);
            return color;
        }

        public static int swapAlpha(int color, float alpha) {
            int f = color >> 16 & 0xFF;
            int f1 = color >> 8 & 0xFF;
            int f2 = color & 0xFF;
            return getColor(f, f1, f2, (int) alpha);
        }

        public static int getColor(int red, int green, int blue, int alpha) {
            int color = 0;
            color |= alpha << 24;
            color |= red << 16;
            color |= green << 8;
            return color |= blue;
        }

        public static float[] rgb(final int color) {
            return new float[]{(color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f};
        }

        public static float[] rgba(final int color) {
            return new float[]{
                    (color >> 16 & 0xFF) / 255f,
                    (color >> 8 & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    (color >> 24 & 0xFF) / 255f
            };
        }

        public static int getAlphaFromColor(int color) {
            return color >> 24 & 0xFF;
        }
    }

    public static class GLU {
        public static void startBlend() {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
            return framebuffer == null || framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight();
        }

        public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
            if (!Minecraft.isScreenMinimized()) {
                if (framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getWidth() || framebuffer.framebufferHeight != mc.getMainWindow().getHeight()) {
                    if (framebuffer != null) {
                        framebuffer.deleteFramebuffer();
                    }
                    return new Framebuffer(mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), true, true);
                }
                return framebuffer;
            }
            return null;
        }

        public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
            if (needsNewFramebuffer(framebuffer)) {
                if (framebuffer != null) {
                    framebuffer.deleteFramebuffer();
                }
                return new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), depth, false);
            }
            return framebuffer;
        }

        public static void endBlend() {
            GlStateManager.disableBlend();
        }

        private static final boolean openGL14 = (mc.player != null);
        public static boolean extBlendFuncSeparate = (mc.player != null);

        public static void glBlendFunc(int sFactorRGB, int dFactorRGB, int sfactorAlpha, int dfactorAlpha) {
            if (openGL14) {
                if (extBlendFuncSeparate) {
                    EXTBlendFuncSeparate.glBlendFuncSeparateEXT(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
                } else {
                    GL14.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
                }
            } else {
                GL11.glBlendFunc(sFactorRGB, dFactorRGB);
            }
        }
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }


    public static void rotate(float posX, float posY, float width, float height, float angleDegrees, Runnable runnable) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(posX + width / 2.0F, posY + height / 2.0F, 0.0D);
        GlStateManager.rotatef(angleDegrees, 0.0F, 0.0F, 1.0F);
        GlStateManager.translated(-posX - width / 2.0F, -posY - height / 2.0F, 0.0D);
        runnable.run();
        GlStateManager.popMatrix();
    }


    public static void drawHead(ResourceLocation skin, float x, float y, float width, float height, float radius, float alpha, float hurtPercent) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        RenderUtil.bindTexture(skin);
        ShaderManager.head.load();
        ShaderManager.head.setUniformf("size", width, height);
        ShaderManager.head.setUniformf("radius", radius);
        ShaderManager.head.setUniformf("hurt_time", hurtPercent);
        ShaderManager.head.setUniformf("alpha", alpha);
        ShaderManager.head.setUniformf("startX", 4.0F);
        ShaderManager.head.setUniformf("startY", 4.0F);
        ShaderManager.head.setUniformf("endX", 8.0F);
        ShaderManager.head.setUniformf("endY", 8.0F);
        ShaderManager.head.setUniformf("texXSize", 32.0F);
        ShaderManager.head.setUniformf("texYSize", 32.0F);
        ShaderManager.drawQuads(x + 2.0F, y + 2.0F, width, height);
        ShaderManager.head.unload();
        disableBlend();
        GlStateManager.popMatrix();
    }

    public void start() {
        RenderSystem.clearCurrentColor();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.shadeModel(7425);
        defaultAlphaFunc();
    }

    public void stop() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        disableBlend();
        RenderSystem.clearCurrentColor();
        RenderSystem.shadeModel(7424);
    }

    public void bindTexture(int texture) {
        RenderSystem.bindTexture(texture);
    }

    public void bindTexture(ResourceLocation texture) {
        mc.getTextureManager().bindTexture(texture);
    }

    public void resetColor() {
        RenderSystem.clearCurrentColor();
    }

    public void setColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public void setColor(int color) {
        setColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public void defaultAlphaFunc() {
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
    }


    public void drawImage(ResourceLocation location, MatrixStack matrix, float x, float y, float width, float height, int color) {
        drawImage(location, matrix, x, y, width, height, color, color, color, color);
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

    public void drawImage(ResourceLocation location, MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
        RenderSystem.pushMatrix();
        RenderSystem.clearCurrentColor();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bindTexture(location);
        RectUtil.drawRect(matrix, x, y, width, height, color1, color2, color3, color4, false, true);
        RenderSystem.shadeModel(7424);
        disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.clearCurrentColor();
        RenderSystem.popMatrix();
    }


    public void enableSmoothLine() {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1F);
    }

    public void disableSmoothLine() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    @UtilityClass
    public class Rounded {

        public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            final ShaderManager shader = ShaderManager.roundedShader;

            shader.load();
            shader.setUniformf("size", width * 2, height * 2);
            shader.setUniformf("round", radius * 2, radius * 2, radius * 2, radius * 2);
            shader.setUniformf("smoothness", -0.5F, 0.5F);
            shader.setUniformf("color1", ColorUtil.rgba(color));
            shader.setUniformf("color2", ColorUtil.rgba(color));
            shader.setUniformf("color3", ColorUtil.rgba(color));
            shader.setUniformf("color4", ColorUtil.rgba(color));
            drawQuads(x, y, width, height, 7);

            shader.unload();
            disableBlend();

            GlStateManager.popMatrix();
        }

        public static void drawRectVerticalW(double x, double y, double w, double h, int color) {
            w = x + w;
            h = y + h;

            if (x < w) {
                double i = x;
                x = w;
                w = i;
            }

            if (y < h) {
                double j = y;
                y = h;
                h = j;
            }

            float[] colorOne = ColorUtil.rgba(color);
            float[] colorTwo = ColorUtil.rgba(color);
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, h, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(w, y, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
            bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
        }

        public static void drawRoundedRect(MatrixStack matrix, float x, float y, float width, float height, float radius, int color) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            final ShaderManager shader = ShaderManager.roundedShader;

            shader.load();
            shader.setUniformf("size", width * 2, height * 2);
            shader.setUniformf("round", radius * 2, radius * 2, radius * 2, radius * 2);
            shader.setUniformf("smoothness", -0.5F, 0.5F);
            shader.setUniformf("color1", ColorUtil.rgba(color));
            shader.setUniformf("color2", ColorUtil.rgba(color));
            shader.setUniformf("color3", ColorUtil.rgba(color));
            shader.setUniformf("color4", ColorUtil.rgba(color));
            // drawQuads(x, y, width, height, 7);

            drawQuads(matrix, x, y, width, height, 7);

            shader.unload();
            disableBlend();

            GlStateManager.popMatrix();
        }

        public static void drawQuads(MatrixStack matrixStack, float x, float y, float width, float height, int glQuads) {
            BUFFER.begin(glQuads, POSITION_TEX);
            {
                Matrix4f matrix4f = matrixStack.getLast().getMatrix();
                BUFFER.pos(matrix4f, x, y, 0).tex(0, 0).endVertex();
                BUFFER.pos(matrix4f, x, y + height, 0).tex(0, 1).endVertex();
                BUFFER.pos(matrix4f, x + width, y + height, 0).tex(1, 1).endVertex();
                BUFFER.pos(matrix4f, x + width, y, 0).tex(1, 0).endVertex();
            }
            Tessellator.getInstance().draw();
        }

        public static void drawQuads(float x, float y, float width, float height, int glQuads) {
            BUFFER.begin(glQuads, POSITION_TEX);
            {
                BUFFER.pos(x, y, 0).tex(0, 0).endVertex();
                BUFFER.pos(x, y + height, 0).tex(0, 1).endVertex();
                BUFFER.pos(x + width, y + height, 0).tex(1, 1).endVertex();
                BUFFER.pos(x + width, y, 0).tex(1, 0).endVertex();
            }
            Tessellator.getInstance().draw();
        }

        public void customRound(MatrixStack matrix, float x, float y, float width, float height, boolean shadow, float shadowAlpha, float offset, float value, float smoothness1, float smoothness2, int color1, int color2, int color3, int color4, int outlineColor, Round round) {
            if (width <= 0 || height <= 0) {
                return;
            }
            final ShaderManager shader = ShaderManager.roundedShader;
            shader.load();

            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.RT, round.RB, round.LT, round.LB);
            shader.setUniformf("smoothness", smoothness1, smoothness2);
            shader.setUniformf("value", value);
            shader.setUniformi("shadow", shadow ? 1 : 0);
            shader.setUniformf("shadowAlpha", shadow ? shadowAlpha : 0);
            shader.setUniformf("color1", ColorUtil.getRGBAf(color1));
            shader.setUniformf("color2", ColorUtil.getRGBAf(color2));
            shader.setUniformf("color3", ColorUtil.getRGBAf(color3));
            shader.setUniformf("color4", ColorUtil.getRGBAf(color4));

            start();
            resetColor();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            CustomFramebuffer.drawQuads(matrix, x - offset, y - offset, width + (offset * 2), height + (offset * 2));
            stop();
            shader.unload();
        }


        public void roundedOutline(MatrixStack matrix, float x, float y, float width, float height, float outline, int color1, int color2, int color3, int color4, Round round) {
            if (width <= 0 || height <= 0) {
                return;
            }
            final ShaderManager shader = ShaderManager.roundedOutlineShader;
            shader.load();

            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.RT, round.RB, round.LT, round.LB);
            shader.setUniformf("smoothness", -outline, outline);
            shader.setUniformf("softness", -outline, outline);
            shader.setUniformf("thickness", 0, outline);
            shader.setUniformf("value", outline);
            shader.setUniformf("color1", ColorUtil.getRGBAf(color1));
            shader.setUniformf("color2", ColorUtil.getRGBAf(color2));
            shader.setUniformf("color3", ColorUtil.getRGBAf(color3));
            shader.setUniformf("color4", ColorUtil.getRGBAf(color4));

            start();
            resetColor();
            CustomFramebuffer.drawQuads(matrix, x - outline, y - outline, width + (outline * 2), height + (outline * 2));
            stop();
            shader.unload();
        }

        public void roundedOutline(MatrixStack matrix, float x, float y, float width, float height, float outline, int color, int color2, Round round) {
            roundedOutline(matrix, x, y, width, height, outline, color, color, color2, color2, round);
        }

        public void roundedOutline(MatrixStack matrix, float x, float y, float width, float height, float outline, int color, Round round) {
            roundedOutline(matrix, x, y, width, height, outline, color, color, color, color, round);
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, int color, int color2, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, -0.5F, 0.5F, color, color, color2, color2, 0, round);
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, int color3, int color4, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, -0.5F, 0.5F, color1, color2, color3, color4, 0, round);
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, int color, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, -0.5F, 0.5F, color, color, color, color, 0, round);
        }

    }

    private static final HashMap<Integer, Integer> shadowCache5 = new HashMap<Integer, Integer>();

    @UtilityClass
    public class Shadow {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();


        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color) {
            drawShadow(matrix, x, y, width, height, radius, 1f, color, color, color, color);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color1, int color2, int color3, int color4) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color1, int color2, int color3, int color4, Round round) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color, Round round) {
            drawShadow(matrix, x, y, width, height, radius, 1f, color, color, color, color, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color) {
            drawShadow(matrix, x, y, width, height, radius, alpha, color, color, color, color);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color1, int color2, int color3, int color4) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color1, int color2, int color3, int color4, Round round) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color, Round round) {
            drawShadow(matrix, x, y, width, height, radius, alpha, color, color, color, color, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color, int color2, Round round) {
            drawShadow(matrix, x, y, width, height, radius, alpha, color, color, color2, color2, round);
        }
    }


    public Vector2d project2D(double x, double y, double z) {
        if (mc.getRenderManager().info == null) return new Vector2d();
        net.minecraft.util.math.vector.Vector3d cameraPosition = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f relativePosition = new Vector3f((float) (cameraPosition.x - x), (float) (cameraPosition.y - y), (float) (cameraPosition.z - z));
        relativePosition.transform(cameraRotation);

        if (mc.gameSettings.viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerEntity) {
                float walkedDistance = playerEntity.distanceWalkedModified;

                float deltaDistance = walkedDistance - playerEntity.prevDistanceWalkedModified;
                float interpolatedDistance = -(walkedDistance + deltaDistance * mc.getRenderPartialTicks());
                float cameraYaw = MathHelper.lerp(mc.getRenderPartialTicks(), playerEntity.prevCameraYaw, playerEntity.cameraYaw);

                Quaternion bobQuaternionX = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI - 0.2F) * cameraYaw) * 5.0F, true);
                bobQuaternionX.conjugate();
                relativePosition.transform(bobQuaternionX);

                Quaternion bobQuaternionZ = new Quaternion(Vector3f.ZP, MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 3.0F, true);
                bobQuaternionZ.conjugate();
                relativePosition.transform(bobQuaternionZ);

                Vector3f bobTranslation = new Vector3f((MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 0.5F), (-Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI) * cameraYaw)), 0.0f);
                bobTranslation.setY(-bobTranslation.getY());
                relativePosition.add(bobTranslation);
            }
        }

        double fieldOfView = (float) mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        float halfHeight = (float) mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (relativePosition.getZ() * (float) Math.tan(Math.toRadians(fieldOfView / 2.0F)));

        if (relativePosition.getZ() < 0.0F) {
            return new Vector2d(-relativePosition.getX() * scaleFactor + (float) (mc.getMainWindow().getScaledWidth() / 2), (float) (mc.getMainWindow().getScaledHeight() / 2) - relativePosition.getY() * scaleFactor);
        }
        return null;
    }

    @UtilityClass
    public class Texture {
        public void start() {
            RenderSystem.clearCurrentColor();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableDepthTest();
            RenderSystem.shadeModel(7425);
            defaultAlphaFunc();
        }

        public void stop() {
            RenderSystem.enableDepthTest();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableCull();
            disableBlend();
            RenderSystem.clearCurrentColor();
            RenderSystem.shadeModel(7424);
        }

        public enum ShaderType {
            DEFAULT, SHADOW, TEXTURE, BLUR, OUTLINE
        }

        public void customRound(MatrixStack stack, ShaderType shaderType, float x, float y, float width, float height, float alpha,
                                int color1, int color2, int color3, int color4, Round round) {
            if (x + width <= 0 || y + height <= 0) {
                return;
            }
            if (x > mw.getScaledWidth() || y > mw.getScaledHeight()) {
                return;
            }
            if (width <= 0 || height <= 0) {
                return;
            }
            final ShaderManager shader = ShaderManager.roundoni;

            shader.load();

            shader.setUniformi("image", 0);
            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.LB, round.LT, round.RB, round.RT);
            shader.setUniformf("value", 0);
            shader.setUniformf("smoothness", -0.5F, 0.5F);
            shader.setUniformf("resolution", mw.getFramebufferWidth(), mw.getFramebufferHeight());

            shader.setUniformi("type", shaderType.ordinal());

            shader.setUniformf("alpha", alpha);
            shader.setUniformf("thickness", 0);

            shader.setUniformf("color1", ColorUtil.getRGBAf(color4));
            shader.setUniformf("color2", ColorUtil.getRGBAf(color3));
            shader.setUniformf("color3", ColorUtil.getRGBAf(color2));
            shader.setUniformf("color4", ColorUtil.getRGBAf(color1));

            RenderUtil.start();
            Matrix4f matrix4f = stack.getLast().getMatrix();

            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            BUFFER.pos(matrix4f, x, y, 0).tex(0, 0).endVertex();
            BUFFER.pos(matrix4f, x, y + height, 0).tex(0, 1).endVertex();
            BUFFER.pos(matrix4f, x + width, y + height, 0).tex(1, 1).endVertex();
            BUFFER.pos(matrix4f, x + width, y, 0).tex(1, 0).endVertex();
            TESSELLATOR.draw();
            RenderUtil.stop();

            shader.unload();
        }

        public void customRound(MatrixStack matrix, float x, float y, float width, float height, float value, float offset, float alpha, float smoothness1, float smoothness2, Round round) {
            final ShaderManager shader = ShaderManager.roundedTextureShader;
            shader.load();

            shader.setUniformi("textureIn", 0);
            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.LB, round.LT, round.RB, round.RT);
            shader.setUniformf("smoothness", smoothness1, smoothness2);
            shader.setUniformf("value", value);
            shader.setUniformf("alpha", alpha);

            start();

            Matrix4f matrix4f = matrix.getLast().getMatrix();

            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            BUFFER.pos(matrix4f, x, y, 0).tex(0, 0).endVertex();
            BUFFER.pos(matrix4f, x, y + height, 0).tex(0, 1).endVertex();
            BUFFER.pos(matrix4f, x + width, y + height, 0).tex(1, 1).endVertex();
            BUFFER.pos(matrix4f, x + width, y, 0).tex(1, 0).endVertex();
            TESSELLATOR.draw();
            stop();
            shader.unload();
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, float alpha, Round round) {
            customRound(matrix, x, y, width, height, 0, 0, alpha, -0.5F, 0.5F, round);
        }

    }
}
