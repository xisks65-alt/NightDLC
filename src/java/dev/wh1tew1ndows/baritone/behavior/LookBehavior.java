package dev.wh1tew1ndows.baritone.behavior;

import dev.wh1tew1ndows.baritone.Baritone;
import dev.wh1tew1ndows.baritone.api.behavior.ILookBehavior;
import dev.wh1tew1ndows.baritone.api.event.events.PlayerUpdateEvent;
import dev.wh1tew1ndows.baritone.api.event.events.RotationMoveEvent;
import dev.wh1tew1ndows.baritone.api.utils.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public final class LookBehavior extends Behavior implements ILookBehavior {

    private Rotation target;
    private boolean force;
    private float lastYaw;

    public LookBehavior(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void updateTarget(Rotation target, boolean force) {
        // Статичная рандомизация (один раз при установке цели)
        double randYaw = (Math.random() - 0.5) * Baritone.settings().randomLooking.value;
        double randPitch = (Math.random() - 0.5) * Baritone.settings().randomLooking.value;
        this.target = new Rotation(target.getYaw() + (float) randYaw, target.getPitch() + (float) randPitch);
        this.force = force || !Baritone.settings().freeLook.value;
    }

    public void updateTarget(BlockPos pos, boolean force) {
        Rotation rot = calculateTargetRotation(pos);
        updateTarget(rot, force);
    }

    private Rotation calculateTargetRotation(BlockPos pos) {
        Vector3d eye = ctx.playerHead();
        Vector3d blockCenter = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);

        long time = System.currentTimeMillis();

        float oscillY = (float) Math.cos(time / 450.0D) * 0.03F;
        float offsetY = 0.06F * oscillY;

        float oscillZ = (float) Math.cos(time / 500.0D) * 0.03F;
        float offsetZ = 0.06F * oscillZ;

        float oscillX = (float) Math.cos(time / 14000.0D);
        float offsetX = 0.5F * oscillX;

        float yAdjust = MathHelper.clamp((float) (eye.y - blockCenter.y), 0.0F, 1.5F) + offsetX;

        Vector3d perturbed = blockCenter.add(offsetZ, yAdjust, offsetY);
        Vector3d directionVec = perturbed.subtract(eye).normalize();

        float baseYaw = (float) Math.toDegrees(Math.atan2(-directionVec.x, directionVec.z));
        float basePitch = MathHelper.clamp(
                - (float) Math.toDegrees(Math.atan2(directionVec.y, Math.hypot(directionVec.x, directionVec.z))),
                -90.0F, 90.0F
        );

        if (!force) {
            float waveA = (float) Math.cos(time / 200.0D);
            float waveB = (float) Math.sin(time / 300.0D);
            float jitterAmplitude = Baritone.settings().randomLooking.value.floatValue() / 3.0F;
            baseYaw += waveA * jitterAmplitude;
            basePitch += waveB * (jitterAmplitude / 2.0F);
        }

        baseYaw = MathHelper.wrapDegrees(baseYaw);

        return new Rotation(baseYaw, basePitch);
    }

    @Override
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (target == null) return;

        boolean silent = Baritone.settings().antiCheatCompatibility.value && !force;

        boolean canRotate = ctx.player().getCooledAttackStrength(0.0F) >= 1.0F;

        switch (event.getState()) {
            case PRE:
                if (force && canRotate) {
                    applyRotation(target);
                    target = null;
                }
                if (silent && canRotate) {
                    lastYaw = ctx.player().packetYaw;
                    applyRotation(target);
                }
                break;
            case POST:
                if (silent) {
                    ctx.player().packetYaw = lastYaw;
                    target = null;
                }
                break;
            default: break;
        }
    }

    @Override
    public void onPlayerRotationMove(RotationMoveEvent event) {
        if (target != null) {
            boolean canRotate = ctx.player().getCooledAttackStrength(0.0F) >= 1.0F;
            if (canRotate) {
                smoothApplyRotation(target, event);
            } else {

                float attackStrength = ctx.player().getCooledAttackStrength(0.0F);
                if (attackStrength >= 0.5F) {
                    smoothApplyRotation(target, event, 0.5F);  // Замедленный режим
                }

            }
            if (!Baritone.settings().antiCheatCompatibility.value && event.getType() == RotationMoveEvent.Type.MOTION_UPDATE && !force) {
                target = null;
            }
        }
    }

    private void applyRotation(Rotation rot) {
        float yaw = MathHelper.wrapDegrees(rot.getYaw());
        float pitch = MathHelper.clamp(rot.getPitch(), -90.0F, 90.0F);
        ctx.player().rotationYaw = yaw;
        ctx.player().rotationYawHead = yaw;
        ctx.player().renderYawOffset = yaw;
        ctx.player().packetYaw = yaw;
        ctx.player().rotationPitch = pitch;
        ctx.player().packetPitch = pitch;
    }

    private void smoothApplyRotation(Rotation rot, RotationMoveEvent event) {
        smoothApplyRotation(rot, event, 1.0F);  // Нормальная скорость
    }

    private void smoothApplyRotation(Rotation rot, RotationMoveEvent event, float speedMultiplier) {

        float smoothedYaw = calculateSmoothedYaw(rot.getYaw(), speedMultiplier);
        float smoothedPitch = calculateSmoothedPitch(rot.getPitch(), speedMultiplier);

        ctx.player().rotationYaw = smoothedYaw;
        ctx.player().rotationYawHead = smoothedYaw;
        ctx.player().renderYawOffset = smoothedYaw;
        ctx.player().packetYaw = smoothedYaw;
        ctx.player().rotationPitch = smoothedPitch;
        ctx.player().packetPitch = smoothedPitch;

        event.setYaw(smoothedYaw);
    }

    private float calculateSmoothedYaw(float targetYaw, float speedMultiplier) {
        float currentYaw = ctx.player().rotationYaw;
        float deltaYaw = MathHelper.wrapDegrees(targetYaw - currentYaw);

        if (Math.abs(deltaYaw) > 180.0F) {
            deltaYaw = Math.signum(deltaYaw) * (360.0F - Math.abs(deltaYaw));
        }

        float smoothingFactor = Math.min(0.2F * speedMultiplier, Math.abs(deltaYaw) / 90.0F);  // 0.2-1.0
        return currentYaw + deltaYaw * smoothingFactor;
    }

    private float calculateSmoothedPitch(float targetPitch, float speedMultiplier) {
        float currentPitch = ctx.player().rotationPitch;
        float deltaPitch = targetPitch - currentPitch;
        float pitchSmoothing = Math.min(0.15F * speedMultiplier, Math.abs(deltaPitch) / 45.0F);
        float pitch = currentPitch + deltaPitch * pitchSmoothing;
        return MathHelper.clamp(pitch, -90.0F, 90.0F);
    }
}