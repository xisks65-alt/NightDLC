package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.annotations.PVE;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;
import java.util.function.Predicate;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@PVE
@ModuleInfo(name = "PotionCombiner", category = Category.MISC, desc = "Автоматическое комбинирование зелий")
public class PotionCombiner extends Module {

    public static PotionCombiner getInstance() {
        return Instance.get(PotionCombiner.class);
    }

    // --- Settings
    // Добавил оба написания комбо-режима для удобства.
    final ModeSetting potionType = new ModeSetting(this, "Зелье",
            "Сила",
            "Скорость",
            "скорка 3 + силка 3",
            "силка 3 + скорка 3"
    );

    // --- State
    final float level = 5f;

    final TimerUtil expTimer = new TimerUtil();  // ливка опыта
    final TimerUtil moveTimer = new TimerUtil(); // перенос предметов
    final TimerUtil openTimer = new TimerUtil(); // открытие наковальни

    final Random rng = new Random();

    Vector2f rotate = new Vector2f(0, 0);
    boolean autoRepair = false;

    // --- Сглаживание / рандом / дедзона (меньше тряски + быстрее сводим)
    static final float BASE_SMOOTH = 0.92f;     // было 0.8f
    static final float SMOOTH_JITTER = 0.005f;  // было 0.02f
    static final float YAW_JITTER_DEG = 0.03f;  // было 0.1f
    static final float PITCH_JITTER_DEG = 0.03f;// было 0.1f
    static final float DEAD_YAW = 0.8f;         // было 0.4f
    static final float DEAD_PITCH = 0.6f;       // было 0.3f

    static final double AIM_POS_JITTER = 0.02;  // было 0.06
    static final int ANVIL_SEARCH_RADIUS = 6;

    // кулдауны (открываем наковальню побыстрее)
    int nextOpenCooldownMs = 8;   // было 20
    int nextExpCooldownMs = 300;
    int nextMoveCooldownMs = 220;

