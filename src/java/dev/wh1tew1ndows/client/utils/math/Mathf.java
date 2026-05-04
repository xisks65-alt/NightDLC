package dev.wh1tew1ndows.client.utils.math;

import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mc;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

@UtilityClass
public class Mathf {

    public static float smoothRandom(float min, float max, float smoothness) {
        // static переменные для запоминания состояния между вызовами
        // (чтобы значения плавно перетекали, а не прыгали)
        if (lastSmoothRandom == null) {
            lastSmoothRandom = new float[]{(min + max) / 2F};
            lastSmoothTarget = new float[]{Mathf.randomValue(min, max)};
        }

        // если прошло достаточно времени — выбираем новую цель
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSmoothTime > 250 + Math.random() * 300) {
            lastSmoothTarget[0] = Mathf.randomValue(min, max);
            lastSmoothTime = currentTime;
        }

        // постепенно приближаемся к новой цели
        lastSmoothRandom[0] += (lastSmoothTarget[0] - lastSmoothRandom[0]) * smoothness;

        return lastSmoothRandom[0];
    }

    public float wrapAngleTo180(float angle) {
        angle %= 360.0F;
        if (angle >= 180.0F) angle -= 360.0F;
        if (angle < -180.0F) angle += 360.0F;
        return angle;
    }

    /**
     * Переводит значение из одного диапазона в другой (линейная интерполяция)
     *
     * @param value   входное значение
     * @param fromMin минимальное значение исходного диапазона
     * @param fromMax максимальное значение исходного диапазона
     * @param toMin   минимальное значение целевого диапазона
     * @param toMax   максимальное значение целевого диапазона
     * @return преобразованное значение
     */
    public static float map(float value, float fromMin, float fromMax, float toMin, float toMax) {
        if (fromMax - fromMin == 0) return toMin; // защита от деления на 0
        return toMin + (toMax - toMin) * ((value - fromMin) / (fromMax - fromMin));
    }

    // глобальные переменные (в твоём классе, не внутри метода)
    private static float[] lastSmoothRandom;
    private static float[] lastSmoothTarget;
    private static long lastSmoothTime = 0L;

    private static void validateRange(double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException("max не может быть меньше min.");
        }
    }

    FastRandom fastRandomize = new FastRandom();

    public static float randomNew(double min, double max) {
        if (min > max) return (float) (fastRandomize.nextFloat() * (min - max) + max);
        return (float) (fastRandomize.nextFloat() * (max - min) + min);
    }

    public Vector3d getPrevPositionVec(
            final Entity entity
    ) {
        return new Vector3d(
                entity.prevPosX,
                entity.prevPosY,
                entity.prevPosZ
        );
    }

    public static double limitDecimals(double value, int decimalPlaces) {
        return Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

    public float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public Vector3d getInterpolatedPositionVec(
            final Entity entity
    ) {
        final Vector3d prev = getPrevPositionVec(entity);

        return prev.add(entity.getPositionVec()
                .subtract(prev)
                .scale(mc.getRenderPartialTicks())
        );
    }

    public static Vector2f rotationToEntity(Entity target) {
        Vector3d vector3d = target.getPositionVec().subtract(Minecraft.getInstance().player.getPositionVec());
        double magnitude = Math.hypot(vector3d.x, vector3d.z);
        return new Vector2f(
                (float) Math.toDegrees(Math.atan2(vector3d.z, vector3d.x)) - 90.0F,
                (float) (-Math.toDegrees(Math.atan2(vector3d.y, magnitude))));
    }

    public Vector2f rotationToVec(Vector3d vec) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0f);
        double diffX = vec != null ? vec.x - eyesPos.x : 0;
        double diffY = vec != null ? vec.y - (mc.player.getPosY() + (double) mc.player.getEyeHeight() + 0.2) : 0;
        double diffZ = vec != null ? vec.z - eyesPos.z : 0;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        yaw = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        pitch = mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);

        return new Vector2f(yaw, pitch);
    }

    public Vector2f rotationToVec(Vector2f rotationVector, Vector3d target) {
        double x = target.x - mc.player.getPosX();
        double y = target.y - mc.player.getEyePosition(1).y;
        double z = target.z - mc.player.getPosZ();
        double dst = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(z, x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, dst)));
        float yawDelta = MathHelper.wrapDegrees(yaw - rotationVector.x);
        float pitchDelta = (pitch - rotationVector.y);

        if (abs(yawDelta) > 180)
            yawDelta -= signum(yawDelta) * 360;

        return new Vector2f(yawDelta, pitchDelta);
    }

    public static float wrapAngleTo180_float(float p_76142_0_) {
        if ((p_76142_0_ %= 360.0f) >= 180.0f) {
            p_76142_0_ -= 360.0f;
        }
        if (p_76142_0_ < -180.0f) {
            p_76142_0_ += 360.0f;
        }
        return p_76142_0_;
    }

    public static float valWave01(float value) {
        return (value > .5 ? 1 - value : value) * 2.F;
    }

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static int lerp(int a, int b, float f) {
        return a + (int) (f * (b - a));
    }


    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }


    public static boolean isInRegion(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double) x && mouseX <= (double) (x + width) && mouseY >= (double) y && mouseY <= (double) (y + height);
    }


    public static void drawSexyRect(float x, float y, float w, float h, float round, boolean colored) {
        RenderUtil.Rounded.smooth(new MatrixStack(), x, y, w, h, new Color(0x5C4D4D51, true).getRGB(), Round.of(round));
    }

    public org.joml.Vector3d interpolate(Entity entity, float partialTicks) {
        double posX = Interpolator.lerp(entity.lastTickPosX, entity.getPosX(), partialTicks);
        double posY = Interpolator.lerp(entity.lastTickPosY, entity.getPosY(), partialTicks);
        double posZ = Interpolator.lerp(entity.lastTickPosZ, entity.getPosZ(), partialTicks);
        return new org.joml.Vector3d(posX, posY, posZ);
    }

    public double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public Vector3d interpolate(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                interpolate(end.getX(), start.getX(), multiple),
                interpolate(end.getY(), start.getY(), multiple),
                interpolate(end.getZ(), start.getZ(), multiple));
    }

    public double interporate(double p_219803_0_, double p_219803_2_, double p_219803_4_) {
        return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_);
    }

    public static int randomInt(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {

        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static float random1(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static double randomWithUpdate(double min, double max, long ms, TimerUtil stopWatch) {
        double randomValue = 0;

        if (stopWatch.isReached(ms)) {
            randomValue = random1((float) min, (float) max);
            stopWatch.reset();
        }

        return randomValue;
    }

    public float fast(float end, float start, float multiple) {
        return (1 - MathHelper.clamp(deltaTime() * multiple, 0, 1)) * end
                + MathHelper.clamp(deltaTime() * multiple, 0, 1) * start;
    }

    public double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            final double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble() * (max - min) + min;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public double randomValue(double min, double max) {
        validateRange(min, max);
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    public float randomValue(float min, float max) {
        validateRange(min, max);
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    public double calcDiff(double a, double b) {
        return a - b;
    }

    public float deltaTime() {
        float debugFPS = Minecraft.getDebugFPS();
        if (debugFPS > 0) {
            return 1.0F / debugFPS;
        } else {
            return 1.0F;
        }
    }


    public String formatTime(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = ((millis % 360000) % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double round(double value, int increment) {
        double num = Math.pow(10, increment);
        return Math.round(value * num) / num;
    }

    public double round(final double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public double step(double value, double steps) {
        double roundedValue = Math.round(value / steps) * steps;
        return Math.round(roundedValue * 100.0) / 100.0;
    }

    public double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }


    public float clamp(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }

    public int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public double clamp01(double value) {
        return clamp(0.0D, 1.0D, value);
    }

    public float clamp01(float value) {
        return clamp(0.0F, 1.0F, value);
    }

    public double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = calcDiff(x2, x1);
        double deltaY = calcDiff(y2, y1);
        double deltaZ = calcDiff(z2, z1);
        return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double getDistance(BlockPos pos1, BlockPos pos2) {
        double deltaX = calcDiff(pos1.getX(), pos2.getX());
        double deltaY = calcDiff(pos1.getY(), pos2.getY());
        double deltaZ = calcDiff(pos1.getZ(), pos2.getZ());
        return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float limit(float current, float inputMin, float inputMax, float outputMin, float outputMax) {
        current = Mathf.clamp(inputMin, inputMax, current);
        float distancePercentage = (current - inputMin) / (inputMax - inputMin);
        return Interpolator.lerp(outputMin, outputMax, distancePercentage);
    }
}