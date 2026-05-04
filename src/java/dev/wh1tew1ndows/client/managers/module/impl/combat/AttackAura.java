package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;
import dev.wh1tew1ndows.client.managers.events.other.GameUpdateEvent;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.neuro.NeuroManager;
import dev.wh1tew1ndows.client.managers.events.player.ActionEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.combat.aura.UAura;
import dev.wh1tew1ndows.client.managers.module.legacyModule.ElytraTarget;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Interpolator;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.rotation.AttackUtil;
import dev.wh1tew1ndows.client.utils.rotation.AuraUtil;
import dev.wh1tew1ndows.client.utils.rotation.RayTraceUtil;
import dev.wh1tew1ndows.client.utils.rotation.RotationUtil;
import dev.wh1tew1ndows.client.utils.rotation.SensUtil;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "AttackAura", category = Category.COMBAT, desc = "Автоматическая атака ближайших врагов")
public class AttackAura extends Module {

    private final SliderSetting attackRange = new SliderSetting(this, "Радиус атаки", 3.0F, 3.0F, 6.0F, 0.1F);
    private final SliderSetting preRange = new SliderSetting(this, "Радиус обнаружения", 1.0F, 0.0F, 5.0F, 0.1F);
    public final ModeSetting componentMode = new ModeSetting(this, "Тип навидения", "SpookyAnka", "Нейро");

    // Настройка выбора нейро-модели — динамический список из NeuroManager
    public final ModeSetting neuroModelSetting = new ModeSetting(this, "Нейро модель", "Нет")
            .setVisible(() -> componentMode.is("Нейро"));

    {
        // Устанавливаем динамический список после инициализации поля
        neuroModelSetting.setDynamicValues(() -> NeuroManager.getInstance().getModelNames());
    }

    public final ModeSetting sortMode = new ModeSetting(this, "Сортировка по", "Дистанции", "Здоровью");
    public final MultiBooleanSetting targets = new MultiBooleanSetting(this, "Кого атаковать",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Голые", true),
            BooleanSetting.of("Мобы", false),
            BooleanSetting.of("Друзей", false));

    public final ModeSetting critmode = new ModeSetting(this, "Тип удара", "Умные криты", "Только криты");
    public final ModeSetting modeSetting = new ModeSetting(this, "Коррекция движения", "Сильная", "Свободная");

    private final MultiBooleanSetting checkattack = new MultiBooleanSetting(this, "Не бить если",
            BooleanSetting.of("Используеш еду", true),
            BooleanSetting.of("Открыт контейнер", false));

    public BooleanSetting attackforStinka = new BooleanSetting(this, "Бить через блоки", false);
    public BooleanSetting onlySworld = new BooleanSetting(this, "Бить только с оружием", false);
    public BooleanSetting breacShild = new BooleanSetting(this, "Автоматичиски ломать щит", false);
    public BooleanSetting shildblock = new BooleanSetting(this, "Отжимать щит при ударе", false);
    public ModeSetting targetType = new ModeSetting(this, "Отображения цели", "Призраки", "Духи", "Не отображать");
    public final ModeSetting stopSprint = new ModeSetting(this, "Сброс спринта", "Выкл", "Spooky");

    private final StopWatch stopWatch = new StopWatch();

    @Getter public LivingEntity target = null;
    private LivingEntity prevTarget = null;
    private boolean canCrit;
    private int count;
    private int p;

    // Нейро: lerp состояние для плавной ротации
    private float neuroLerpYaw = 0;
    private float neuroLerpPitch = 0;
    private boolean neuroLerpInit = false;

    public static final float CRIT_COOLDOWN_THRESHOLD = 0.5F;
    private boolean spookySprintReset = false;

    // SpookyAnka multipoint & distance-based rotation
    private int spookyPointTimer = 0;
    private Vector3d currentSpookyPoint = Vector3d.ZERO;