    // применяем рассчитанные углы локально
    @EventHandler
    private void onWalking(MotionEvent e) {
        float yaw = normalizeYaw(rotate.x);
        float pitch = clampPitch(rotate.y);
        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        // Когда фармим опыт — смотрим вниз стабильнее
        if (autoRepair) {
            float targetPitch = 87f + rndRange(-0.7f, 0.7f); // было 84 ±2
            Vector2f target = new Vector2f(mc.player.rotationYaw, targetPitch);
            rotate = smoothRotateExpWithDeadzone(rotate, target);
        }

        // Есть уровень — ищем и открываем ближайшую наковальню
        if (mc.player.experienceLevel >= level) {
            BlockPos anvilPos = findNearestAnvil(ANVIL_SEARCH_RADIUS);
            if (anvilPos != null) {
                Vector3d base = new Vector3d(anvilPos.getX() + 0.5, anvilPos.getY() + 0.9, anvilPos.getZ() + 0.5);
                Vector3d aim = jitterPoint(base, AIM_POS_JITTER);
                lookAtPosition(aim, true, true);

                if (!(mc.currentScreen instanceof AnvilScreen)
                        && openTimer.isReached(nextOpenCooldownMs)
                        && isAimedClose(aim, 2.0f, 1.6f) // допуски чуть шире: открываем раньше
                        && isCrosshairOn(anvilPos)
                        && withinReach(aim, 4.6)) {
                    sendOpenAnvil(anvilPos);
                    openTimer.reset();
                    nextOpenCooldownMs = rngRange(0, 1);
                }
            }
        }

        // Работа с GUI наковальни
        if (mc.currentScreen instanceof AnvilScreen && !autoRepair) {
            movePotionsToAnvil();
            if (areBothSlotsReadyForMode() && mc.player.experienceLevel >= level) {
                if (((AnvilScreen) mc.currentScreen).getContainer().getSlot(2).getHasStack()) {
                    takeResult();
                }
            }
        }

        // Если уровня не хватает — льём опыт
        if (mc.player.experienceLevel < level && findExp() != -1) {
            autoRepair = true;
        }

        if (autoRepair) {
            if (mc.currentScreen instanceof AnvilScreen) {
                mc.player.closeScreen();
            }

            if (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE) {
                int expSlot = findExp();
                if (expSlot != -1 && expSlot != mc.player.inventory.currentItem + 36) {
                    InventoryUtil.moveItem(expSlot, mc.player.inventory.currentItem + 36, true);
                } else if (expSlot == -1) {
                    autoRepair = false;
                }
            } else if (expTimer.isReached(nextExpCooldownMs)) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                expTimer.reset();
                nextExpCooldownMs = rngRange(50, 70); // умеренно часто
                if (mc.player.experienceLevel >= level) {
                    autoRepair = false;
                }
            }
        }
    }

    // ---------- Инвентарь / GUI ----------

    // Для одиночных режимов — как прежде; для комбо — нужна ровно 1 Speed III и 1 Strength III
    private boolean areBothSlotsReadyForMode() {
        ItemStack s0 = getSlotStack(0);
        ItemStack s1 = getSlotStack(1);

        if (isComboSelected()) {
            boolean pair01 = isSpeedLvl(s0, 3) && isStrengthLvl(s1, 3);
            boolean pair10 = isStrengthLvl(s0, 3) && isSpeedLvl(s1, 3);
            return pair01 || pair10;
        }
        return isSelectedPotion(s0) && isSelectedPotion(s1);
    }

    private boolean isSelectedPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PotionItem)) return false;
        for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack)) {
            // одиночные режимы как раньше: Сила II / Скорость II (amplifier 1)
            if (potionType.is("Сила") && effect.getPotion() == Effects.STRENGTH && effect.getAmplifier() == 1)
                return true;
            if (potionType.is("Скорость") && effect.getPotion() == Effects.SPEED && effect.getAmplifier() == 1)
                return true;
            // в комбо этот метод используется только для поиска "любой из нужных", но строгую проверку делаем отдельно
            if (isComboSelected() && (
                    (effect.getPotion() == Effects.SPEED && effect.getAmplifier() == 2) ||
                            (effect.getPotion() == Effects.STRENGTH && effect.getAmplifier() == 2))) {
                return true; // Speed III или Strength III
            }
        }
        return false;
    }

    private void movePotionsToAnvil() {
        if (!(mc.currentScreen instanceof AnvilScreen)) return;

        if (isComboSelected()) {
            ensureComboSlots(); // строго: 1 Speed III + 1 Strength III
            return;
        }

        // одиночные режимы (как было)
        for (int i = 0; i < 2; i++) {
            ItemStack slotStack = safeGetSlotStack(i);
            if ((slotStack == null || slotStack.isEmpty()) && moveTimer.isReached(nextMoveCooldownMs)) {
                int slot = findPotionInInventory();
                if (slot != -1) {
                    swapOneItem(slot, i);
                    moveTimer.reset();
                    nextMoveCooldownMs = rngRange(85, 120);
                }
            }
        }
    }

    // Строгая раскладка для "силка 3 + скорка 3": в двух входных слотах должны быть разные банки
    private void ensureComboSlots() {
        if (!(mc.currentScreen instanceof AnvilScreen)) return;

        ItemStack s0 = safeGetSlotStack(0);
        ItemStack s1 = safeGetSlotStack(1);

        boolean s0Speed = isSpeedLvl(s0, 3);
        boolean s0Str = isStrengthLvl(s0, 3);
        boolean s1Speed = isSpeedLvl(s1, 3);
        boolean s1Str = isStrengthLvl(s1, 3);

        // Уже идеально
        if ((s0Speed && s1Str) || (s0Str && s1Speed)) return;

        if (!moveTimer.isReached(nextMoveCooldownMs)) return;

        // Оба пустые — кладём сначала Speed III, если нет — Strength III
        if ((s0 == null || s0.isEmpty()) && (s1 == null || s1.isEmpty())) {
            int slotSpeed = findPotionInInventory(this::isSpeed3);
            if (slotSpeed != -1) {
                swapOneItem(slotSpeed, 0);
            } else {
                int slotStr = findPotionInInventory(this::isStrength3);
                if (slotStr != -1) swapOneItem(slotStr, 0);
            }
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }

        // Один пустой — подставляем противоположный тип к уже лежащему
        if (s0 == null || s0.isEmpty()) {
            int slot = findPotionInInventory(s1Speed ? this::isStrength3 : this::isSpeed3);
            if (slot != -1) swapOneItem(slot, 0);
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }
        if (s1 == null || s1.isEmpty()) {
            int slot = findPotionInInventory(s0Speed ? this::isStrength3 : this::isSpeed3);
            if (slot != -1) swapOneItem(slot, 1);
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }

        // Оба заняты, но пара неправильная
        boolean ok0 = s0Speed || s0Str;
        boolean ok1 = s1Speed || s1Str;

        // Если какой-то слот вообще не из нужных — выгружаем
        if (!ok0) {
            quickMoveToInventory(0);
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }
        if (!ok1) {
            quickMoveToInventory(1);
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }

        // Дубликаты — заменяем правый слот на противоположный
        if (s0Speed && s1Speed) {
            int slotStr = findPotionInInventory(this::isStrength3);
            if (slotStr != -1) {
                quickMoveToInventory(1);
                swapOneItem(slotStr, 1);
            }
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
            return;
        }
        if (s0Str && s1Str) {
            int slotSpeed = findPotionInInventory(this::isSpeed3);
            if (slotSpeed != -1) {
                quickMoveToInventory(1);
                swapOneItem(slotSpeed, 1);
            }
            moveTimer.reset();
            nextMoveCooldownMs = rngRange(85, 120);
        }
    }

    private void quickMoveToInventory(int slotIdx) {
        if (mc.player.openContainer == null) return;
        mc.playerController.windowClick(
                mc.player.openContainer.windowId,
                slotIdx,
                0,
                ClickType.QUICK_MOVE,
                mc.player
        );
    }

    private boolean isStrengthLvl(ItemStack stack, int lvl) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PotionItem)) return false;
        for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack)) {
            if (effect.getPotion() == Effects.STRENGTH && effect.getAmplifier() == (lvl - 1)) return true;
        }
        return false;
    }

    private boolean isSpeedLvl(ItemStack stack, int lvl) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PotionItem)) return false;
        for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack)) {
            if (effect.getPotion() == Effects.SPEED && effect.getAmplifier() == (lvl - 1)) return true;
        }
        return false;
    }

    private boolean isSpeed3(ItemStack s) {
        return isSpeedLvl(s, 3);
    }

    private boolean isStrength3(ItemStack s) {
        return isStrengthLvl(s, 3);
    }

    private int findPotionInInventory() {
        return findPotionInInventory(this::isSelectedPotion);
    }

    private int findPotionInInventory(Predicate<ItemStack> matcher) {
        if (mc.player.openContainer == null) return -1;
        for (int i = 3; i < mc.player.openContainer.inventorySlots.size(); i++) {
            ItemStack stack = mc.player.openContainer.inventorySlots.get(i).getStack();
            if (matcher.test(stack)) return i;
        }
        return -1;
    }

    private void swapOneItem(int from, int to) {
        if (mc.player.openContainer == null) return;
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, to, 1, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.openContainer.windowId, from, 0, ClickType.PICKUP, mc.player);
    }

    private ItemStack getSlotStack(int slotId) {
        return mc.player.openContainer.inventorySlots.get(slotId).getStack();
    }

    private ItemStack safeGetSlotStack(int slotId) {
        if (mc.player.openContainer == null || slotId < 0 || slotId >= mc.player.openContainer.inventorySlots.size())
            return ItemStack.EMPTY;
        return mc.player.openContainer.inventorySlots.get(slotId).getStack();
    }

    private void takeResult() {
        if (mc.player.openContainer == null) return;
        mc.playerController.windowClick(mc.player.openContainer.windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
        if (mc.currentScreen instanceof AnvilScreen) {
            ((AnvilScreen) mc.currentScreen).nameField.setText("");
        }
    }

    private int findExp() {
        if (mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) return 45;
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) {
                return i < 9 ? 36 + i : i;
            }
        }
        return -1;
    }

    // ---------- Поиск/открытие наковальни ----------

    private BlockPos findNearestAnvil(int radius) {
        BlockPos playerPos = mc.player.getPosition();
        Vector3d eye = mc.player.getEyePosition(1.0f);

        BlockPos best = null;
        double bestDist2 = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = playerPos.add(dx, dy, dz);
                    Block b = mc.world.getBlockState(p).getBlock();
                    if (b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL || b == Blocks.DAMAGED_ANVIL) {
                        Vector3d centerTop = new Vector3d(p.getX() + 0.5, p.getY() + 0.9, p.getZ() + 0.5);
                        double d2 = centerTop.squareDistanceTo(eye);
                        if (d2 < bestDist2) {
                            bestDist2 = d2;
                            best = p;
                        }
                    }
                }
            }
        }
        return best;
    }

    private void sendOpenAnvil(BlockPos anvilPos) {
        Vector3d center = new Vector3d(anvilPos.getX() + 0.5, anvilPos.getY() + 0.5, anvilPos.getZ() + 0.5);
        Vector3d hit = jitterPoint(center, 0.08); // было 0.14 — меньше дрожь при клике
        BlockRayTraceResult result = new BlockRayTraceResult(hit, Direction.UP, anvilPos, false);
        mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, result));
    }

    private boolean isCrosshairOn(BlockPos pos) {
        if (mc.objectMouseOver instanceof BlockRayTraceResult) {
            return ((BlockRayTraceResult) mc.objectMouseOver).getPos().equals(pos);
        }
        return false;
    }

    private boolean withinReach(Vector3d point, double reach) {
        Vector3d eye = mc.player.getEyePosition(1.0f);
        return eye.squareDistanceTo(point) <= reach * reach;
    }

    // ---------- Ротации ----------

    private void lookAtPosition(Vector3d targetPos, boolean smooth, boolean addJitter) {
        Vector3d aim = addJitter ? jitterPoint(targetPos, AIM_POS_JITTER) : targetPos;
        Vector3d eye = mc.player.getEyePosition(1.0f);
        Vector2f need = calcRotations(eye, aim);

        // лёгкий шум по углам
        need = new Vector2f(
                need.x + rndRange(-YAW_JITTER_DEG, YAW_JITTER_DEG),
                need.y + rndRange(-PITCH_JITTER_DEG, PITCH_JITTER_DEG)
        );

        rotate = smooth ? smoothRotateExpWithDeadzone(rotate, need) : need;
    }

    private Vector2f smoothRotateExpWithDeadzone(Vector2f current, Vector2f target) {
        float yawDiff = wrapDegrees(target.x - current.x);
        float pitchDiff = target.y - current.y;

        // если очень близко — не шевелим (экономим пакеты)
        if (Math.abs(yawDiff) < DEAD_YAW && Math.abs(pitchDiff) < DEAD_PITCH) return current;

        float t = clamp01(BASE_SMOOTH + rndRange(-SMOOTH_JITTER, SMOOTH_JITTER));
        float newYaw = normalizeYaw(current.x + yawDiff * t);
        float newPitch = clampPitch(current.y + pitchDiff * t);
        return new Vector2f(newYaw, newPitch);
    }

    private boolean isAimedClose(Vector3d targetPos, float yawTolDeg, float pitchTolDeg) {
        Vector2f need = calcRotations(mc.player.getEyePosition(1.0f), targetPos);
        float dyaw = Math.abs(wrapDegrees(need.x - rotate.x));
        float dp = Math.abs(need.y - rotate.y);
        return dyaw <= yawTolDeg && dp <= pitchTolDeg;
    }

    private Vector2f calcRotations(Vector3d fromEye, Vector3d to) {
        double dx = to.x - fromEye.x;
        double dy = to.y - fromEye.y;
        double dz = to.z - fromEye.z;
        double distXZ = Math.hypot(dx, dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        return new Vector2f(normalizeYaw(yaw), clampPitch(pitch));
    }

    // ---------- Утилиты ----------

    private Vector3d jitterPoint(Vector3d base, double r) {
        double jx = rndRange((float) -r, (float) r);
        double jz = rndRange((float) -r, (float) r);
        double jy = rndRange((float) (-r * 0.5), (float) (r * 0.5));
        return new Vector3d(base.x + jx, base.y + jy, base.z + jz);
    }

    private float rndRange(float a, float b) {
        return a + (b - a) * rng.nextFloat();
    }

    private int rngRange(int a, int b) {
        return a + rng.nextInt(Math.max(1, b - a + 1));
    }

    private static float wrapDegrees(float v) {
        v %= 360f;
        if (v >= 180f) v -= 360f;
        if (v < -180f) v += 360f;
        return v;
    }

    private static float normalizeYaw(float yaw) {
        return wrapDegrees(yaw);
    }

    private static float clampPitch(float pitch) {
        if (pitch > 90f) return 90f;
        if (pitch < -90f) return -90f;
        return pitch;
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    // --- helpers ---
    private boolean isComboSelected() {
        return potionType.is("скорка 3 + силка 3") || potionType.is("силка 3 + скорка 3");
    }
}
