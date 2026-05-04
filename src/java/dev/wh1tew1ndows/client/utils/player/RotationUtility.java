package dev.wh1tew1ndows.client.utils.player;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;
import org.joml.Vector2f;

import static java.lang.Math.PI;

public class RotationUtility implements IMinecraft {


    public Vector2f calculate(final double x, final double y, final double z) {
        net.minecraft.util.math.vector.Vector3d pos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        return calculate(new org.joml.Vector3d(pos.x, pos.y, pos.z), new org.joml.Vector3d(x, y, z));
    }

    public static Vector2f calculate(final org.joml.Vector3d to) {
        net.minecraft.util.math.vector.Vector3d pos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        org.joml.Vector3d from = new org.joml.Vector3d(pos.x, pos.y, pos.z);
        return calculate(from, to);
    }

    public static Vector2f calculate(final org.joml.Vector3d from, final org.joml.Vector3d to) {
        org.joml.Vector3d diff = to.sub(from);
        double distance = java.lang.Math.hypot(diff.x(), diff.z());
        float yaw = (float) (MathHelper.atan2(diff.z(), diff.x()) * 180F / PI) - 90F;
        float pitch = (float) (-(MathHelper.atan2(diff.y(), distance) * 180F / PI));
        yaw = normalize(yaw);
        pitch = Math.clamp(-90, 90, pitch);
        return new Vector2f(yaw, pitch);
    }

    public static float normalize(float value) {
        value = value % 360.0f;
        if (value > 180.0f) {
            value -= 360.0f;
        } else if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }

    public static Vector2f calculate(final Entity entity) {
        net.minecraft.util.math.vector.Vector3d pos = entity.getPositionVec().add(0,
                java.lang.Math.max(0,
                        Math.min(mc.player.getPosY() - entity.getPosY() + mc.player.getEyeHeight(),
                                (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.75F)),
                0);
        org.joml.Vector3d to = new org.joml.Vector3d(pos.x, pos.y, pos.z);
        return calculate(to);
    }


}
