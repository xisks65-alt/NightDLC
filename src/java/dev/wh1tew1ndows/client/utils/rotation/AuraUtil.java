package dev.wh1tew1ndows.client.utils.rotation;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector4f;

import static net.minecraft.util.math.MathHelper.clamp;

@UtilityClass
public class AuraUtil implements IMinecraft {

    public Vector3d getClosestVec(Vector3d vec, AxisAlignedBB AABB) {
        return new Vector3d(
                MathHelper.clamp(vec.getX(), AABB.minX, AABB.maxX),
                MathHelper.clamp(vec.getY(), AABB.minY, AABB.maxY),
                MathHelper.clamp(vec.getZ(), AABB.minZ, AABB.maxZ)
        );
    }

    public static Vector3d getVector(LivingEntity target) {

        double wHalf = target.getWidth() / 2;

        double yExpand = clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());

        double xExpand = clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        double zExpand = clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }

    public static Vector3d getBestVec3d(final Vector3d pos, final AxisAlignedBB axisAlignedBB) {
        double lastDistance = Double.MAX_VALUE;
        Vector3d bestVec = null;

        final double xWidth = axisAlignedBB.maxX - axisAlignedBB.minX;
        final double zWidth = axisAlignedBB.maxZ - axisAlignedBB.minZ;
        final double height = axisAlignedBB.maxY - axisAlignedBB.minY;

        for (float x = 0F; x < 1F; x += 0.1F) {
            for (float y = 0F; y < 1F; y += 0.1F) {
                for (float z = 0F; z < 1F; z += 0.1F) {

                    final Vector3d hitVec = new Vector3d(
                            axisAlignedBB.minX + xWidth * x,
                            axisAlignedBB.minY + height * y,
                            axisAlignedBB.minZ + zWidth * z
                    );

                    final double distance = pos.distanceTo(hitVec);

                    if (isHitBoxNotVisible(hitVec) && distance < lastDistance) {
                        bestVec = hitVec;
                        lastDistance = distance;
                    }
                }
            }
        }

        return bestVec;
    }

    public static boolean isHitBoxNotVisible(final Vector3d vec3d) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                mc.player.getEyePosition(1F),
                vec3d,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == RayTraceResult.Type.MISS;
    }

    public static Vector3d getSpookyVector(LivingEntity target) {
        double yExpand = net.minecraft.util.math.MathHelper.clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());
        double xExpand = net.minecraft.util.math.MathHelper.clamp(mc.player.getPosX() - target.getPosX(), -0, 0);
        double zExpand = MathHelper.clamp(mc.player.getPosZ() - target.getPosZ(), -0, 0);

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }

    public static Vector3d calculateTargetVector(LivingEntity target) {
        Vector3d targetEyePosition = target.getPositionVec().add(0, target.getEyeHeight() - 0.24, 0);
        return targetEyePosition.subtract(mc.player.getEyePosition(1.0F));
    }

    public Vector3d getClosestVec(Vector3d vec, Entity entity) {
        return getClosestVec(vec, entity.getBoundingBox());
    }

    public Vector3d getClosestVec(Entity entity) {
        Vector3d eyePosVec = mc.player.getEyePosition(mc.getRenderPartialTicks());
        return getClosestVec(eyePosVec, entity).subtract(eyePosVec);
    }

    public double getStrictDistance(Entity entity) {
        return getClosestVec(entity).length();
    }

    public double getStrictDistance(LivingEntity entity) {
        return getClosestVec(entity).length();
    }

    public Vector3d getClosestTargetPoint(Entity entity) {
        return getClosestTargetPoint(mc.player.getEyePosition(mc.getRenderPartialTicks()), entity, Math.min(entity.getWidth(), entity.getHeight()) / 4F);
    }

    public Vector3d getClosestTargetPoint(Vector3d vec, Entity entity, float point) {
        if (entity == null) {
            return Vector3d.ZERO;
        }

        AxisAlignedBB boundingBox = entity.getBoundingBox().grow(-point);
        Vector3d center = boundingBox.getCenter();
        Vector3d closestPoint = Vector3d.ZERO;
        double closestDistance = Double.MAX_VALUE;

        for (double offsetX = 0; offsetX <= (boundingBox.maxX - boundingBox.minX) / 2; offsetX += 0.1) {
            for (double offsetY = 0; offsetY <= (boundingBox.maxY - boundingBox.minY) / 2; offsetY += 0.1) {
                for (double offsetZ = 0; offsetZ <= (boundingBox.maxZ - boundingBox.minZ) / 2; offsetZ += 0.1) {
                    for (int signX : new int[]{-1, 1}) {
                        for (int signY : new int[]{-1, 1}) {
                            for (int signZ : new int[]{-1, 1}) {
                                double x = center.x + signX * offsetX;
                                double y = center.y + signY * offsetY;
                                double z = center.z + signZ * offsetZ;
                                Vector3d potentialPoint = new Vector3d(x, y, z);
                                Vector2f rotation = RotationUtil.calculate(potentialPoint);
                                RayTraceResult result = RayTraceUtil.calculateRayTrace(
                                        mc.playerController.extendedReach() ? 6.0D : 3.0D,
                                        rotation.x,
                                        rotation.y,
                                        mc.player,
                                        false
                                );

                                if (result instanceof EntityRayTraceResult entityTrace && entityTrace.getEntity().equals(entity)) {
                                    double distance = vec.distanceTo(potentialPoint);
                                    if (distance < closestDistance) {
                                        closestDistance = distance;
                                        closestPoint = potentialPoint;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!closestPoint.equals(Vector3d.ZERO)) {
            return closestPoint;
        }

        double closestX = MathHelper.clamp(vec.x, boundingBox.minX, boundingBox.maxX);
        double closestY = MathHelper.clamp(vec.y, boundingBox.minY, boundingBox.maxY);
        double closestZ = MathHelper.clamp(vec.z, boundingBox.minZ, boundingBox.maxZ);

        return new Vector3d(closestX, closestY, closestZ);
    }

    public static float calculateCorrectYawOffset(float yaw) {
        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        float distSquared = (float) (xDiff * xDiff + zDiff * zDiff);
        float renderYawOffset = mc.player.prevRenderYawOffset;
        float offset = renderYawOffset;
        float yawOffsetDiff;
        if (distSquared > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDiff, xDiff) * 180.0f / (float) Math.PI - 90.0f;
        }
        if (mc.player != null && mc.player.swingProgress > 0.0f) {
            offset = yaw;
        }
        yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.15f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -45, 45);
        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.2f;
        }

        return renderYawOffset;
    }

    public Vector4f calculateRotation(Entity target) {
        Vector3d vec = getClosestTargetPoint(target).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()));

        float rawYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
        float rawPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.z, 2)))));
        float yawDelta = MathHelper.wrapDegrees(rawYaw - mc.player.rotationYaw);
        float pitchDelta = rawPitch - mc.player.rotationPitch;

        return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
    }

    public Vector4f calculateRotationFromCamera(LivingEntity target) {
        Vector3d vec = getClosestTargetPoint(target).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()));

        float rawYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
        float rawPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.z, 2)))));
        float yawDelta = MathHelper.wrapDegrees(rawYaw - Rotation.cameraYaw());
        float pitchDelta = rawPitch - Rotation.cameraPitch();

        return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
    }

    public double calculateFOV(LivingEntity target) {
        Vector4f rotation = calculateRotation(target);
        float yawDelta = rotation.z;
        float pitchDelta = rotation.w;

        return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }

    public double calculateFOVFromCamera(LivingEntity target) {
        Vector4f rotation = calculateRotationFromCamera(target);
        float yawDelta = rotation.z;
        float pitchDelta = rotation.w;

        return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }

}