    // MX / SpookyTime humanized rotation state
    // Mouse physics simulator — emulates real mouse movement with inertia, friction and noise
    private float mouseVelX = 0;
    private float mouseVelY = 0;
    private float aimErrorYaw = 0;
    private float aimErrorPitch = 0;
    private int aimErrorTicks = 0;
    private int coastTicks = 0;
    private int twitchTicks = 0;
    private float twitchYaw = 0;
    private float twitchPitch = 0;
    private float lastDeltaYaw = 9999;
    private float lastDeltaPitch = 9999;
    private int sameDeltaCount = 0;
    private int overshootTicks = 0;
    private float overshootYaw = 0;
    private float overshootPitch = 0;
    private float currentStiffness = 0.04f;
    private float currentFriction = 0.78f;
    private int paramRefreshTicks = 0;
    private int breakLockTicks = 0;

    public static AttackAura getInstance() { return Instance.get(AttackAura.class); }

    @Override
    public void toggle() { super.toggle(); reset(); }

    @EventHandler
    public void onPacketEvent(PacketEvent event) {
        if (!event.isSend()) return;
        if (Criticals.getInstance().isEnabled()) {
            if (event.getPacket() instanceof CHeldItemChangePacket)
                stopWatch.reset();
            else if (event.getPacket() instanceof CAnimateHandPacket)
                stopWatch.reset();
        } else if (event.getPacket() instanceof CHeldItemChangePacket) {
            stopWatch.reset();
        } else if (event.getPacket() instanceof CAnimateHandPacket) {
            stopWatch.reset();
        }
    }

