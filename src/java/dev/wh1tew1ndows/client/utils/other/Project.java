package dev.wh1tew1ndows.client.utils.other;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import org.joml.Vector2f;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Project implements IMinecraft {
    public Vector2f project2D(Vector3d vec) {
        return project2D(vec.x, vec.y, vec.z);
    }

    public static boolean isInView(Entity ent) {
        assert (mc.getRenderViewEntity() != null);
        WorldRenderer.frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return WorldRenderer.frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public Vector2f project2D(double x, double y, double z) {
        if (mc.getRenderManager().info == null) return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
//        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
//        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
//        cameraRotation.conjugate();
//
//        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
//        result3f.transform(cameraRotation);
//
//        Entity renderViewEntity = mc.getRenderViewEntity();
//        if (renderViewEntity instanceof PlayerEntity playerentity) {
//            final NoRender noRender = Client.inst().featureManager().noRender;
//            if (!noRender.enabled() || !noRender.elements().get("Тряска экрана").get())
//                hurtCameraEffect(playerentity, result3f);
//            if (mc.gameSettings.viewBobbing) {
//                calculateViewBobbing(playerentity, result3f);
//            }
//        }
//
//        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
//
//        return calculateScreenPosition(result3f, fov);

        net.minecraft.util.math.vector.Vector2f result = new net.minecraft.util.math.vector.Vector2f(0, 0);
        //  boolean render = worldToScreen(mc.gameRenderer.getProject2DMatrix(), (float) (x - mc.getRenderManager().renderPosX()), (float) (y - mc.getRenderManager().renderPosY()), (float) (z - mc.getRenderManager().renderPosZ()), result);
//
        //  if (render)
        //      return new Vector2f(result.x, result.y);

        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    private void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }

    private void hurtCameraEffect(PlayerEntity playerentity, Vector3f result3f) {
        float partialTicks = mc.getRenderPartialTicks();
        float f = (float) playerentity.hurtTime - partialTicks;

        if (playerentity.getShouldBeDead()) {
            float f1 = Math.min((float) playerentity.deathTime + partialTicks, 20.0F);
            Quaternion quaternion1 = new Quaternion(Vector3f.ZP, 40.0F - 8000.0F / (f1 + 200.0F), true);
            quaternion1.conjugate();
            result3f.transform(quaternion1);
        }

        if (f < 0.0F) {
            return;
        }

        f = f / (float) playerentity.maxHurtTime;
        f = MathHelper.sin(f * f * f * f * (float) Math.PI);
        float f2 = playerentity.attackedAtYaw;

        Quaternion quaternion1 = new Quaternion(Vector3f.YP, -f2, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Quaternion quaternion2 = new Quaternion(Vector3f.ZP, -f * 14.0F, true);
        quaternion2.conjugate();
        result3f.transform(quaternion2);

        Quaternion quaternion3 = new Quaternion(Vector3f.ZP, f2, true);
        quaternion3.conjugate();
        result3f.transform(quaternion3);
    }

    private Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mw.getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));

        if (result3f.getZ() < 0.0F) {

            float aspectRation = (float) mc.getMainWindow().getFramebufferWidth() / (float) mc.getMainWindow().getFramebufferHeight();

            float width = mw.getScaledWidth() / aspectRation * (aspectRation + 2);
            float height = mw.getScaledHeight();

            return new Vector2f(-result3f.getX() * scaleFactor + width / 2.0F, height / 2.0F - result3f.getY() * scaleFactor);
        }

        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    private static final Map<Entity, Vector4d> entityPositions = new HashMap<>();

    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);


    public static Vector4d updatePlayerPositions(Entity player, float partialTicks) {
        Vector3d projection = mc.getRenderManager().info.getProjectedView();
        double x = Mathf.interpolate(player.getPosX(), player.lastTickPosX, partialTicks);
        double y = Mathf.interpolate(player.getPosY(), player.lastTickPosY, partialTicks);
        double z = Mathf.interpolate(player.getPosZ(), player.lastTickPosZ, partialTicks);
        Vector3d size = new Vector3d(player.getBoundingBox().maxX - player.getBoundingBox().minX, player.getBoundingBox().maxY - player.getBoundingBox().minY, player.getBoundingBox().maxZ - player.getBoundingBox().minZ);
        AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2.0, y, z - size.z / 2.0, x + size.x / 2.0, y + size.y, z + size.z / 2.0);
        Vector4d position2 = null;
        for (int i = 0; i < 8; ++i) {
            Vector3d vector = new Vector3d(i % 2 == 0 ? aabb.minX : aabb.maxX, i / 2 % 2 == 0 ? aabb.minY : aabb.maxY, i / 4 % 2 == 0 ? aabb.minZ : aabb.maxZ);
            vector = project22D(vector.x - projection.x, vector.y - projection.y, vector.z - projection.z);
            if (vector == null || !(vector.z >= 0.0) || !(vector.z < 1.0)) continue;
            if (position2 == null) {
                position2 = new Vector4d(vector.x, vector.y, vector.z, 1.0);
                continue;
            }
            position2.x = Math.min(vector.x, position2.x);
            position2.y = Math.min(vector.y, position2.y);
            position2.z = Math.max(vector.x, position2.z);
            position2.w = Math.max(vector.y, position2.w);
        }

        // Store the position for later retrieval
        if (position2 != null) {
            entityPositions.put(player, position2);
        }

        return position2;
    }

    private static Vector3d project22D(double x, double y, double z) {
        GL11.glGetFloatv(2982, modelview);
        GL11.glGetFloatv(2983, projection);
        GL11.glGetIntegerv(2978, viewport);
        if (gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, vector)) {
            return new Vector3d(vector.get(0) / 2.0f, ((float) mc.getMainWindow().getHeight() - vector.get(1)) / 2.0f, vector.get(2));
        }
        return null;
    }

    public static boolean gluProject(float objx, float objy, float objz, FloatBuffer modelMatrix, FloatBuffer projMatrix, IntBuffer viewport, FloatBuffer win_pos) {
        return Project.gluProject(objx, objy, objz, modelMatrix, projMatrix, viewport, win_pos);
    }

    public static Vector4f getEntity2DPosition(Matrix4f matrix4f, Entity entity, float partialTicks) {
        float width = entity.getWidth() * 0.6F;
        float height = entity.getHeight();

        float interpX = (float) EntityUtil.getRenderInterpX(entity, partialTicks);
        float interpY = (float) EntityUtil.getRenderInterpY(entity, partialTicks);
        float interpZ = (float) EntityUtil.getRenderInterpZ(entity, partialTicks);

        //if (entity.getType() == EntityType.ITEM) {
        //    interpY = entity.getAnimatedPosY(partialTicks);
        //}

        float minX = interpX - width;
        float minZ = interpZ - width;
        float maxX = interpX + width;
        float maxY = interpY + height;
        float maxZ = interpZ + width;

        Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        net.minecraft.util.math.vector.Vector2f screenCoords = net.minecraft.util.math.vector.Vector2f.ZERO;

        float[][] corners = {
                {minX, interpY, minZ},
                {minX, maxY, minZ},
                {maxX, interpY, minZ},
                {maxX, maxY, minZ},
                {minX, interpY, maxZ},
                {minX, maxY, maxZ},
                {maxX, interpY, maxZ},
                {maxX, maxY, maxZ}
        };

        for (float[] corner : corners) {
            if (worldToScreen(matrix4f, corner[0], corner[1], corner[2], screenCoords)) {
                position.set(
                        Math.min(screenCoords.x, position.getX()),
                        Math.min(screenCoords.y, position.getY()),
                        Math.max(screenCoords.x, position.getZ()),
                        Math.max(screenCoords.y, position.getW())
                );
            }
        }

        return position;
    }

    public static boolean worldToScreen(Matrix4f matrix, float x, float y, float z, net.minecraft.util.math.vector.Vector2f result) {
        float w = matrix.m30 * x + matrix.m31 * y + matrix.m32 * z + matrix.m33;

        if (w < 0.01F) {
            return false;
        }

        float x2 = matrix.m00 * x + matrix.m01 * y + matrix.m02 * z + matrix.m03;
        float y2 = matrix.m10 * x + matrix.m11 * y + matrix.m12 * z + matrix.m13;

        //int width = (int) mw.getHalfWidth();
        //int height = (int) mw.getHalfHeight();

        //result.set(
        //        width * 0.5F + 0.5F * (x2 / w * width + 1.0F),
        //        height * 0.5F - 0.5F * (y2 / w * height + 1.0F)
        //);

        return true;
    }
}
