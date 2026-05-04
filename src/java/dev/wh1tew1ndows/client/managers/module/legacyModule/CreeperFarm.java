package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.annotations.Beta;
import dev.wh1tew1ndows.client.api.annotations.PVE;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Beta
@PVE
@ModuleInfo(name = "CreeperFarm", category = Category.MISC, desc = "Автоматическая ферма криперов для получения пороха")
public class CreeperFarm extends Module {
    private Entity target;
    private final TimerUtil2 timerUtil = new TimerUtil2();
    private final TimerUtil2 hubTimer = new TimerUtil2();
    private Vector2f rotateVector = new Vector2f(0, 0);
    private boolean needRotate = false;
    private boolean isRunningAway = false;
    private Vector3d runAwayPosition = null;

    private Vector3d lastPosition;
    private long lastMoveTime;
    private boolean isStuck = false;
    private boolean strafingRight = false;
    private final boolean sentHubCommand = false;
    private final boolean hasExploded = false;

    // --- параметры обхода/уклонения ---
    private static final double SEPARATION_DIST = 4.0; // сколько держать от крипера
    private static final double RUN_AWAY_DIST = 4.0; // на сколько отскакивать точкой
    private final float obstacleTurnStep = 14.0f;            // шаг «чуть-чуть повернуть»
    private final double obstacleRayDist = 1.35;             // длина луча вперёд
    private final double obstacleSideOffset = 0.40;          // смещение боковых лучей от центра
    private final long obstacleCooldownMs = 220L;            // кулдаун подруливания
    private long lastObstacleAdjust = 0L;

    // лёгкий «режим обхода» стены
    private boolean detouring = false;
    private int detourDir = 0;                         // -1 = влево, +1 = вправо
    private long detourUntil = 0L;
    private final long detourMinTime = 200L;                 // хотя бы столько держать детур
    private final long detourMaxTime = 900L;                 // максимум детур

    @EventHandler
    public void onEvent(MoveInputEvent event) {
        if (target != null && mc.player != null) {
            MoveUtil.fixMovementES(event, rotateVector.x);
        }
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (isRunningAway) {
            runFromCreeper();
        } else {
            updateTarget();
            if (target != null) {
                processRotationLogic();
                moveToTarget();
            } else {
                timerUtil.setLastMS(0);
                reset();
            }
        }

        if (target instanceof CreeperEntity && !isRunningAway && !hasExploded) {
            if (mc.player.getDistance(target) <= 3.0) {
                attack((CreeperEntity) target);
            }
        }
    }

    @EventHandler
    public void event(MotionEvent event) {
        if (target == null && !isRunningAway) return;
        setPlayerRotation(event);
    }

    private void moveToTarget() {
        if (target == null || mc.player == null) return;

        double deltaX = target.getPosX() - mc.player.getPosX();
        double deltaZ = target.getPosZ() - mc.player.getPosZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // если цель — предмет, просто идём
        if (target instanceof ItemEntity) {
            moveTowards(distance);
            return;
        }

        // если цель — крипер
        if (target instanceof CreeperEntity creeper) {
            // если слишком близко — отходим на 4 блока
            if (distance < SEPARATION_DIST) {
                isRunningAway = true;
                runAwayPosition = getRunAwayPosition(creeper);
                return;
            }

            // если «завёлся» — тоже отходим на 4
            if (creeper.getCreeperState() > 0) {
                isRunningAway = true;
                runAwayPosition = getRunAwayPosition(creeper);
                return;
            }

            // иначе можем атаковать в удобной зоне
            if (distance <= 6.0) {
                attack(creeper);
            }
        }

        moveTowards(distance);
    }

    private void moveTowards(double distance) {
        if (distance > 1.5) {
            checkStuck();

            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
            mc.gameSettings.keyBindForward.setPressed(true);
            mc.gameSettings.keyBindJump.setPressed(true); // перепрыгивать мелкие бортики

            navigateWithObstacleLogic();

        } else {
            resetKeys();
        }
    }

