package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.annotations.Beta;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.Projectiles;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.InvUtil;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Beta
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "TargetPearl", category = Category.PLAYER)
public class TargetPearl extends Module {
    public static TargetPearl getInstance() {
        return Instance.get(TargetPearl.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Key", "Auto");
    private final BindSetting bind = new BindSetting(this, "Кнопка", -1).setVisible(() -> mode.is("Key"));
    private final BooleanSetting onlyTarget = new BooleanSetting(this, "Только за таргетом", false);
    private final SliderSetting distance = new SliderSetting(this, "Минимальная дистанция до пёрла", 10, 8, 20, 1);
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();

    private boolean cooldownCheck() {
        return !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL);
    }

    @EventHandler
    public void onKeyPress(EventKeyboardMouse event) {
        if (mode.is("Key") && event.getKey() == (bind.getValue()) && cooldownCheck() && script.isFinished()) {
            aimAndThrowPearl();
        }
    }


    @EventHandler
    public void onUpdateEvent(UpdateEvent event) {
        if (mode.is("Auto") && cooldownCheck() && script.isFinished()) {
            aimAndThrowPearl();
        }
        script.update();
    }

    public float[] calculateYawPitch(Vector3d targetPosition, double velocity) {
        Vector3d playerPosition = mc.player.getPositionVec();

        double deltaX = targetPosition.x - playerPosition.x;
        double deltaY = targetPosition.y - (playerPosition.y + mc.player.getEyeHeight());
        double deltaZ = targetPosition.z - playerPosition.z;

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float gravity = 0.03F;
        float pitch = (float) -Math.toDegrees(Math.atan((velocity * velocity - Math.sqrt(velocity * velocity * velocity * velocity - gravity * (gravity * horizontalDistance * horizontalDistance + 2 * deltaY * velocity * velocity))) / (gravity * horizontalDistance)));

        return new float[]{yaw, pitch};
    }

    public void aimAndThrowPearl() {
        Vector3d targetPearlLandingPosition = getTargetPearlLandingPosition();

        if (targetPearlLandingPosition != null && mc.player.getPositionVec().distanceTo(targetPearlLandingPosition) > distance.getValue()) {
            float[] yawPitch = calculateYawPitch(targetPearlLandingPosition, 1.5F);
            boolean findPearl = InvUtil.getSlot(Items.ENDER_PEARL) != null;
            Vector3d trajectoryPearl = checkTrajectory(yawPitch[0], yawPitch[1]);

            if (findPearl && trajectoryPearl != null && targetPearlLandingPosition.distanceTo(trajectoryPearl) > 8) {
                return;
            }

            // if (ViaUtil.allowedBypass()) {
            //     InvUtil.findItemAndThrow(Items.ENDER_PEARL, yawPitch[0], yawPitch[1]);
            // } else {
            RotationComponent.update(new Rotation(yawPitch[0], yawPitch[1]), 360, 360, 0, 500);
            script.cleanup().addTickStep(1, () -> InventoryUtil.inventorySwapClick2(Items.ENDER_PEARL));
            //}
        }
    }

    private Vector3d getTargetPearlLandingPosition() {
        for (Entity entity : mc.world.getAllEntities())
            if (entity instanceof EnderPearlEntity pearl)
                if (pearl.getShooter() != null && pearl.getShooter().equals(mc.player))
                    return null;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderPearlEntity pearl) {
                Entity shooter = pearl.getShooter();
                if (shooter != null && ((TargetComponent.currentTarget() != null && shooter.equals(TargetComponent.currentTarget()) || !onlyTarget.getValue()))) {
                    Vector3d pearlPosition = pearl.getPositionVec();
                    Vector3d pearlMotion = pearl.getMotion();
                    Vector3d lastPosition;

                    for (int i = 0; i <= 300; i++) {
                        lastPosition = pearlPosition;
                        pearlPosition = pearlPosition.add(pearlMotion);
                        pearlMotion = Projectiles.getInstance().updatePearlMotion(pearl, pearlMotion, pearlPosition);

                        if (Projectiles.getInstance().shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                            return lastPosition;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Vector3d checkTrajectory(float yaw, float pitch) {
        if (Float.isNaN(pitch))
            return null;
        float yawRad = yaw / 180.0f * 3.1415927f;
        float pitchRad = pitch / 180.0f * 3.1415927f;
        double x = mc.player.getPosX() - MathHelper.cos(yawRad) * 0.16f;
        double y = mc.player.getPosY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1;
        double z = mc.player.getPosZ() - MathHelper.sin(yawRad) * 0.16f;
        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        double motionY = -MathHelper.sin(pitchRad) * 0.4f;
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        motionX *= 1.5f;
        motionY *= 1.5f;
        motionZ *= 1.5f;
        if (!mc.player.isOnGround()) motionY += mc.player.getMotion().getY();
        return traceTrajectory(new Vector3d(x, y, z), new Vector3d(motionX, motionY, motionZ));
    }

    private Vector3d traceTrajectory(Vector3d pearlPos, Vector3d motion) {
        Vector3d lastPos;
        for (int i = 0; i <= 300; i++) {
            lastPos = pearlPos;
            pearlPos = pearlPos.add(motion);
            motion = Projectiles.getInstance().updatePearlMotion(new EnderPearlEntity(mc.world, 0, 0, 0), motion, pearlPos);

            if (Projectiles.getInstance().shouldEntityHit(pearlPos, lastPos) || pearlPos.y <= 0) {
                return pearlPos;
            }
        }
        return null;
    }
}
