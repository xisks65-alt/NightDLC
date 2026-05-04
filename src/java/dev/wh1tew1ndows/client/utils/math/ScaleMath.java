package dev.wh1tew1ndows.client.utils.math;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2d;
import org.joml.Vector2f;

@UtilityClass
public class ScaleMath implements IMinecraft {
    private final int SCALE = 2;

    public void scalePre() {
        mc.gameRenderer.setupOverlayRendering(SCALE);
    }

    public Vector2f getMouse(double mouseX, double mouseY) {
        return new Vector2f((float) (mouseX * mc.getMainWindow().getScaleFactor() / SCALE), (float) (mouseY * mc.getMainWindow().getScaleFactor() / SCALE));
    }

    public Vector2d getMouse2(double mouseX, double mouseY) {
        return new Vector2d((float) (mouseX * mc.getMainWindow().getScaleFactor() / SCALE), (float) (mouseY * mc.getMainWindow().getScaleFactor() / SCALE));
    }

    public void scalePre(float scale) {
        mc.gameRenderer.setupOverlayRendering(scale);
    }

    public float getScaled(double value) {
        return (float) (value * mc.getMainWindow().getScaleFactor() / SCALE);
    }

    public Vector2f getMouse(double mouseX, double mouseY, float scale) {
        return new Vector2f((float) (mouseX * mc.getMainWindow().getScaleFactor() / scale), (float) (mouseY * mc.getMainWindow().getScaleFactor() / scale));
    }

    public float getScaled(double value, float scale) {
        return (float) (value * mc.getMainWindow().getScaleFactor() / scale);
    }

    public void setupOverlayRendering(float scale) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        mw.setGuiScale(scale);
        RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, mw.getScaledWidth(), mw.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        RenderHelper.setupGui3DDiffuseLighting();
    }

    public void setupOverlayRendering() {
        int guiScale = mw.calcGuiScale(mc.gameSettings.guiScale, mc.getForceUnicodeFont());
        setupOverlayRendering(guiScale);
    }

    public void resetProjectionMatrix() {
        RenderSystem.translatef(0.0F, 0.0F, 2000.0F);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
    }

    public void scalePost() {
        mc.gameRenderer.setupOverlayRendering();
    }
}