    private void runFromCreeper() {
        if (runAwayPosition == null || mc.player == null) {
            isRunningAway = false;
            return;
        }

        double deltaX = runAwayPosition.x - mc.player.getPosX();
        double deltaZ = runAwayPosition.z - mc.player.getPosZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        rotateVector = new Vector2f(smoothRotation(mc.player.rotationYaw, yaw, 150), mc.player.rotationPitch);
        needRotate = true;

        mc.player.setSprinting(true);
        mc.gameSettings.keyBindJump.setPressed(true);

        // во время отхода тоже смотрим препятствия
        checkStuck();
        navigateWithObstacleLogic();

        // как только оторвались на 4+ блока — стоп отход
        if (target instanceof CreeperEntity) {
            double d = mc.player.getDistance(target);
            if (d >= SEPARATION_DIST - 0.05) {
                isRunningAway = false;
                runAwayPosition = null;
                // не сбрасываю сразу клавиши, пусть инерционно идёт, но выключу детур
                detouring = false;
                mc.gameSettings.keyBindLeft.setPressed(false);
                mc.gameSettings.keyBindRight.setPressed(false);
            }
        }

        mc.gameSettings.keyBindForward.setPressed(true);
    }

    /**
     * Главная логика: обходим/разворачиваемся при упоре.
     */
    private void navigateWithObstacleLogic() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        boolean forwardPressed = mc.gameSettings.keyBindForward.isPressed();
        if (!forwardPressed) return;

        // Лучи вперёд (ноги/грудь) и по бокам (лево/право) — возвращают дистанцию до столкновения
        double frontLow = rayDistAhead(obstacleRayDist, 0.0, 0.10);
        double frontHigh = rayDistAhead(obstacleRayDist, 0.0, 0.95);
        double leftLow = rayDistAhead(obstacleRayDist, -obstacleSideOffset, 0.10);
        double leftHigh = rayDistAhead(obstacleRayDist, -obstacleSideOffset, 0.95);
        double rightLow = rayDistAhead(obstacleRayDist, +obstacleSideOffset, 0.10);
        double rightHigh = rayDistAhead(obstacleRayDist, +obstacleSideOffset, 0.95);

        boolean frontBlocked = isBlocked(frontLow) || isBlocked(frontHigh);
        boolean leftBlocked = isBlocked(leftLow) || isBlocked(leftHigh);
        boolean rightBlocked = isBlocked(rightLow) || isBlocked(rightHigh);

        // сильный зажим: спереди и по бокам тесно -> разворот
        if ((frontBlocked && leftBlocked && rightBlocked) || (isStuck && frontBlocked)) {
            if (now - lastObstacleAdjust >= obstacleCooldownMs) {
                float newYaw = mc.player.rotationYaw + 180f;
                rotateVector = new Vector2f(newYaw, rotateVector.y);
                needRotate = true;
                // отпустим боковые, сменим сторону на всякий
                mc.gameSettings.keyBindLeft.setPressed(false);
                mc.gameSettings.keyBindRight.setPressed(false);
                strafingRight = !strafingRight;
                detouring = false;
                lastObstacleAdjust = now;
            }
            return;
        }