    @EventHandler public void onWorldLoad(WorldLoadEvent e) { reset(); }
    @EventHandler public void onWorldChange(WorldChangeEvent e) { reset(); }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!mc.player.isAlive()) { toggle(); return; }
        if (target == null || !isValidTarget(target)) updateTarget();
        if (target != null) {
            TargetComponent.currentTarget = target;
            if (prevTarget != target) {
                neuroLerpInit = false; // сброс lerp при смене цели
                currentSpookyPoint = Vector3d.ZERO;
                spookyPointTimer = 0;
                mouseVelX = 0;
                mouseVelY = 0;
                aimErrorYaw = 0;
                aimErrorPitch = 0;
                aimErrorTicks = 0;
                coastTicks = 0;
                twitchTicks = 0;
                lastDeltaYaw = 9999;
                lastDeltaPitch = 9999;
            }
            prevTarget = target;
        }
        if (target != null && mc.player != null && mc.world != null) {
            if (!checkReturn() && !shieldBreaker()) updateAttack();
        } else {
            reset();
        }
    }

    @EventHandler
    public void onGameUpdate(GameUpdateEvent event) {
        if (target != null && mc.player != null && mc.world != null) updateRotation();
        else reset();
    }

    private void updateRotation() {
        ElytraTarget elytraTarget = Zetrix.inst().moduleManager().get(ElytraTarget.class);
        if (mc.player.isElytraFlying() && elytraTarget != null && elytraTarget.isEnabled()) return;
        String mode = componentMode.getValue();
        switch (mode) {
            case "Нейро": updateNeuro(); break;
            case "SpookyAnka": updateSpookyAnka(); break;
        }
    }

    private void updateSpookyAnka() { updateHumanizedRotation(true); }

    /**
     * Улучшенный физический симулятор мыши для обхода античитов.
     * Ключевые изменения:
     * — Прямое применение через SensUtil (GCD), без RotationComponent
     * — Miss ticks: иногда пропускаем обновление
     * — Break lock: редкий микро-флик ломает aimlock-паттерн
     * — Нет coast вблизи цели (убран aimlock)
     * — Повышенный шум и underaim
     * — Более консервативная скорость
     */
    private void updateHumanizedRotation(boolean aggressive) {
        if (target == null) return;

        // 1. Miss tick — иногда не обновляем ротацию (человек отвлекается)
        if (ThreadLocalRandom.current().nextInt(100) < (aggressive ? 10 : 18)) {
            return;
        }

        // 2. Break lock — редкий микро-флик в сторону ломает Bi-LSTM aimlock-паттерн
        if (breakLockTicks > 0) {
            breakLockTicks--;
            return;
        } else if (ThreadLocalRandom.current().nextInt(100) < (aggressive ? 2 : 4)) {
            breakLockTicks = ThreadLocalRandom.current().nextInt(1, 3);
            float flickYaw = gaussRandom(0f, aggressive ? 5f : 10f);
            float flickPitch = gaussRandom(0f, aggressive ? 3f : 6f);
            applySafeRotation(mc.player.rotationYaw + flickYaw, mc.player.rotationPitch + flickPitch, 10, 8, 14, 14, 0, 0, false);
            return;
        }

        // 3. Coast — полная остановка мыши
        if (coastTicks > 0) {
            coastTicks--;
            if (ThreadLocalRandom.current().nextInt(100) < (aggressive ? 18 : 40)) {
                float microYaw = SensUtil.getSens(gaussRandom(0f, aggressive ? 0.18f : 0.35f));
                float microPitch = SensUtil.getSens(gaussRandom(0f, aggressive ? 0.14f : 0.25f));
                applySafeRotation(mc.player.rotationYaw + microYaw, mc.player.rotationPitch + microPitch, 8, 6, 14, 14, 0, 0, false);
            }
            if (coastTicks == 1) {
                currentSpookyPoint = Vector3d.ZERO;
            }
            return;
        }

        // Случайный coast
        int coastChance = aggressive ? 2 : 10;
        if (ThreadLocalRandom.current().nextInt(100) < coastChance) {
            boolean macroPause = ThreadLocalRandom.current().nextInt(100) < (aggressive ? 3 : 22);
            coastTicks = macroPause ? ThreadLocalRandom.current().nextInt(2, 5) : ThreadLocalRandom.current().nextInt(1, 3);
            mouseVelX *= 0.1f;
            mouseVelY *= 0.1f;
            return;
        }

        Vector3d eye = mc.player.getEyePosition(mc.getRenderPartialTicks());

        // 4. Мультипоинт
        if (currentSpookyPoint.equals(Vector3d.ZERO) || spookyPointTimer <= 0) {
            currentSpookyPoint = pickSpookyPoint(aggressive);
            spookyPointTimer = ThreadLocalRandom.current().nextInt(3, aggressive ? 8 : 15);
        }
        spookyPointTimer--;

        // 5. Идеальный угол
        Vector3d dir = currentSpookyPoint.subtract(eye).normalize();
        float idealYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float idealPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(dir.y, Math.hypot(dir.x, dir.z))), -90, 90);

        // 6. Ошибка прицеливания (медленный дрейф)
        if (aimErrorTicks <= 0) {
            aimErrorTicks = ThreadLocalRandom.current().nextInt(12, 45);
            float range = aggressive ? 1.6f : 3.0f;
            aimErrorYaw = gaussRandom(0f, range * 0.6f);
            aimErrorPitch = gaussRandom(0f, range * 0.4f);
        }
        aimErrorTicks--;
        idealYaw += aimErrorYaw;
        idealPitch += aimErrorPitch;

        // 7. Underaim — намеренно не доводим до цели (человек редко попадает идеально)
        idealYaw += gaussRandom(0f, aggressive ? 1.0f : 1.8f);
        idealPitch += gaussRandom(0f, aggressive ? 0.6f : 1.2f);

        // 8. Расстояние и ошибка
        double dist = AuraUtil.getStrictDistance(target);
        float errorYaw = MathHelper.wrapDegrees(idealYaw - mc.player.rotationYaw);
        float errorPitch = idealPitch - mc.player.rotationPitch;
        float absErrorYaw = Math.abs(errorYaw);
        float absErrorPitch = Math.abs(errorPitch);

        // 9. Обновление параметров физики
        paramRefreshTicks--;
        if (paramRefreshTicks <= 0) {
            paramRefreshTicks = ThreadLocalRandom.current().nextInt(15, 35);
            currentStiffness = 0.018f + ThreadLocalRandom.current().nextFloat() * 0.045f;
            currentFriction = 0.74f + ThreadLocalRandom.current().nextFloat() * 0.12f;
        }

        // 10. Физика мыши
        float maxSpeed = calcSpookySpeedByDistance(dist, aggressive);
        if (aggressive) maxSpeed *= 0.85f + ThreadLocalRandom.current().nextFloat() * 0.30f;

        float stiffness = currentStiffness + 0.035f * MathHelper.clamp(absErrorYaw / 55f, 0f, 1f);
        if (aggressive) stiffness *= 1.15f;

        // Friction — всегда высокое для плавности
        float friction = currentFriction;
        if (absErrorYaw < 6 && absErrorPitch < 4) {
            friction = 0.80f + ThreadLocalRandom.current().nextFloat() * 0.10f;
        }

        mouseVelX += errorYaw * stiffness;
        mouseVelY += errorPitch * stiffness;

        // 11. Gaussian шум (повышенный)
        float noiseBase = aggressive ? 0.45f : 0.75f;
        mouseVelX += gaussRandom(0f, noiseBase);
        mouseVelY += gaussRandom(0f, noiseBase * 0.70f);

        // 12. Twitch
        if (twitchTicks > 0) {
            twitchTicks--;
            float decay = twitchTicks / 3.0f;
            mouseVelX += twitchYaw * decay;
            mouseVelY += twitchPitch * decay;
        } else if (ThreadLocalRandom.current().nextInt(100) < (aggressive ? 8 : 28)) {
            twitchTicks = ThreadLocalRandom.current().nextInt(1, 3);
            twitchYaw = gaussRandom(0f, aggressive ? 1.4f : 3.2f);
            twitchPitch = gaussRandom(0f, aggressive ? 0.7f : 2.0f);
        }

        // 13. Overshoot
        if (overshootTicks > 0) {
            overshootTicks--;
            float decay = overshootTicks / 5.0f;
            mouseVelX += overshootYaw * decay;
            mouseVelY += overshootPitch * decay;
        } else if (absErrorYaw < 8 && absErrorPitch < 5 && ThreadLocalRandom.current().nextInt(100) < (aggressive ? 2 : 12)) {
            overshootTicks = ThreadLocalRandom.current().nextInt(2, 6);
            overshootYaw = gaussRandom(errorYaw * 0.3f, aggressive ? 1.2f : 2.2f);
            overshootPitch = gaussRandom(errorPitch * 0.3f, aggressive ? 0.8f : 1.4f);
        }

        // Трение
        mouseVelX *= friction;
        mouseVelY *= friction;

        // Ограничение скорости
        mouseVelX = MathHelper.clamp(mouseVelX, -maxSpeed, maxSpeed);
        mouseVelY = MathHelper.clamp(mouseVelY, -maxSpeed * 0.65f, maxSpeed * 0.65f);

        // 14. Защита от повторяющихся дельт
        float deltaDiffYaw = Math.abs(mouseVelX - lastDeltaYaw);
        float deltaDiffPitch = Math.abs(mouseVelY - lastDeltaPitch);
        if (deltaDiffYaw < 0.25f && deltaDiffPitch < 0.18f) {
            sameDeltaCount++;
            if (sameDeltaCount >= 2) {
                mouseVelX += gaussRandom(0f, 1.8f);
                mouseVelY += gaussRandom(0f, 1.2f);
                sameDeltaCount = 0;
            }
        } else {
            sameDeltaCount = 0;
        }
        lastDeltaYaw = mouseVelX;
        lastDeltaPitch = mouseVelY;

        // 15. Микро-шум вблизи цели (вместо aimlock-coast)
        if (absErrorYaw < 3f && absErrorPitch < 2.5f) {
            mouseVelX += gaussRandom(0f, 0.35f);
            mouseVelY += gaussRandom(0f, 0.25f);
        }

        float targetYaw = mc.player.rotationYaw + mouseVelX;
        float targetPitch = mc.player.rotationPitch + mouseVelY;

        // 16. GCD-jitter
        if (ThreadLocalRandom.current().nextInt(100) < 22) {
            targetYaw += gaussRandom(0f, 0.07f);
            targetPitch += gaussRandom(0f, 0.05f);
        }

        applySafeRotation(targetYaw, targetPitch, 18, 14, 14, 14, 0, 0, false);
    }

    /** Gaussian random: Box-Muller transform */
    private float gaussRandom(float mean, float stdDev) {
        if (stdDev <= 0f) return mean;
        double u1 = ThreadLocalRandom.current().nextDouble();
        double u2 = ThreadLocalRandom.current().nextDouble();
        double mag = stdDev * Math.sqrt(-2.0 * Math.log(u1));
        return (float) (mean + mag * Math.cos(2.0 * Math.PI * u2));
    }

    /** Выбирает случайную точку на хитбоксе цели: голова / тело / ноги */
    private Vector3d pickSpookyPoint(boolean aggressive) {
        net.minecraft.util.math.AxisAlignedBB bb = target.getBoundingBox();
        double cx = bb.minX + (bb.maxX - bb.minX) * 0.5;
        double cz = bb.minZ + (bb.maxZ - bb.minZ) * 0.5;
        double height = target.getHeight();
        double offsetXZ = ThreadLocalRandom.current().nextDouble(aggressive ? -0.25 : -0.15, aggressive ? 0.25 : 0.15);
        cx += offsetXZ;
        cz += offsetXZ;
        int zone = ThreadLocalRandom.current().nextInt(100);
        double cy;
        if (zone < 50) { // 50% голова
            cy = bb.minY + height * ThreadLocalRandom.current().nextDouble(0.78, 1.0);
        } else if (zone < 80) { // 30% тело
            cy = bb.minY + height * ThreadLocalRandom.current().nextDouble(0.4, 0.72);
        } else { // 20% ноги
            cy = bb.minY + height * ThreadLocalRandom.current().nextDouble(0.05, 0.35);
        }
        return new Vector3d(cx, cy, cz);
    }

    /** Чем ближе — тем медленнее/плавнее ротация */
    private float calcSpookySpeedByDistance(double dist, boolean aggressive) {
        if (dist <= 1.0) return aggressive ? 16f + ThreadLocalRandom.current().nextFloat() * 6f : 7f + ThreadLocalRandom.current().nextFloat() * 5f;
        if (dist <= 2.0) return aggressive ? 24f + ThreadLocalRandom.current().nextFloat() * 10f : 15f + ThreadLocalRandom.current().nextFloat() * 8f;
        if (dist <= 3.0) return aggressive ? 32f + ThreadLocalRandom.current().nextFloat() * 14f : 26f + ThreadLocalRandom.current().nextFloat() * 12f;
        return aggressive ? 42f + ThreadLocalRandom.current().nextFloat() * 16f : 38f + ThreadLocalRandom.current().nextFloat() * 14f;
    }

    // ---- Нейро-аура ----

    /**
     * Режим "Нейро": если выбрана модель — применяем предсказание ротации.
     * Если модель "Нет" — ничего не делаем (пустышка для записи).
     */
    private void updateNeuro() {
        if (target == null || mc.player == null) return;
        NeuroManager nm = NeuroManager.getInstance();

        nm.setActiveModel(neuroModelSetting.getValue());

        if (nm.isRecording()) {
            recordNeuroSample(nm);
        }

        if (nm.getActiveModel() == null) return;

        Vector3d eye  = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d tPos = target.getPositionVec().add(0, target.getHeight() / 2.0, 0);
        Vector3d delta = tPos.subtract(eye);
        float dist     = (float) AuraUtil.getStrictDistance(target);
        float cooldown = mc.player.getCooledAttackStrength(1.5F);

        float[] pred = nm.predict(
                (float) delta.x, (float) delta.y, (float) delta.z,
                dist, cooldown
        );
        // pred[0] = yawOffset, pred[1] = pitchOffset, pred[2] = sprintResetProb

        // Базовая ротация на цель
        Vector3d dir    = delta.normalize();
        float baseYaw   = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float basePitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(dir.y, Math.hypot(dir.x, dir.z))), -90, 90);

        float targetYaw   = baseYaw   + MathHelper.clamp(pred[0], -8, 8);
        float targetPitch = MathHelper.clamp(basePitch + MathHelper.clamp(pred[1], -6, 6), -90, 90);

        if (!neuroLerpInit) {
            neuroLerpYaw   = mc.player.rotationYaw;
            neuroLerpPitch = mc.player.rotationPitch;
            neuroLerpInit  = true;
        }

        float lerpSpeed = 0.20f;
        neuroLerpYaw   = neuroLerpYaw   + MathHelper.wrapDegrees(targetYaw - neuroLerpYaw) * lerpSpeed;
        neuroLerpPitch = neuroLerpPitch + (targetPitch - neuroLerpPitch) * lerpSpeed;

        long t = System.currentTimeMillis();
        float microYaw   = (float)(Math.sin(t / 320.0) * 0.35 + Math.cos(t / 190.0) * 0.2);
        float microPitch = (float)(Math.cos(t / 280.0) * 0.2  + Math.sin(t / 230.0) * 0.15);

        float finalYaw   = neuroLerpYaw   + microYaw;
        float finalPitch = MathHelper.clamp(neuroLerpPitch + microPitch, -90, 90);

        applySafeRotation(finalYaw, finalPitch, 18, 2, 20, 20, 2, 15, false);
    }

    private void recordNeuroSample(NeuroManager nm) {
        if (target == null) return;
        Vector3d eye = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d tPos = target.getPositionVec().add(0, target.getHeight() / 2.0, 0);
        Vector3d delta = tPos.subtract(eye);
        nm.recordSample(
                (float) delta.x, (float) delta.y, (float) delta.z,
                (float) AuraUtil.getStrictDistance(target),
                mc.player.getCooledAttackStrength(1.5F)
        );
    }

    @EventHandler public void onMoveInput(MoveInputEvent e) {
        if (p > 0 && !mc.player.isOnGround() && !mc.player.isInWater()) { e.setForward(0); p--; }
    }
    @EventHandler public void onEvent(MotionEvent e) {}
    @EventHandler public void onAttackStop(ActionEvent e) {
        if (p > 0 && !mc.player.isOnGround() && !mc.player.isInWater()) { e.setSprintState(false); p--; }
        if (target != null && UAura.cancelSprintTick(target, new float[]{(float) attackDistance(), 0}, stopSprint.getValue())) {
            if (!stopSprint.is("Выкл") && !stopSprint.is("Spooky")) e.setSprintState(false);
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (target != null && mc.player != null && mc.world != null)
            canCrit = !e.isToGround() && e.getFrom().y > e.getTo().y && mc.player.nextFallDistance != 0;
        else canCrit = false;
    }













    private void applySafeRotation(float targetYaw, float targetPitch, float speed, float smoothness,
                                   int yawSteps, int pitchSteps, int minDelay, int maxDelay, boolean instant) {
        RotationComponent.update(new Rotation(targetYaw, targetPitch), speed, smoothness,
                                yawSteps, pitchSteps, minDelay, maxDelay, instant);
    }
    
    private void updateAttack() {
        if (AuraUtil.getStrictDistance(target) >= attackDistance()) return;
        if (componentMode.is("Нейро") && neuroModelSetting.getValue().equals("Нет")) return;
        boolean inFluid = mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);

        if (reasonToAttack() && shouldAttack() && AuraUtil.getStrictDistance(target) < attackDistance()) {
            if (!inFluid && !mc.player.isOnGround() && !mc.player.isElytraFlying()) {
                p = 1;
            }

            if (stopSprint.is("Spooky")) {
                if (!spookySprintReset && rayTrace()) {
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                    mc.player.setSprinting(false);
                    spookySprintReset = true;
                    stopWatch.reset();
                    return;
                }
                if (spookySprintReset && rayTrace()) {
                    if (mc.player.isBlocking() && shildblock.getValue()) mc.playerController.onStoppedUsingItem(mc.player);
                    attackEntity(target);
                    if (mc.player.movementInput.moveForward > 0) {
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                        mc.player.setSprinting(true);
                    }
                    spookySprintReset = false;
                }
            } else {
                if (rayTrace()) {
                    if (mc.player.isBlocking() && shildblock.getValue()) mc.playerController.onStoppedUsingItem(mc.player);
                    attackEntity(target);
                }
            }
            count = (count + 1) % 3;
            canCrit = false;
        }
        stopWatch.reset();
    }

    private boolean shieldBreaker() {
        if (breacShild.getValue()) {
            int axe = -1;
            for (int i = 0; i < 9; i++) if (mc.player.inventory.getStackInSlot(i).getItem() instanceof net.minecraft.item.AxeItem) { axe = i; break; }
            if (axe >= 0 && target instanceof PlayerEntity) {
                PlayerEntity pt = (PlayerEntity) target;
                if (pt.isActiveItemStackBlocking() && pt.getActiveItemStack().getItem() instanceof net.minecraft.item.ShieldItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(axe));
                    attackEntity(target);
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                }
            }
        }
        return false;
    }

    private boolean checkReturn() {
        return (mc.player.isHandActive() && checkattack.getValue("Используеш еду") && !(mc.player.getActiveItemStack().getItem() instanceof net.minecraft.item.ShieldItem))
                || (mc.currentScreen != null && checkattack.getValue("Открыт контейнер"))
                || (!(mc.player.getHeldItemMainhand().getItem() instanceof net.minecraft.item.AxeItem)
                && !(mc.player.getHeldItemMainhand().getItem() instanceof net.minecraft.item.SwordItem) && onlySworld.getValue());
    }

    private void attackEntity(Entity e) { mc.playerController.attackEntity(mc.player, e); mc.player.swingArm(Hand.MAIN_HAND); }

    private final ArrayList<LivingEntity> validTargets = new ArrayList<>();

    private void updateTarget() {
        validTargets.clear();
        for (Entity e : mc.world.getAllEntities())
            if (e instanceof LivingEntity && isValidTarget((LivingEntity) e)) validTargets.add((LivingEntity) e);
        if (validTargets.isEmpty()) { target = null; return; }
        if (validTargets.size() == 1) { target = validTargets.get(0); return; }

        String sort = sortMode.getValue();
        if (sort.equals("Дистанции")) {
            validTargets.sort((a, b) -> Double.compare(mc.player.getDistance(a), mc.player.getDistance(b)));
        } else if (sort.equals("Здоровью")) {
            validTargets.sort((a, b) -> Float.compare(a.getHealth(), b.getHealth()));
        }
        target = validTargets.get(0);
    }

    public boolean isValidTarget(LivingEntity e) {
        if (e instanceof ClientPlayerEntity || e.ticksExisted < 3) return false;
        if (mc.player.getDistance(e) > attackFactor()) return false;
        if (!attackforStinka.getValue() && !RayTraceUtil.canSeen(RayTraceUtil.getPoint(e))) return false;
        if (e instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) e;
            if (Zetrix.inst().friendManager().isFriend(p.getName().getString()) && !targets.getValue("Друзей")) return false;
            if (AntiBot.getInstance().isBot(p) && AntiBot.getInstance().isEnabled()) return false;
            if (!targets.getValue("Игроки")) return false;
            if (e.getTotalArmorValue() == 0 && !targets.getValue("Голые")) return false;
            if (p.isCreative()) return false;
        }
        if ((e instanceof net.minecraft.entity.monster.MonsterEntity || e instanceof net.minecraft.entity.monster.SlimeEntity
                || e instanceof net.minecraft.entity.merchant.villager.VillagerEntity || e instanceof net.minecraft.entity.passive.AnimalEntity
                || e instanceof net.minecraft.entity.passive.DolphinEntity || e instanceof net.minecraft.entity.passive.SquidEntity
                || e instanceof net.minecraft.entity.passive.fish.AbstractFishEntity || e instanceof net.minecraft.entity.monster.GhastEntity
                || e instanceof net.minecraft.entity.monster.ShulkerEntity || e instanceof net.minecraft.entity.monster.PhantomEntity
                || e instanceof net.minecraft.entity.merchant.villager.WanderingTraderEntity) && !targets.getValue("Мобы")) return false;
        return !e.isInvulnerable() && e.isAlive() && !(e instanceof net.minecraft.entity.item.ArmorStandEntity);
    }

    private float attackFactor() {
        float f = 0;
        ElytraTarget elytraTarget = Zetrix.inst().moduleManager().get(ElytraTarget.class);
        if (elytraTarget != null && elytraTarget.isEnabled() && mc.player.isElytraFlying())
            f = elytraTarget.distance.getValue();
        return attackRange.getValue() + preRange.getValue() + f;
    }

    // FIX 2: убран perfectDelay — он блокировал все атаки
    public boolean shouldAttack() { return cooldownComplete(); }

    // FIX 3: если режим крита не выбран — всегда бьём
    public boolean reasonToAttack() {
        boolean onlyCrits = critmode.is("Только криты");
        boolean smartCrits = critmode.is("Умные криты");
        if (Criticals.getInstance().isEnabled() && mc.player.fallDistance > 0) return true;
        if (!onlyCrits && !smartCrits) return true;
        return AttackUtil.isAttack(onlyCrits || smartCrits, smartCrits, canCrit);
    }

    public boolean cooldownComplete() {
        float jitter = -0.015f + ThreadLocalRandom.current().nextFloat() * 0.04f;
        return mc.player.getCooledAttackStrength(1.5F) >= (0.93F + jitter);
    }

    public boolean rayTrace() {
        ElytraTarget elytraTarget = Zetrix.inst().moduleManager().get(ElytraTarget.class);
        return RayTraceUtil.rayTraceWithBlock(attackRange.getValue(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player, target, false)
                || componentMode.is("SpookyAnka")
                || (mc.player.isElytraFlying() && elytraTarget != null && elytraTarget.offlyth.getValue()
                && elytraTarget.isEnabled());
    }

    public double attackDistance() { return attackRange.getValue(); }

    private void reset() {
        TargetComponent.clearTarget(); TargetComponent.updateTargetList();
        target = null; canCrit = false; neuroLerpInit = false;
        spookySprintReset = false;
        currentSpookyPoint = Vector3d.ZERO; spookyPointTimer = 0;
        mouseVelX = 0; mouseVelY = 0;
        aimErrorYaw = 0; aimErrorPitch = 0; aimErrorTicks = 0;
        coastTicks = 0; twitchTicks = 0;
        lastDeltaYaw = 9999; lastDeltaPitch = 9999;
        sameDeltaCount = 0; overshootTicks = 0; overshootYaw = 0; overshootPitch = 0;
        currentStiffness = 0.04f; currentFriction = 0.78f; paramRefreshTicks = 0;
    }

    public float randomLerp(float min, float max) { return Interpolator.lerp(max, min, new SecureRandom().nextFloat()); }

    public float cooldownFromLastSwing() { return MathHelper.clamp(mc.player.ticksSinceLastSwing / randomLerp(8, 12), 0, 1); }
}
