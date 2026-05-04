package dev.wh1tew1ndows.client.utils.rotation;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class RayTraceUtil implements IMinecraft {


    public boolean canSeen(Vector3d vec) {
        Vector3d vector3d = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        return mc.world.rayTraceBlocks(new RayTraceContext(vector3d, vec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player)).getType() == RayTraceResult.Type.MISS;
    }

    public double getDistanceFromEye(Vector3d vec) {
        float f = (float) (mc.player.getPosX() - vec.x);
        float f1 = (float) (mc.player.getPosYEye() - vec.y);
        float f2 = (float) (mc.player.getPosZ() - vec.z);
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public Vector3d getPoint(LivingEntity target) {
        return getPoint(target, false);
    }

    public Vector3d getPoint(LivingEntity target, boolean silent) {
        if (target == null) return Vector3d.ZERO;
        double hitboxSize = target.getBoundingBox().maxY - target.getBoundingBox().minY;
        double additional = hitboxSize / 2;
        Vector3d pos = getBestPoint(mc.player.getEyePosition(mc.timer.renderPartialTicks), target, silent);

        return pos;
    }

    public Vector3d getBestPoint(Vector3d pos, LivingEntity entity, boolean silent) {
        if (entity == null) return Vector3d.ZERO;

        double safePoint = 0;
        Vector3d fastPoint = new Vector3d(
                MathHelper.clamp(pos.x,
                        entity.getBoundingBox().minX + safePoint,
                        entity.getBoundingBox().maxX - safePoint),

                MathHelper.clamp(pos.y,
                        entity.getBoundingBox().minY + safePoint,
                        entity.getBoundingBox().maxY - safePoint),

                MathHelper.clamp(pos.z,
                        entity.getBoundingBox().minZ + safePoint,
                        entity.getBoundingBox().maxZ - safePoint)
        );

        if (!silent && !RayTraceUtil.canSeen(fastPoint)) {
            MultiPoints.update(entity);
            Vector3d bestPoint = MultiPoints.getBestPoint(entity);

            if (bestPoint != null)
                return bestPoint;
        }

        return fastPoint;
    }

    public Entity getTargetedEntity(Entity target, float targetYaw, float targetPitch, double distance) {
        Entity viewerEntity = mc.getRenderViewEntity();
        if (viewerEntity == null || mc.world == null) {
            return null;
        }

        Vector3d startVector = viewerEntity.getEyePosition(mc.getRenderPartialTicks());
        Vector3d directionVector = getVectorForRotation(targetPitch, targetYaw);
        Vector3d endVector = startVector.add(directionVector.scale(distance));

        AxisAlignedBB targetBoundingBox = target.getBoundingBox().grow(target.getCollisionBorderSize());
        EntityRayTraceResult entityRayTraceResult = traceEntities(viewerEntity, startVector, endVector, targetBoundingBox,
                (entity) -> !entity.isSpectator() && entity.canBeCollidedWith(), distance);

        return entityRayTraceResult != null ? entityRayTraceResult.getEntity() : null;
    }

    public EntityRayTraceResult traceEntities(Entity shooter, Vector3d startVector, Vector3d endVector, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance) {
        World world = shooter.world;
        double closestDistance = distance;
        Entity closestEntity = null;
        Vector3d closestHitVector = null;

        for (Entity entity : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter)) {
            AxisAlignedBB entityBoundingBox = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
            Optional<Vector3d> optional = entityBoundingBox.rayTrace(startVector, endVector);

            if (entityBoundingBox.contains(startVector) || optional.isPresent()) {
                double distanceToHit = optional.map(startVector::distanceTo).orElse(0.0D);
                if (distanceToHit < closestDistance || closestDistance == 0.0D) {
                    if (entity.getLowestRidingEntity() != shooter.getLowestRidingEntity()) {
                        closestEntity = entity;
                        closestHitVector = optional.orElse(startVector);
                        closestDistance = distanceToHit;
                    }
                }
            }
        }

        return closestEntity == null ? null : new EntityRayTraceResult(closestEntity, closestHitVector);
    }

    public RayTraceResult calculateRayTrace(double distance, float yaw, float pitch, Entity entity, boolean ignoreBlocks) {
        Vector3d startVector = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d directionVector = getVectorForRotation(pitch, yaw);
        Vector3d endVector = startVector.add(directionVector.scale(distance));

        RayTraceResult blockResult = traceBlock(startVector, endVector, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE);
        double entityDistance = blockResult.getHitVec().squareDistanceTo(startVector);

        AxisAlignedBB entityBoundingBox = entity.getBoundingBox().expand(directionVector.scale(distance)).grow(1.0D);
        EntityRayTraceResult entityRayTraceResult = ProjectileHelper.rayTraceEntities(entity, startVector, endVector, entityBoundingBox,
                (e) -> !e.isSpectator() && e.isAlive() && e.canBeCollidedWith(), distance);

        if (entityRayTraceResult != null && (ignoreBlocks || entityRayTraceResult.getHitVec().squareDistanceTo(startVector) < entityDistance)) {
            return entityRayTraceResult;
        }

        return blockResult;
    }


    public boolean rayTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target, boolean blocks) {

        if (!mc.player.isInWater()) {

            RayTraceResult object = null;
            if (target == null) return false;
            if (entity != null && mc.world != null) {
                float partialTicks = mc.getRenderPartialTicks();
                double distance = rayTraceDistance;
                object = rayTrace(rayTraceDistance, yaw, pitch, entity);
                Vector3d vector3d = entity.getEyePosition(partialTicks);
                boolean flag = false;
                double d1 = distance;

                d1 = d1 * d1;

                if (object != null) {
                    d1 = object.getHitVec().squareDistanceTo(vector3d);
                }

                Vector3d vector3d1 = getVectorForRotation(pitch, yaw);
                Vector3d vector3d2 = vector3d.add(vector3d1.x * distance, vector3d1.y * distance, vector3d1.z * distance);
                float f = 1.0F;
                AxisAlignedBB axisalignedbb = entity.getBoundingBox().expand(vector3d1.scale(distance)).grow(1.0D, 1.0D, 1.0D);
                boolean traced = tracedTo(entity, vector3d, vector3d2, axisalignedbb, (p_lambda$getMouseOver$0_0_) ->
                {
                    return !p_lambda$getMouseOver$0_0_.isSpectator() && p_lambda$getMouseOver$0_0_.canBeCollidedWith();
                }, d1, target);

                return traced;

            }
        }
        return true;
    }

    public static boolean tracedTo(Entity shooter, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance, Entity target) {
        World world = shooter.world;
        double d0 = distance;

        for (Entity entity1 : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter)) {

            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(entity1.getCollisionBorderSize());
            Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

            if (axisalignedbb.contains(startVec)) {

                if (d0 >= 0.0D) {
                    if (entity1 == target) return true;
                    d0 = 0.0D;
                }
            } else if (optional.isPresent()) {

                Vector3d vector3d1 = optional.get();
                double d1 = startVec.squareDistanceTo(vector3d1);

                //if (d1 < d0 || d0 == 0.0D)
                {

                    if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity()) {
                        if (d0 == 0.0D) {
                            if (entity1 == target) return true;
                        }
                    } else {
                        if (entity1 == target) return true;
                        d0 = d1;
                    }
                }
            }
        }

        return false;
    }

    public RayTraceResult rayTrace(double rayTraceDistance,
                                   float yaw,
                                   float pitch,
                                   Entity entity) {
        Vector3d startVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = startVec.add(
                directionVec.x * rayTraceDistance,
                directionVec.y * rayTraceDistance,
                directionVec.z * rayTraceDistance
        );

        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                entity)
        );
    }


    public boolean rayTraceEntity(float yaw, float pitch, double distance, Entity entity) {
        Vector3d eyeVec = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d lookVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = eyeVec.add(lookVec.scale(distance));

        AxisAlignedBB entityBox = entity.getBoundingBox();
        return entityBox.contains(eyeVec) || entityBox.rayTrace(eyeVec, endVec).isPresent();
    }

    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float) Math.PI / 180) - (float) Math.PI;
        float pitchRadians = -pitch * ((float) Math.PI / 180);

        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosPitch = -MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);

        return new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }

    public RayTraceResult traceBlock(Vector3d startVec, Vector3d endVec, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode) {
        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                blockMode,
                fluidMode,
                mc.player)
        );
    }
}