        // обычный случай: спереди упёрлись — выбрать сторону с большим «зазором»
        if (frontBlocked) {
            if (now - lastObstacleAdjust >= obstacleCooldownMs) {
                double leftClear = clearanceScore(leftLow, leftHigh);
                double rightClear = clearanceScore(rightLow, rightHigh);

                int dir = (rightClear > leftClear) ? +1 : -1; // +1 = вправо, -1 = влево
                detourDir = dir;
                detouring = true;
                detourUntil = now + (detourMinTime + (long) (Math.random() * (detourMaxTime - detourMinTime)));

                float yawAdjust = dir * (obstacleTurnStep * 1.8f);
                rotateVector = new Vector2f(mc.player.rotationYaw + yawAdjust, rotateVector.y);
                needRotate = true;

                if (dir > 0) {
                    mc.gameSettings.keyBindRight.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    strafingRight = true;
                } else {
                    mc.gameSettings.keyBindLeft.setPressed(true);
                    mc.gameSettings.keyBindRight.setPressed(false);
                    strafingRight = false;
                }

                lastObstacleAdjust = now;
            }
        } else {
            // если шли в детуре, но фронт свободен и время прошло — выходим
            if (detouring) {
                if (now >= detourUntil || (!leftBlocked && !rightBlocked)) {
                    detouring = false;
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            } else {
                // если формально «застряли», мягко повернём и подстрафуем
                if (isStuck && now - lastObstacleAdjust >= obstacleCooldownMs) {
                    int dir = strafingRight ? +1 : -1;
                    rotateVector = new Vector2f(mc.player.rotationYaw + dir * obstacleTurnStep, rotateVector.y);
                    needRotate = true;

                    if (dir > 0) {
                        mc.gameSettings.keyBindRight.setPressed(true);
                        mc.gameSettings.keyBindLeft.setPressed(false);
                    } else {
                        mc.gameSettings.keyBindLeft.setPressed(true);
                        mc.gameSettings.keyBindRight.setPressed(false);
                    }
                    strafingRight = !strafingRight;
                    lastObstacleAdjust = now;
                }
            }
        }
    }

    /**
     * дистанция до препятствия; MISS => вернём максимально возможную (dist), HIT => фактическую
     */
    private double rayDistAhead(double dist, double offsetXz, double yOff) {
        if (mc.player == null) return dist;

        float yawRad = (float) Math.toRadians(mc.player.rotationYaw);
        double dirX = -MathHelper.sin(yawRad);
        double dirZ = MathHelper.cos(yawRad);

        double perpX = -dirZ; // перпендикуляр для бокового смещения
        double perpZ = dirX;

        Vector3d start = new Vector3d(
                mc.player.getPosX() + perpX * offsetXz,
                mc.player.getPosY() + yOff,
                mc.player.getPosZ() + perpZ * offsetXz
        );
        Vector3d end = start.add(dirX * dist, 0.0, dirZ * dist);

        RayTraceResult res = mc.world.rayTraceBlocks(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        ));

        if (res == null || res.getType() == RayTraceResult.Type.MISS) return dist;

        Vector3d hit = res.getHitVec();

