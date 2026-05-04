package dev.wh1tew1ndows.client.managers.component.impl.target;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AntiBot;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class TargetComponent extends Component {
    private static final List<LivingEntity> entityList = Collections.synchronizedList(new ArrayList<>());
    // Переиспользуемые списки — не создаём new каждый кадр
    private static final List<Entity> reusableEntityResult = new ArrayList<>(64);
    private static final List<LivingEntity> reusableLivingResult = new ArrayList<>(64);
    private static int countLoadedEntities;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Getter
    public static LivingEntity currentTarget;
    @Getter
    private static LivingEntity lastTarget;
    private static double lastRange = 0;

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        entityList.clear();
    }

    public static void updateTargetList() {
        if (mc.world == null) {
            synchronized (entityList) {
                entityList.clear();
            }
            return;
        }
        List<LivingEntity> entities = mc.world.loadedLivingEntityList()
                .stream()
                .filter(entity -> entity != mc.player)
                .toList();

        synchronized (entityList) {
            entityList.clear();
            entityList.addAll(entities);
        }
    }

    public static List<LivingEntity> getTargets(final double range, boolean saveRange) {
        if (countLoadedEntities != mc.world.getCountLoadedEntities()) {
            executorService.execute(TargetComponent::updateTargetList);
            countLoadedEntities = mc.world.getCountLoadedEntities();
        }

        if (currentTarget != null && !isValid(currentTarget)) {
            currentTarget = null;
        }
        if (saveRange) lastRange = range;

        // Переиспользуем список вместо создания нового каждый кадр
        reusableLivingResult.clear();
        synchronized (entityList) {
            double rangeSq = range * range;
            for (LivingEntity entity : entityList) {
                if (ENTITY_FILTER.test(entity) && mc.player.getDistanceSq(entity) <= rangeSq) {
                    reusableLivingResult.add(entity);
                }
            }
        }
        return reusableLivingResult;
    }

    public static List<Entity> getTargets(final double range, Predicate<Entity> predicate, boolean saveRange) {
        if (countLoadedEntities != mc.world.getCountLoadedEntities()) {
            executorService.execute(TargetComponent::updateTargetList);
            countLoadedEntities = mc.world.getCountLoadedEntities();
        }

        if (currentTarget != null && !isValid(currentTarget)) {
            currentTarget = null;
        }
        if (saveRange) lastRange = range;

        // Переиспользуем список вместо создания нового каждый кадр
        reusableEntityResult.clear();
        synchronized (entityList) {
            double rangeSq = range * range;
            for (LivingEntity entity : entityList) {
                if (predicate.test(entity) && mc.player.getDistanceSq(entity) <= rangeSq) {
                    reusableEntityResult.add(entity);
                }
            }
        }
        return reusableEntityResult;
    }

    public static LivingEntity getTarget(final double range) {
        return getTarget(range, true);
    }

    public static LivingEntity getTarget(final double range, boolean saveRange) {
        if (currentTarget == null || !isValid(currentTarget) || mc.player.getDistance(currentTarget) > range) {
            lastTarget = currentTarget = find(range, saveRange).orElse(null);
        }
        return currentTarget;
    }

    public static boolean targetExist() {
        return getTarget(lastRange, false) != null;
    }

    public static void clearTarget() {
        currentTarget = null;
    }

    private static final Predicate<LivingEntity> ENTITY_FILTER = TargetComponent::isValid;

    public static boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity.ticksExisted < 3) return false;
        if (!AttackAura.getInstance().attackforStinka.getValue()) {
            if (!mc.player.canEntityBeSeen(entity)) return false;
        }
        if (entity instanceof PlayerEntity p) {
            if (Zetrix.inst().friendManager().isFriend(p.getName().getString())) {
                return false;
            }
            if (AntiBot.getInstance().isBot(p) && AntiBot.getInstance().isEnabled()) {
                return false;
            }
        }
        if (entity instanceof PlayerEntity && !AttackAura.getInstance().targets.getValue("Игроки")) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !AttackAura.getInstance().targets.getValue("Голые")) {
            return false;
        }
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
            return false;
        }
        if ((entity instanceof MonsterEntity || entity instanceof SlimeEntity || entity instanceof VillagerEntity || entity instanceof AnimalEntity) && !AttackAura.getInstance().targets.getValue("Мобы")) {
            return false;
        }
        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private static Optional<LivingEntity> find(final double range, boolean saveRange) {
        List<LivingEntity> validTargets = getTargets(range, saveRange);
        if (validTargets.isEmpty()) return Optional.empty();

        // Кэшируем позицию глаз и вектор взгляда — не создаём в компараторе
        final Vector3d eyePos = mc.player.getEyePosition(1.0F);
        final Vector3d lookVec = mc.player.getLookVec().normalize();

        LivingEntity best = null;
        double bestDot = -2.0;
        for (LivingEntity e : validTargets) {
            double dx = e.getPosX() - eyePos.x;
            double dy = e.getPosY() + e.getHeight() / 2.0 - eyePos.y;
            double dz = e.getPosZ() - eyePos.z;
            double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (len == 0) continue;
            double dot = (lookVec.x * dx + lookVec.y * dy + lookVec.z * dz) / len;
            if (dot > bestDot) { bestDot = dot; best = e; }
        }
        return Optional.ofNullable(best);
    }

    private static double compareArmor(LivingEntity entity) {
        return (entity instanceof PlayerEntity player) ? -PlayerUtil.getEntityArmor(player) : -entity.getTotalArmorValue();
    }
}
