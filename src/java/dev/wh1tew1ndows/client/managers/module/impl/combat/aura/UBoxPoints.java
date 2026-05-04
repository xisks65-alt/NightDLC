package dev.wh1tew1ndows.client.managers.module.impl.combat.aura;

import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class UBoxPoints {
    private static final Minecraft mc = Minecraft.getInstance();

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(value, min));
    }

    public static int lerp(int a, int b, float f) {
        return a + (int) (f * (b - a));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static Vector2f getVanillaRotate(Vector3d vec) {
        final Vector3d eyesPos = mc.player.getEyePosition(mc.getRenderPartialTicks());
        final Vector3d rot = vec.add(-eyesPos.x, -eyesPos.y, -eyesPos.z);
        final double xzD = MathHelper.sqrt(rot.x * rot.x + rot.z * rot.z);
        float yaw = (float) (Math.atan2(rot.z, rot.x) * 180.F / Math.PI - 90.F);
        float pitch = (float) Math.toDegrees(-Math.atan2(rot.y, xzD));
        return new Vector2f(yaw, pitch);
    }

    public static RayTraceResult traceBlock(Vector3d startVec, Vector3d endVec, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode) {
        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                blockMode,
                fluidMode,
                mc.player)
        );
    }

    private static double getDistanceXZ(ClientPlayerEntity self, double x, double z) {
        double d0 = self.getPosX() - x, d1 = self.getPosZ() - z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1);
    }

    private static boolean seenOnce3(ClientPlayerEntity self, double x, double y, double z) {
        Vector3d vector3d1 = new Vector3d(x, y, z);
        return mc.world != null && traceBlock(self.getEyePosition(mc.getRenderPartialTicks()), vector3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE).getType() != RayTraceResult.Type.BLOCK;
    }

    private static boolean seenOnceVector3d(ClientPlayerEntity self, Vector3d vec) {
        Vector3d vector3d = new Vector3d(self.getPosX(), self.getPosYEye(), self.getPosZ());
        return mc.world != null && traceBlock(vector3d, vec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE).getType() != RayTraceResult.Type.BLOCK;
    }

    private static boolean localSeen(ClientPlayerEntity selfEntity, final Vector3d xyz, final float scale) {
        return scale == 0 ? seenOnce3(selfEntity, xyz.x, xyz.y, xyz.z) :
                seenOnce3(selfEntity, xyz.x, xyz.y, xyz.z) &&
                        seenOnce3(selfEntity, xyz.x, xyz.y + scale, xyz.z) &&
                        seenOnce3(selfEntity, xyz.x, xyz.y - scale, xyz.z) &&
                        seenOnce3(selfEntity, xyz.x + scale, xyz.y, xyz.z) &&
                        seenOnce3(selfEntity, xyz.x - scale, xyz.y, xyz.z) &&
                        seenOnce3(selfEntity, xyz.x, xyz.y, xyz.z + scale) &&
                        seenOnce3(selfEntity, xyz.x, xyz.y, xyz.z - scale);
    }

    public static List<Vector3d> entityBoxVec3dsAlternates(Entity entity) {
        //List<Vector3d> list = new ArrayList<>();
        //for (AxisAlignedBB aabb : Client.instance.moduleManager.backTrackModule.getTracksAsEntity(entity, entity.getBoundingBox(), true))
        //    list.addAll(entityBoxVec3dsAlternate(entity, aabb));
        //return list;
        return entityBoxVec3dsAlternate(entity, entity.getRenderBoundingBox());
    }

    public static List<Vector3d> entityBoxVec3dsAlternate(Entity entity, AxisAlignedBB aabb) {
        if (entity == null) return null;
        final List<Vector3d> vecs = new ArrayList<>();
        double offsetXYZ = .02F;
        int maxPointsCountXZ = 14, minPointsCountXZ = 5;
        int maxPointsCountY = 27, minPointsCountY = 9;
        double[] whh = new double[]{entity.getWidth() - offsetXYZ * 2D, entity.getHeight() - offsetXYZ * 2D, (entity.getHeight() - offsetXYZ * 2D) / 1.05D};
        double[] xyz = new double[]{entity.getPosX(), entity.getPosY(), entity.getPosZ()};
        double[] xyz1 = new double[]{xyz[0] + whh[0] / 2.D, xyz[1] + whh[1], xyz[2] + whh[0] / 2.D};
        double[] xyz2 = new double[]{xyz[0] - whh[0] / 2.D, xyz[1], xyz[2] - whh[0] / 2.D};
        if (aabb != null) {
            aabb = aabb.grow(-offsetXYZ);
            whh = new double[]{aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, (aabb.maxY - aabb.minY) / 1.05D};
            xyz = new double[]{aabb.minX + whh[0] / 2.D, aabb.minY, aabb.minZ + whh[0] / 2.D};
            xyz1 = new double[]{aabb.minX, aabb.minY, aabb.minZ};
            xyz2 = new double[]{aabb.maxX, aabb.maxY, aabb.maxZ};
        } else {
            xyz2 = new double[]{xyz[0] + whh[0] / 2.D, xyz[1] + whh[1], xyz[2] + whh[0] / 2.D};
            xyz1 = new double[]{xyz[0] - whh[0] / 2.D, xyz[1], xyz[2] - whh[0] / 2.D};
        }
        float sqrtWHH0CubeD2 = (float) Math.sqrt(whh[0] * whh[0] + whh[0] * whh[0] + whh[0] * whh[0]) / 2.F;
        final ClientPlayerEntity me = mc.player;
        if (me == null) return null;
        final float factorCount = (1.F - Math.min(me.getDistanceToCoord(xyz[0], xyz[1], xyz[2]) / 5.F, 1.F)) * Math.min(me.getDistanceToCoord(xyz[0], me.getPosY(), xyz[2]) / .6F, 1.F);
        final int pointsCountXZ = lerp(minPointsCountXZ, maxPointsCountXZ, factorCount);
        final int pointsCountY = lerp(minPointsCountY, maxPointsCountY, factorCount);


        float scaleSeenCheck = .0F;//zero is very strong optimize
        //final Vector3d xyz3 = new Vector3d(0.D, 0.D, 0.D);
        int cIX = 0;
        for (final Integer xsI : IntStream.range(0, pointsCountXZ).toArray()) {
            final boolean edgeX = xsI == 0 || xsI == pointsCountXZ - 1;
            final double xs = lerp(xyz1[0], xyz2[0], xsI / (float) (pointsCountXZ - 1));
            for (final Integer zsI : IntStream.range(0, pointsCountXZ).toArray()) {
                final boolean edgeZ = zsI == 0 || zsI == pointsCountXZ - 1;
                final double zs = lerp(xyz1[2], xyz2[2], zsI / (float) (pointsCountXZ - 1));
                for (final Integer ysI : IntStream.range(0, pointsCountY).toArray()) {
                    final boolean edgeY = ysI == 0 || ysI == pointsCountY - 1;
                    final double ys = lerp(xyz1[1], xyz2[1], ysI / (float) (pointsCountY - 1));
                    final Vector3d vec = new Vector3d(xs, ys, zs);
                    if (!edgeX && !edgeZ && !edgeY || me.getDistanceToVec(vec.add(0.D, -me.getEyeHeight(), 0.D)) < sqrtWHH0CubeD2 || !localSeen(me, vec, scaleSeenCheck))
                        continue;
                    if (!vecs.add(vec)) break;
                    //mc.world.addParticle(ParticleTypes.BUBBLE, xs, ys, zs, 0, 0, 0);
                }
            }
        }
        return vecs;
    }

    private static double getDistanceAtVec3dToVec3d(Vector3d first, Vector3d second) {
        final double xDiff, yDiff, zDiff;
        return Math.sqrt((xDiff = first.x - second.x) * xDiff + (yDiff = first.y - second.y) * yDiff + (zDiff = first.z - second.z) * zDiff);
    }

    public static Vector3d getBestVector3dOnEntityBox(Entity entity, boolean alwaysMultipoints) {
        if (entity == null) return mc.player.getEyePosition(1.F);
        AxisAlignedBB aabb = entity.getRenderBoundingBox();
        //for (AxisAlignedBB aabb : Client.instance.moduleManager.backTrackModule.getTracksAsEntity(entity, entity.getBoundingBox(), true)) {
        double[] whh = new double[]{entity.getWidth(), entity.getHeight(), entity.getHeight() / 1.05F};
        double[] xyz = new double[]{entity.getPosX(), entity.getPosY(), entity.getPosZ()};
        if (aabb != null) {
            whh = new double[]{aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, (aabb.maxY - aabb.minY) / 1.1F};
            xyz = new double[]{aabb.minX + whh[0] / 2.D, aabb.minY, aabb.minZ + whh[0] / 2.D};
        }
        double[] diffs = new double[]{mc.player.getPosY() - xyz[1], getDistanceXZ(mc.player, xyz[0], xyz[2])};
        // double ddtn = clamp(diffs[1] / (2.9D + whh[0] / 2.D), 0.4D, .95D);
        double ddtn = clamp(Easings.QUART_OUT.ease((diffs[1] - whh[0] / 2.F) / (5.D + whh[0] / 2.D)), 0.1D, .95D);
        double pca = clamp(ddtn * ddtn, 0.D, 1.D);
        final double pitchPointHeight = clamp((whh[2] / 2.D * pca + (whh[2] / 2.D) * (clamp(diffs[0] + pca, 0.D, 1.D))), 0, whh[2]);
        Vector3d defaultVec = new Vector3d(xyz[0], xyz[1] + pitchPointHeight, xyz[2]);
        if (!alwaysMultipoints && !seenOnceVector3d(mc.player, defaultVec))
            defaultVec = defaultVec.add(0.D, -pitchPointHeight / 2.D, 0.D);
        if (whh[1] <= 1D || !alwaysMultipoints && seenOnceVector3d(mc.player, defaultVec)) {
            return defaultVec;
        } else {
            final List<Vector3d> normalVecs = entityBoxVec3dsAlternate(entity, aabb);
            //entityBoxVec3d`s
            float factorDown = 1.F - (float) Math.max(Math.min((diffs[1] - 2.F) / 3.F, 1.F), 0.F);
            final Vector3d toSortVec = new Vector3d(mc.player.getPosX(), mc.player.getPosY() + .6F + lerp(pitchPointHeight, pitchPointHeight / 2.5F, factorDown), mc.player.getPosZ());
            if (normalVecs != null && normalVecs.size() > 1)
                normalVecs.sort(Comparator.comparing(vec3 -> getDistanceAtVec3dToVec3d(toSortVec, vec3)));
            //if (normalVecs.get(0) != null) mc.world.addParticle(ParticleTypes.MYCELIUM, normalVecs.get(0).x, normalVecs.get(0).y, normalVecs.get(0).z, 0, 0, 0);
            return normalVecs != null && normalVecs.size() > 0 ? normalVecs.get(0) : defaultVec;
        }
        //}
        //return entity.getEyePosition(1.F);

    }

    public static Vector3d getBestVector3dOnEntityBox(Entity entity) {
        return getBestVector3dOnEntityBox(entity, mc.player.getDistanceToCoord(entity.getPosX(), mc.player.getPosY(), entity.getPosZ()) > entity.getWidth() * 1.37F);
    }

    public static Vector2f getBestRotateVector2fOnEntityBox(Entity entity, boolean alwaysMultipoints) {
        Vector3d vec = UBoxPoints.getBestVector3dOnEntityBox(entity, alwaysMultipoints);
        return UBoxPoints.getVanillaRotate(vec);
    }

    public static Vector2f getBestRotateVector2fOnEntityBox(Entity entity) {
        return getBestRotateVector2fOnEntityBox(entity, true);
    }
}