        if (hit == null) return 0.0;
        return start.distanceTo(hit);
    }

    private boolean isBlocked(double rayDist) {
        // считаем «близким» препятствием, если луч упёрся раньше 60% своей длины
        return rayDist < obstacleRayDist * 0.6;
    }

    private double clearanceScore(double low, double high) {
        // чем больше расстояние — тем «свободнее» сторона
        return low + high;
    }

    private void checkStuck() {
        if (mc.player == null) return;

        Vector3d currentPos = mc.player.getPositionVec();
        long now = System.currentTimeMillis();

        if (lastPosition != null) {
            double dx = currentPos.x - lastPosition.x;
            double dz = currentPos.z - lastPosition.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            long dt = now - lastMoveTime;

            boolean forwardPressed = mc.gameSettings.keyBindForward.isPressed();
            isStuck = forwardPressed && dt >= 320L && horizontal < 0.02;

            if (dt >= 320L) {
                lastPosition = currentPos;
                lastMoveTime = now;
            }
        } else {
            lastPosition = currentPos;
            lastMoveTime = now;
            isStuck = false;
        }
    }

    private Vector3d getRunAwayPosition(CreeperEntity creeper) {
        // вектор от крипера к игроку → нормируем → 4 блока
        double dx = mc.player.getPosX() - creeper.getPosX();
        double dz = mc.player.getPosZ() - creeper.getPosZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-3) len = 1e-3;
        dx /= len;
        dz /= len;

        double targetX = creeper.getPosX() + dx * RUN_AWAY_DIST;
        double targetZ = creeper.getPosZ() + dz * RUN_AWAY_DIST;

        return new Vector3d(targetX, mc.player.getPosY(), targetZ);
    }

    private void updateTarget() {
        if (isRunningAway) return;

        Entity bestTarget = null;
        double bestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof CreeperEntity) && !(entity instanceof ItemEntity)) continue;
            if (entity instanceof ItemEntity && !((ItemEntity) entity).getItem().getItem().equals(Items.GUNPOWDER))
                continue;

            double distance = mc.player.getDistance(entity);

            if (entity instanceof CreeperEntity creeper) {
                double heightDifference = creeper.getPosY() - mc.player.getPosY();
                if (heightDifference < -3 || heightDifference > 3) continue;
                if (!isPathClear(creeper)) continue;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = entity;
            }
        }

        target = bestTarget;
    }

    private boolean isPathClear(Entity target) {
        Vector3d playerPos = new Vector3d(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ());
        Vector3d targetPos = new Vector3d(target.getPosX(), target.getPosY() + target.getHeight() * 0.5, target.getPosZ());

        RayTraceResult result = mc.world.rayTraceBlocks(new RayTraceContext(
                playerPos, targetPos,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        ));

        return result == null || result.getType() == RayTraceResult.Type.MISS;
    }

    private void processRotationLogic() {
        if (target == null || mc.player == null) return;

        Vector3d vec = target.getPositionVec()
                .add(0, target.getHeight() * 0.5, 0)
                .subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()));

        double distance = vec.length();
        vec = vec.normalize();

        float targetYaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
        float targetPitch = (float) MathHelper.clamp(
                -Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z))),
                -90, 90
        );

        rotateVector = new Vector2f(
                smoothRotation(mc.player.rotationYaw, targetYaw, 150),
                smoothRotation(mc.player.rotationPitch, targetPitch, 150)
        );
        needRotate = true;

        if (target instanceof CreeperEntity && distance < 3.0) {
            attack((CreeperEntity) target);
        }
    }

    private void setPlayerRotation(MotionEvent event) {
        if (!needRotate) return;

        event.setYaw(rotateVector.x);
        event.setPitch(rotateVector.y);

        mc.player.rotationYaw = rotateVector.x;
        mc.player.rotationPitch = rotateVector.y;
        mc.player.rotationYawHead = rotateVector.x;
        mc.player.renderYawOffset = rotateVector.x;
        mc.player.rotationPitchHead = rotateVector.y;
    }

    private float smoothRotation(float current, float target, float maxSpeed) {
        float speed = Math.min(maxSpeed, Math.abs(target - current) * 2);
        float delta = MathHelper.wrapDegrees(target - current);
        return MathHelper.wrapDegrees(current + MathHelper.clamp(delta, -speed, speed));
    }

    private void attack(CreeperEntity creeper) {
        // если крипер «шипит» — сначала держим дистанцию
        if (creeper.getCreeperState() > 0) {
            isRunningAway = true;
            runAwayPosition = getRunAwayPosition(creeper);
            return;
        }

        if (timerUtil.hasTimeElapsed()) {
            mc.playerController.attackEntity(mc.player, creeper);
            mc.player.swingArm(Hand.MAIN_HAND);
            timerUtil.setLastMS(505);
        }
    }

    private void resetKeys() {
        mc.gameSettings.keyBindForward.setPressed(false);
        mc.gameSettings.keyBindRight.setPressed(false);
        mc.gameSettings.keyBindLeft.setPressed(false);
        mc.gameSettings.keyBindJump.setPressed(false);
        mc.player.setSprinting(false);
    }

    private void reset() {
        if (mc.player != null) {
            resetKeys();
        }
        needRotate = false;
        detouring = false;
        rotateVector = new Vector2f(
                mc.player != null ? mc.player.rotationYaw : 0,
                mc.player != null ? mc.player.rotationPitch : 0
        );
    }

    private boolean isChangingItem;
    private final int originalSlot = -1;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            reset();
            target = null;
            isRunningAway = false;
            runAwayPosition = null;
            lastPosition = null;
            isStuck = false;
            lastObstacleAdjust = 0L;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        timerUtil.setLastMS(0);
        target = null;
        isRunningAway = false;
        runAwayPosition = null;
        lastPosition = null;
        isStuck = false;
    }
}
