// RenderUtilRing.java (MCP 1.16.5)
package dev.wh1tew1ndows.client.utils.render.draw;


import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public final class RenderUtilRing {

    /**
     * Кольцо с мягко «пропадающим» сегментом (gap).
     */
    public static void drawFadingRing(MatrixStack ms,
                                      float cx, float cy,
                                      float radius,
                                      float thickness,
                                      float gapCenter,
                                      float gapSize,
                                      int c1, int c2,
                                      int baseColor) {

        if (baseColor != -1) {
            drawRing(ms, cx, cy, radius, thickness, 0f, 1f, baseColor, baseColor, 1f);
        }

        final int segments = Math.max(64, (int) (radius * 8));
        final float soft = gapSize * 0.35f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest(); // оверлей

        ms.push();
        Matrix4f m = ms.getLast().getMatrix();

        buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            float ang = (float) (t * Math.PI * 2.0);

            float aMask = 1.0f - smoothGap(t, gapCenter, gapSize, soft);

            int col = lerpColorARGB(c1, c2, t);
            float a = ((col >>> 24) & 0xFF) / 255f;
            float r = ((col >> 16) & 0xFF) / 255f;
            float g = ((col >> 8) & 0xFF) / 255f;
            float b = (col & 0xFF) / 255f;

            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);

            float outerX = cx + cos * radius;
            float outerY = cy + sin * radius;
            float innerX = cx + cos * (radius - thickness);
            float innerY = cy + sin * (radius - thickness);

            float finalA = a * aMask;

            buf.pos(m, outerX, outerY, 0).color(r, g, b, finalA).endVertex();
            buf.pos(m, innerX, innerY, 0).color(r, g, b, finalA).endVertex();
        }

        tess.draw();
        ms.pop();

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    // Базовый ринг (фон)
    private static void drawRing(MatrixStack ms, float cx, float cy, float radius, float thickness,
                                 float t0, float t1, int c1, int c2, float alphaMul) {
        int segments = Math.max(48, (int) (radius * 6));
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        ms.push();
        Matrix4f m = ms.getLast().getMatrix();

        buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float t = t0 + (t1 - t0) * (i / (float) segments);
            float ang = (float) (t * Math.PI * 2.0);

            int col = lerpColorARGB(c1, c2, (i / (float) segments));
            float a = ((col >>> 24) & 0xFF) / 255f * alphaMul;
            float r = ((col >> 16) & 0xFF) / 255f;
            float g = ((col >> 8) & 0xFF) / 255f;
            float b = (col & 0xFF) / 255f;

            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);

            float ox = cx + cos * radius;
            float oy = cy + sin * radius;
            float ix = cx + cos * (radius - thickness);
            float iy = cy + sin * (radius - thickness);

            buf.pos(m, ox, oy, 0).color(r, g, b, a).endVertex();
            buf.pos(m, ix, iy, 0).color(r, g, b, a).endVertex();
        }
        tess.draw();
        ms.pop();

        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    // -------- helpers --------
    private static float smoothGap(float t, float center, float size, float soft) {
        float d = wrap01(t - center);
        if (d > 0.5f) d -= 1f;
        if (d < -0.5f) d += 1f;
        d = Math.abs(d);
        float half = size * 0.5f;
        float edge0 = half - soft;
        float edge1 = half;
        return smoothstep(edge0, edge1, d);
    }

    private static float wrap01(float v) {
        v = v - (float) Math.floor(v);
        return v;
    }

    private static float smoothstep(float a, float b, float x) {
        float t = clamp01((x - a) / Math.max(1e-6f, (b - a)));
        return t * t * (3f - 2f * t);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int lerpColorARGB(int c1, int c2, float t) {
        t = clamp01(t);
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
