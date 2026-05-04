package dev.wh1tew1ndows.client.utils.animation;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.platform.GlStateManager;


public class AnimationMath implements IMinecraft {
    // leaked by itskekoff; discord.gg/sk3d h22wVxPN
    public static double deltaTime() {
        return Minecraft.getDebugFPS() > 0 ? (1.0000 / Minecraft.getDebugFPS()) : 1;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathHelper.clamp((float) (AnimationMath.deltaTime() * multiple), 0, 1)) * end + MathHelper.clamp((float) (AnimationMath.deltaTime() * multiple), 0, 1) * start;
    }

    public static Vector3d interpolate(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                interpolate(end.getX(), start.getX(), multiple),
                interpolate(end.getY(), start.getY(), multiple),
                interpolate(end.getZ(), start.getZ(), multiple));
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * MathHelper.clamp(AnimationMath.deltaTime() * multiple, 0, 1));
    }

    public static double lerp(double end, double start, double multiple) {
        return (end + (start - end) * MathHelper.clamp(AnimationMath.deltaTime() * multiple, 0, 1));
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public static float smoothAnimation(float current, float target, float speed, float minStep) {
        float diff = target - current;
        if (Math.abs(diff) < minStep) {
            return target;
        }
        return current + diff * speed;
    }

    public static int lerpColor(int color1, int color2, float amount) {
        float inverseAmount = 1.0f - amount;

        int r = (int) ((color1 >> 16 & 0xFF) * inverseAmount + (color2 >> 16 & 0xFF) * amount);
        int g = (int) ((color1 >> 8 & 0xFF) * inverseAmount + (color2 >> 8 & 0xFF) * amount);
        int b = (int) ((color1 & 0xFF) * inverseAmount + (color2 & 0xFF) * amount);
        int a = (int) ((color1 >> 24 & 0xFF) * inverseAmount + (color2 >> 24 & 0xFF) * amount);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }


    public static void moveAnimation(double x, double y, double targetX, double targetY, double speed) {
        double newX = lerp(targetX, x, speed);
        double newY = lerp(targetY, y, speed);

        GlStateManager.translated(newX - x, newY - y, 0);
    }
}
