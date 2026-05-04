package dev.wh1tew1ndows.client.utils.keyboard;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;


public class ScaleHelper implements IMinecraft {

    public static float size = 2;

    public static void scale_pre() {
        final ScaledResolution scaledRes = new ScaledResolution(Minecraft.getInstance());
        final double scale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2);
        GL11.glPushMatrix();
        GL11.glScaled(scale * size, scale * size, scale * size);
    }

    public static void scale_post() {
        GL11.glScaled(size, size, size);
        GL11.glPopMatrix();
    }

    public static void scaleStart(float x, float y, float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }

    public static void scaleEnd() {
        RenderSystem.popMatrix();
    }

    public static int calc(int value) {
        ScaledResolution rs = new ScaledResolution(Minecraft.getInstance());
        return (int) (value * rs.getScaleFactor() / size);
    }

    public static float calcFloat(float value) {
        ScaledResolution rs = new ScaledResolution(Minecraft.getInstance());
        return value * rs.getScaleFactor() / size;
    }

    public static int calc(float value) {
        ScaledResolution rs = new ScaledResolution(Minecraft.getInstance());
        return (int) (value * rs.getScaleFactor() / size);
    }

    public static float[] calc(float mouseX, float mouseY) {
        ScaledResolution rs = new ScaledResolution(Minecraft.getInstance());
        mouseX = mouseX * rs.getScaleFactor() / size;
        mouseY = mouseY * rs.getScaleFactor() / size;
        return new float[]{mouseX, mouseY};
    }

    public static void scaleNonMatrix(float x, float y, float scale) {
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }

}
