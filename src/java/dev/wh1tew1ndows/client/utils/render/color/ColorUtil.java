package dev.wh1tew1ndows.client.utils.render.color;

import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.*;

import static net.minecraft.util.ColorHelper.PackedColor.*;

@SuppressWarnings("unused")
@UtilityClass
public class ColorUtil {
    private final FastRandom RANDOM = new FastRandom();
    private final long CACHE_EXPIRATION_TIME = 60 * 1000;
    private final ConcurrentHashMap<ColorKey, CacheEntry> colorCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private final DelayQueue<CacheEntry> cleanupQueue = new DelayQueue<>();


    public int applyOpacity(int color, float opacity) {
        return ColorHelper.PackedColor.packColor((int) (getAlpha(color) * opacity / 255), getRed(color), getGreen(color), getBlue(color));
    }

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public static void setAlphaColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        int red1 = getRed(color1);
        int green1 = getGreen(color1);
        int blue1 = getBlue(color1);
        int alpha1 = getAlpha(color1);

        int red2 = getRed(color2);
        int green2 = getGreen(color2);
        int blue2 = getBlue(color2);
        int alpha2 = getAlpha(color2);

        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolateD(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static void setColor(int color) {
        setAlphaColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public int darker(int color, int factor) {
        int red = getRed(color);
        int green = getGreen(color);
        int blue = getBlue(color);

        red = Math.max(0, red - factor);
        green = Math.max(0, green - factor);
        blue = Math.max(0, blue - factor);

        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int glColor(int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
        return color;
    }

    public static int skyRainbow(int speed, int index) {
        double angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor(
                ((angle %= 360) / 360.0) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0),
                0.5F,
                1.0F
        ).hashCode();
    }

    public static int fadeBetween(float speed, int offset, int color1, int color2) {
        long time = System.currentTimeMillis() + offset;
        double factor = (Math.sin(time * 0.001 * speed) + 1) / 2.0; // колебание от 0 до 1

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (r << 16) | (g << 8) | b;
    }

    public static int fadeBetween(int from, int to, float fraction) {
        fraction = clamp01(fraction);

        int a1 = (from >> 24) & 0xFF;
        int r1 = (from >> 16) & 0xFF;
        int g1 = (from >> 8) & 0xFF;
        int b1 = from & 0xFF;

        int a2 = (to >> 24) & 0xFF;
        int r2 = (to >> 16) & 0xFF;
        int g2 = (to >> 8) & 0xFF;
        int b2 = to & 0xFF;

        int a = (int) (a1 + (a2 - a1) * fraction);
        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);

        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

    private static float clamp01(float v) {
        if (v < 0F) return 0F;
        if (v > 1F) return 1F;
        return v;
    }

    static {
        cacheCleaner.scheduleWithFixedDelay(() -> {
            CacheEntry entry = cleanupQueue.poll();
            while (entry != null) {
                if (entry.isExpired()) {
                    colorCache.remove(entry.getKey());
                }
                entry = cleanupQueue.poll();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static float[] rgba(final int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int[] rgbas(final int color) {
        return new int[]{
                (int) ((color >> 16 & 0xFF) / 255f),
                (int) ((color >> 8 & 0xFF) / 255f),
                (int) ((color & 0xFF) / 255f),
                (int) ((color >> 24 & 0xFF) / 255f)
        };
    }

    public final int RED = getColor(255, 0, 0);
    public final int GREEN = getColor(0, 255, 0);
    public final int BLUE = getColor(0, 0, 255);
    public final int YELLOW = getColor(255, 255, 0);
    public final int WHITE = getColor(255);
    public final int BLACK = getColor(0);

    public int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public int blue(int c) {
        return c & 0xFF;
    }

    public int alpha(int c) {
        return (c >> 24) & 0xFF;
    }

    public float redf(int c) {
        return red(c) / 255.0f;
    }

    public float greenf(int c) {
        return green(c) / 255.0f;
    }

    public float bluef(int c) {
        return blue(c) / 255.0f;
    }

    public float alphaf(int c) {
        return alpha(c) / 255.0f;
    }

    public int[] getRGBA(int c) {
        return new int[]{red(c), green(c), blue(c), alpha(c)};
    }

    public int[] getRGB(int c) {
        return new int[]{red(c), green(c), blue(c)};
    }

    public float[] getRGBAf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
    }

    public float[] getRGBf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c)};
    }

    public int getColor(float red, float green, float blue, float alpha) {
        return getColor(Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255), Math.round(alpha * 255));
    }

    public int getColor(int red, int green, int blue, float alpha) {
        return getColor(red, green, blue, Math.round(alpha * 255));
    }

    public int getColor(float red, float green, float blue) {
        return getColor(red, green, blue, 1.0F);
    }

    public int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public int getColor(int brightness, float alpha) {
        return getColor(brightness, Math.round(alpha * 255));
    }

    public int getColor(int brightness) {
        return getColor(brightness, brightness, brightness);
    }

    public int replAlpha(int color, int alpha) {
        return getColor(red(color), green(color), blue(color), alpha);
    }

    public int replAlpha(int color, float alpha) {
        return getColor(red(color), green(color), blue(color), alpha);
    }

    public int multAlpha(int color, float percent01) {
        return getColor(red(color), green(color), blue(color), Math.round(alpha(color) * percent01));
    }

    public int toGray(int color, float percent01) {
        int r = red(color);
        int g = green(color);
        int b = blue(color);
        int a = alpha(color);

        // целевой серый
        int target = 128;

        // интерполяция к серому
        r = Math.round(r + (target - r) * percent01);
        g = Math.round(g + (target - g) * percent01);
        b = Math.round(b + (target - b) * percent01);

        // чуть затемняем (например на 10%)
        float darkFactor = (percent01 / 2);
        r = Math.round(r * darkFactor);
        g = Math.round(g * darkFactor);
        b = Math.round(b * darkFactor);

        return getColor(r, g, b, a);
    }


    public int multDark(int color, float percent01) {
        return getColor(
                Math.round(red(color) * percent01),
                Math.round(green(color) * percent01),
                Math.round(blue(color) * percent01),
                alpha(color)
        );
    }

    public int multBright(int color, float percent01) {
        return getColor(
                Math.min(255, Math.round(red(color) / percent01)),
                Math.min(255, Math.round(green(color) / percent01)),
                Math.min(255, Math.round(blue(color) / percent01)),
                alpha(color)
        );
    }

    public int overCol(int color1, int color2, float percent01) {
        final float percent = MathHelper.clamp(percent01, 0F, 1F);
        return getColor(
                Interpolator.lerp(red(color1), red(color2), percent),
                Interpolator.lerp(green(color1), green(color2), percent),
                Interpolator.lerp(blue(color1), blue(color2), percent),
                Interpolator.lerp(alpha(color1), alpha(color2), percent)
        );
    }

    public int overCol(int color1, int color2) {
        return overCol(color1, color2, 0.5f);
    }

    public int[] genGradientForText(int color1, int color2, int length) {
        int[] gradient = new int[length];
        for (int i = 0; i < length; i++) {
            float pc = (float) i / (length - 1);
            gradient[i] = overCol(color1, color2, pc);
        }
        return gradient;
    }


    public static int interpolate(int color1, int color2, double amount) {
        amount = (float) MathHelper.clamp(amount, 0, 1);
        return getColor(
                Interpolator.lerp(red(color1), red(color2), amount),
                Interpolator.lerp(green(color1), green(color2), amount),
                Interpolator.lerp(blue(color1), blue(color2), amount),
                Interpolator.lerp(alpha(color1), alpha(color2), amount)
        );
    }

    public int randomColor() {
        return Color.HSBtoRGB(RANDOM.nextFloat(), 0.5F + (RANDOM.nextFloat() / 2F), 1F);
    }

    public int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return getColor(red(color), green(color), blue(color), Math.round(opacity * 255));
    }


    public int fade(int speed, int index, int first, int second) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = angle >= 180 ? 360 - angle : angle;
        return overCol(first, second, angle / 180f);
    }

