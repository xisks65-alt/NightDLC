package dev.wh1tew1ndows.client.managers.component.impl.rotation;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.managers.module.legacyModule.CrystalAura;
import dev.wh1tew1ndows.client.managers.module.impl.movement.TargetStrafe;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.rotation.AuraUtil;
import dev.wh1tew1ndows.client.utils.rotation.SensUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Getter
@Setter
@Accessors(fluent = true)
public class RotationComponent extends Component {
    public static RotationComponent getInstance() {
        return Instance.getComponent(RotationComponent.class);
    }

    static float tickrs;

    public static float turnTick() {
        return tickrs;
    }

    private RotationTask currentTask = RotationTask.IDLE;
    private float currentYawSpeed;
    private float currentPitchSpeed;
    private float currentYawReturnSpeed;
    private float currentPitchReturnSpeed;
    private int currentPriority;
    private int currentTimeout;
    private int idleTicks;
    private Rotation targetRotation;

    public static void resetParentTimeout() {
        final RotationComponent instance = RotationComponent.getInstance();
        instance.currentTimeout = 0;
        instance.currentTask = RotationTask.IDLE;
        instance.currentPriority = 0;
        FreeLookComponent.setActive(false);
    }

    @EventHandler
    public void onEvent(MoveInputEvent event) {
        if (!mc.player.isElytraFlying()) {
            if (AttackAura.getInstance().modeSetting.is("Таргетированная") && !mc.player.isElytraFlying() && !Zetrix.inst().moduleManager().get(TargetStrafe.class).isEnabled()) {
                if (AttackAura.getInstance().target != null) {
                    Vector3d direction = AttackAura.getInstance().target.getPositionVec().add(Mathf.randomValue(-0.3F, 0.3F), 0, Mathf.randomValue(-0.3F, 0.3F)).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks())).normalize();
                    float targetYaw = (float) wrapDegrees(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
                    MoveUtil.targetMove(event, mc.player.rotationYaw, targetYaw);
                }
            }
            if (AttackAura.getInstance().target != null && AttackAura.getInstance().modeSetting.is("Преследования")) {
                MoveUtil.moveToPosition(event,
                        AttackAura.getInstance().target.getPositionVec().add(Mathf.randomValue(-0.05F, 0.05F), 0, Mathf.randomValue(-0.05F, 0.05F)), mc.player.rotationYaw);
            }

            if (AttackAura.getInstance().target != null && Zetrix.inst().moduleManager().get(TargetStrafe.class).isEnabled()) {
                if (AuraUtil.getStrictDistance(AttackAura.getInstance().target) <= AttackAura.getInstance().attackDistance())
                    event.setJump(true);
                MoveUtil.moveToPosition(event, AttackAura.getInstance().target.getPositionVec().add(Zetrix.inst().moduleManager().get(TargetStrafe.class).rageByte.getValue(), 0, Zetrix.inst().moduleManager().get(TargetStrafe.class).rageByte.getValue()), mc.player.rotationYaw);
            }
            if (isRotating() && AttackAura.getInstance().modeSetting.is("Свободная") && !Zetrix.inst().moduleManager().get(TargetStrafe.class).isEnabled() || Zetrix.inst().moduleManager().get(CrystalAura.class).isEnabled()) {
                MoveUtil.fixMovement(event, MathHelper.wrapDegrees(mc.gameRenderer.getActiveRenderInfo().getYaw()) + (mc.gameSettings.getPointOfView().thirdPersonFront() ? 180 : 0));
            }
        }
    }

    private void resetRotation() {
        Rotation targetRotation = new Rotation(FreeLookComponent.getFreeYaw(), FreeLookComponent.getFreePitch());
        if (updateRotation(targetRotation, currentYawReturnSpeed(), currentPitchReturnSpeed())) {
            stopRotation();
        }
    }

    private final TimerUtil turn = new TimerUtil();

    @EventHandler
    public void onEvent(UpdateEvent event) {
        tickrs = (float) Mathf.randomWithUpdate(19, 113, 100, turn);
        if (currentTask().equals(RotationTask.AIM) && idleTicks() > currentTimeout()) {
            currentTask(RotationTask.RESET);
        }

        if (currentTask().equals(RotationTask.RESET)) {
            resetRotation();
        }
        idleTicks++;
    }

    public static void update(Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation) {
        final RotationComponent instance = RotationComponent.getInstance();
        if (instance.currentPriority() > priority) {
            return;
        }

        if (instance.currentTask().equals(RotationTask.IDLE) && !clientRotation) {
            FreeLookComponent.setActive(true);
        }

        instance.currentYawSpeed(yawSpeed);
        instance.currentPitchSpeed(pitchSpeed);
        instance.currentYawReturnSpeed(yawReturnSpeed);
        instance.currentPitchReturnSpeed(pitchReturnSpeed);
        instance.currentTimeout(timeout);
        instance.currentPriority(priority);
        instance.currentTask(RotationTask.AIM);
        instance.targetRotation(target);

        instance.updateRotation(target, yawSpeed, pitchSpeed);
    }

    public static void update(Rotation targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
        update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
    }

    private boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed) {
        if (mc.player == null) return false;

        Rotation currentRotation = new Rotation(mc.player);
        float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
        float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();

        float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
        float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);

        mc.player.rotationYaw += SensUtil.getSens(MathHelper.clamp(yawDelta, -clampedYaw, clampedYaw));
        mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch + SensUtil.getSens(MathHelper.clamp(pitchDelta, -clampedPitch, clampedPitch)), -90F, 90F);

        idleTicks(0);
        return new Rotation(mc.player).getDelta(targetRotation) < 1F;
    }

    public void stopRotation() {
        currentTask(RotationTask.IDLE);
        RotationComponent rotationComponent = currentPriority(0);
        FreeLookComponent.setActive(false);

    }

    public boolean isRotating() {
        return !currentTask.equals(RotationTask.IDLE);
    }

    public enum RotationTask {
        AIM,
        RESET,
        IDLE
    }
}
