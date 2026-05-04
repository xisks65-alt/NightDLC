package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "BowAimbot", category = Category.COMBAT)
public class BowAimbot extends Module implements IMinecraft {

    // Настройки модуля
    public final SliderSetting range = new SliderSetting(this, "Дистанция", 50f, 5f, 100f, 1f);
    public final SliderSetting fov = new SliderSetting(this, "FOV", 90f, 10f, 180f, 1f);
    public final SliderSetting speed = new SliderSetting(this, "Скорость наведения", 5f, 1f, 20f, 0.1f);
    public final BooleanSetting predictMovement = new BooleanSetting(this, "Предсказание движения", true);
    public final BooleanSetting onlyPlayers = new BooleanSetting(this, "Только игроки", true);
    public final BooleanSetting throughWalls = new BooleanSetting(this, "Сквозь стены", false);

    private Entity target = null;
    private float targetYaw = 0f;
    private float targetPitch = 0f;
    private boolean isAiming = false;

    @Override
    public void onEnable() {
        target = null;
        isAiming = false;
    }

    @Override
    public void onDisable() {
        target = null;
        isAiming = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Проверяем, держим ли мы лук или трезубец
        if (!isHoldingBowOrTrident()) {
            target = null;
            isAiming = false;
            return;
        }

        // Ищем цель
        findTarget();

        // Если цель найдена, рассчитываем углы и поворачиваем
        if (target != null) {
            calculateAngles();
            rotateToTarget();
        }
    }

    private void rotateToTarget() {
        if (!isAiming || target == null) return;

        // Плавно поворачиваем к цели
        float currentYaw = mc.player.rotationYaw;
        float currentPitch = mc.player.rotationPitch;

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = MathHelper.wrapDegrees(targetPitch - currentPitch);

        float rotationSpeed = speed.getValue() * 0.1f;

        float newYaw = currentYaw + deltaYaw * rotationSpeed;
        float newPitch = currentPitch + deltaPitch * rotationSpeed;

        // Устанавливаем новые углы поворота
        mc.player.rotationYaw = newYaw;
        mc.player.rotationPitch = MathHelper.clamp(newPitch, -90f, 90f);
    }

    private boolean isHoldingBowOrTrident() {
        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();

        return (mainHand.getItem() == Items.BOW || mainHand.getItem() == Items.TRIDENT) ||
                (offHand.getItem() == Items.BOW || offHand.getItem() == Items.TRIDENT);
    }

    private void findTarget() {
        List<Entity> entities = mc.world.getEntitiesWithinAABB(Entity.class,
                mc.player.getBoundingBox().grow(range.getValue()));

        List<Entity> validTargets = entities.stream()
                .filter(this::isValidTarget)
                .filter(this::isInRange)
                .filter(this::canSeeTarget)
                .filter(this::isInVerticalRange) // Ограничение по вертикали -90° до +90°
                .sorted(Comparator.comparingDouble(this::getFOVDistance)) // Сортировка по FOV и расстоянию
                .collect(Collectors.toList());

        if (!validTargets.isEmpty()) {
            target = validTargets.get(0);
        } else {
            target = null;
        }
    }

    private double getFOVDistance(Entity entity) {
        // Приоритет: сначала по FOV, потом по расстоянию
        double fovDistance = getFOVToEntity(entity);
        double actualDistance = mc.player.getDistance(entity);

        // Если цель в FOV - приоритет по расстоянию
        if (fovDistance <= fov.getValue() / 2f) {
            return actualDistance;
        }
        // Если цель вне FOV - штрафуем
        else {
            return actualDistance + 1000; // Большой штраф за выход из FOV
        }
    }

    private double getFOVToEntity(Entity entity) {
        double deltaX = entity.getPosX() - mc.player.getPosX();
        double deltaZ = entity.getPosZ() - mc.player.getPosZ();

        float targetAngle = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;
        float deltaAngle = MathHelper.wrapDegrees(targetAngle - mc.player.rotationYaw);

        return Math.abs(deltaAngle);
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (((LivingEntity) entity).isDead()) return false;

        if (onlyPlayers.getValue()) {
            return entity instanceof PlayerEntity;
        }

        return true;
    }