    public int fade(int index) {
        InterFace interFace = InterFace.getInstance();
        return fade(interFace.getSpeed(), index, interFace.themeColor(index), multDark(interFace.themeColor(index), 0.5F));
    }

    public int fadeCo(int index) {
        InterFace interFace = InterFace.getInstance();
        return fade(interFace.getSpeed(), index, multDark(interFace.themeColor(index), 0.8F), multDark(interFace.themeColor(index), 0.35F));
    }

    public int fade(int index, float mudak) {
        InterFace interFace = InterFace.getInstance();
        return fade(interFace.getSpeed(), index, interFace.themeColor(index), multDark(interFace.themeColor(index), mudak));
    }

    public int fade(int index, float mudak,float svet) {
        InterFace interFace = InterFace.getInstance();
        return fade(interFace.getSpeed(), index,multBright(interFace.themeColor(index),svet), multDark(interFace.themeColor(index), mudak));
    }


    public int fade() {
        InterFace interFace = InterFace.getInstance();
        return interFace.themeColor();
    }


    public int fade(int index, int speed) {
        InterFace interFace = InterFace.getInstance();
        return fade(speed, index, interFace.themeColor(index), multDark(interFace.themeColor(index * 2), 0.6F));
    }

    public static int getColorGR(int index) {
        InterFace interFace = InterFace.getInstance();
        return ColorUtil.gradient(InterFace.getInstance().themeColor(index), multDark(interFace.themeColor(index * 2), 0.6F), index, 10);
    }

    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = interpolate(start, end, MathHelper.clamp(angle / 180f - 1, 0, 1));
        float[] hs = rgba(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public int fadeA(int index, float alpha) {
        InterFace interFace = InterFace.getInstance();
        return fade(interFace.getSpeed(), index, multAlpha(interFace.clientColor(), alpha), multAlpha(interFace.darkColor(), alpha));
    }

    public int getColor(int red, int green, int blue, int alpha) {
        ColorKey key = new ColorKey(red, green, blue, alpha);
        CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
            CacheEntry newEntry = new CacheEntry(k, computeColor(red, green, blue, alpha), CACHE_EXPIRATION_TIME);
            cleanupQueue.offer(newEntry);
            return newEntry;
        });
        return cacheEntry.getColor();
    }

    public int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    private int computeColor(int red, int green, int blue, int alpha) {
        return ((MathHelper.clamp(alpha, 0, 255) << 24) |
                (MathHelper.clamp(red, 0, 255) << 16) |
                (MathHelper.clamp(green, 0, 255) << 8) |
                MathHelper.clamp(blue, 0, 255));
    }

    private String generateKey(int red, int green, int blue, int alpha) {
        return red + "," + green + "," + blue + "," + alpha;
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class ColorKey {
        final int red, green, blue, alpha;
    }

    @Getter
    private static class CacheEntry implements Delayed {
        private final ColorKey key;
        private final int color;
        private final long expirationTime;

        CacheEntry(ColorKey key, int color, long ttl) {
            this.key = key;
            this.color = color;
            this.expirationTime = System.currentTimeMillis() + ttl;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = expirationTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof CacheEntry) {
                return Long.compare(this.expirationTime, ((CacheEntry) other).expirationTime);
            }
            return 0;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

    }

    public void shutdownCacheCleaner() {
        cacheCleaner.shutdown();
    }
}