    private boolean isInRange(Entity entity) {
        return mc.player.getDistance(entity) <= range.getValue();
    }

    private boolean canSeeTarget(Entity entity) {
        if (throughWalls.getValue()) return true;

        return mc.player.canEntityBeSeen(entity);
    }

    private boolean isInVerticalRange(Entity entity) {
        Vector3d playerPos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);

        // Проверяем разные части тела цели
        Vector3d headPos = entity.getPositionVec().add(0, entity.getHeight() - 0.2, 0); // Верх головы
        Vector3d feetPos = entity.getPositionVec(); // Ноги

        // Проверяем угол до головы
        Vector3d deltaHead = headPos.subtract(playerPos);
        double distanceHead = deltaHead.length();
        double pitchHead = Math.toDegrees(Math.asin(-deltaHead.y / distanceHead));

        // Проверяем угол до ног
        Vector3d deltaFeet = feetPos.subtract(playerPos);
        double distanceFeet = deltaFeet.length();
        double pitchFeet = Math.toDegrees(Math.asin(-deltaFeet.y / distanceFeet));

        // Цель валидна если хотя бы одна часть тела в диапазоне -90° до +90°
        boolean headInRange = pitchHead >= -90.0 && pitchHead <= 90.0;
        boolean feetInRange = pitchFeet >= -90.0 && pitchFeet <= 90.0;

        return headInRange || feetInRange;
    }

    private void calculateAngles() {
        if (target == null) return;

        Vector3d targetPos = target.getPositionVec();

        // Предсказание движения
        if (predictMovement.getValue()) {
            targetPos = predictTargetPosition();
        }

        // Простой расчет - прицеливаемся в центр тела
        Vector3d aimPos = targetPos.add(0, target.getHeight() * 0.5, 0);

        Vector3d playerPos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        Vector3d delta = aimPos.subtract(playerPos);

        double distance = delta.length();

        // Рассчитываем углы
        double yaw = Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90;
        double pitch = Math.toDegrees(Math.asin(-delta.y / distance));

        // Простая корректировка для лука
        if (isHoldingBow()) {

        }

        targetYaw = (float) yaw;
        targetPitch = (float) pitch;
        isAiming = true;
    }

    private Vector3d predictTargetPosition() {
        if (target == null) return target.getPositionVec();

        Vector3d currentPos = target.getPositionVec();
        Vector3d velocity = target.getMotion();

        // Время полета снаряда (приблизительно)
        double distance = mc.player.getDistance(target);
        double projectileSpeed = isHoldingBow() ? 3.0 : 2.5; // Скорость стрелы/трезубца
        double flightTime = distance / projectileSpeed;

        // Предсказываем позицию через время полета
        return currentPos.add(velocity.scale(flightTime));
    }

    private boolean isHoldingBow() {
        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();

        return mainHand.getItem() == Items.BOW || offHand.getItem() == Items.BOW;
    }

    private float getBowDrawPower() {
        if (!isHoldingBow()) return 0f;

        // Получаем силу натягивания лука (0.0 - 1.0)
        ItemStack bowStack = mc.player.getHeldItemMainhand();
        if (bowStack.getItem() != Items.BOW) {
            bowStack = mc.player.getHeldItemOffhand();
        }

        // Проверяем, натягивается ли лук
        if (mc.player.isHandActive() && bowStack.getItem() == Items.BOW) {
            // Получаем время натягивания (в тиках)
            int useTime = mc.player.getItemInUseMaxCount();
            int maxUseTime = bowStack.getUseDuration();

            // Рассчитываем силу натягивания (0.0 - 1.0)
            float drawPower = Math.min(1.0f, (float) useTime / maxUseTime);

            // Минимальная сила для выстрела
            if (drawPower < 0.1f) drawPower = 0.1f;

            return drawPower;
        }

        return 1.0f; // Полная сила если не натягивается
    }

}
