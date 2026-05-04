package dev.wh1tew1ndows.client.managers.module.impl.combat.aura;

import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.security.SecureRandom;
import java.util.Arrays;

public class UAura {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Minecraft mc = Minecraft.getInstance();
    public static long hitCounterCPSBypass;

    public static void hitCounterCPSBypassNext() {
        hitCounterCPSBypass++;
    }

    public static void hitCounterCPSBypassReset() {
        hitCounterCPSBypass = 0L;
    }

    public static boolean cpsBypassTrigger() {
        return (hitCounterCPSBypass % 7L == 3L);
    }

    public static PlayerEntity getSelf() {
        return mc.player;
    }

    public static World getWorld() {
        return mc.world;
    }

    public static float applyGaussianJitter(float paramFloat) {
        return (float) (paramFloat + secureRandom.nextGaussian() * 0.20000000298023224D * 2.0D - 0.20000000298023224D);
    }

    public static boolean randomBoolean(int paramInt) {
        return (secureRandom.nextInt(paramInt + 1) >= 1.0F * 1.0F / Math.max(paramInt, 1.0F));
    }

    public static boolean randomBoolean() {
        return (secureRandom.nextInt(2) == 1);
    }

    public static float randomFloat(float paramFloat1, float paramFloat2) {
        return secureRandom.nextFloat(paramFloat1, paramFloat2);
    }

    public static float randomFloat() {
        return randomFloat(0.0F, 1.0F);
    }

    public static int randomInt1PosibleOrNot() {
        return randomBoolean() ? 1 : -1;
    }

    public static int getAxeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.mainInventory.get(i).getItem() instanceof net.minecraft.item.AxeItem)
                return i;
        }
        return -1;
    }

    public static Runnable[] hitShieldBreakTaskForUse(LivingEntity livingEntity, boolean enabled) {
        final Runnable[] pre$post = new Runnable[]{() -> {}, () -> {}};
        if (!enabled) return pre$post;
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) livingEntity;
            if (Math.abs(MathHelper.wrapDegrees(mc.player.rotationYaw - playerEntity.rotationYaw - 180.0F)) > 90.0F) {
                ItemStack itemStack1 = playerEntity.getHeldItemMainhand();
                ItemStack itemStack2 = playerEntity.getHeldItemOffhand();
                if (itemStack1 != null && itemStack2 != null) {
                    Item item1 = itemStack1.getItem();
                    Item item2 = itemStack2.getItem();
                    int handSlot = mc.player.inventory.currentItem;
                    int axeSlot = getAxeSlot();
                    if ((item1 == Items.SHIELD || item2 == Items.SHIELD) && axeSlot != -1 && axeSlot != handSlot) {
                        final int slot = axeSlot;
                        pre$post[0] = () -> new CHeldItemChangePacket(slot).sendSilent();
                        pre$post[1] = () -> new CHeldItemChangePacket(handSlot).sendSilent();
                    }
                }
            }
        }
        return pre$post;
    }

    public static Runnable[] resetShieldSilentTaskForUse(boolean enabled) {
        final Runnable[] pre$post = new Runnable[]{() -> {}, () -> {}};
        if (!enabled) return pre$post;
        if (mc.player.isActiveItemStackBlocking()) {
            Hand hand = mc.player.getActiveHand();
            if (hand == null) return pre$post;
            pre$post[0] = () -> new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN).sendSilent();
            pre$post[1] = () -> new CPlayerTryUseItemPacket(hand).sendSilent();
        }
        return pre$post;
    }

    public static Runnable[] skipSilentSprintingTaskForUse(boolean enabled) {
        final Runnable[] pre$post = new Runnable[]{() -> {}, () -> {}};
        if (!enabled || mc.player == null) return pre$post;
        if (mc.player.isServerSprintState() && !mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
            pre$post[0] = () -> {
                if (mc.player == null) return;
                mc.player.setSprinting(false);
                new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING).sendSilent();
            };
            pre$post[1] = () -> {
                if (mc.player == null) return;
                mc.player.setSprinting(true);
                new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING).sendSilent();
            };
        }
        return pre$post;
    }

    public static double getYCapacityOnPlayerPos(int rangeY) {
        if (mc.world == null) return 1.0D;
        Vector3d eyePos = mc.player.getEyePosition(mc.getRenderPartialTicks());
        double minDst = rangeY * 2.0D;
        double maxY = 255.0D, minY = -64.0D;
        float halfW = mc.player.getWidth() / 2.0F - 0.01F;
        for (Vector3d vec : Arrays.asList(
                eyePos.add(-halfW, 0.0D, -halfW),
                eyePos.add(halfW, 0.0D, halfW),
                eyePos.add(halfW, 0.0D, -halfW),
                eyePos.add(-halfW, 0.0D, halfW))) {
            BlockRayTraceResult r1 = mc.world.rayTraceBlocks(new RayTraceContext(vec, vec.add(0.0D, -rangeY, 0.0D), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, mc.player));
            BlockRayTraceResult r2 = mc.world.rayTraceBlocks(new RayTraceContext(vec, vec.add(0.0D, rangeY, 0.0D), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, mc.player));
            if (maxY > r2.getHitVec().y) maxY = r2.getHitVec().y;
            if (minY < r1.getHitVec().y) minY = r1.getHitVec().y;
            double dst = maxY - minY;
            if (minDst > dst) minDst = dst;
        }
        return minDst - mc.player.getHeight();
    }

    public static double convenientFallOffset() {
        double d = mc.player.fallDistance;
        if (mc.world != null && !mc.player.isOnGround()
                && mc.player.getMotion().y < -0.0784000015258789D
                && !mc.world.getBlockState(mc.player.getPosition()).getMaterial().isLiquid()
                && !mc.world.getBlockState(mc.player.getPosition().up()).getMaterial().isLiquid()
                && mc.player.fallDistance < -mc.player.getMotion().y
                && mc.player.ticksOnGround > 6) {
            d = -mc.player.getMotion().y;
        }
        return d;
    }

    public static boolean isBestMomentToHit(boolean fallCheck) {
        if (!fallCheck) return true;
        float f1 = 0.0F, f2 = 0.2F;
        if (cpsBypassTrigger() && getYCapacityOnPlayerPos(2) > 0.20000004768371582D) {
            f1 = f2;
        }
        boolean hasFall = (convenientFallOffset() > f1 || getYCapacityOnPlayerPos(2) < 0.10000000149011612D);
        if (hasFall) return true;
        boolean badLiquid = (!mc.player.movementInput.jump && (mc.player.isInWater() || mc.player.isInLava()))
                || mc.player.areEyesInFluid(FluidTags.WATER)
                || mc.player.areEyesInFluid(FluidTags.LAVA)
                || mc.player.isMaterialInBB(Material.WEB);
        return true;
    }

    public static int randomInt1PosibleOrNotOnHit = -1;

    public static boolean useEntity(LivingEntity livingEntity, Runnable preHit, Runnable postHit, Hand hand, boolean cpsBypass) {
        if (preHit != null) preHit.run();
        if (livingEntity != null) {
            assert mc.playerController != null;
            mc.playerController.attackEntity(mc.player, livingEntity);
            if (cpsBypass) hitCounterCPSBypassNext();
            else hitCounterCPSBypassReset();
            cooldownTimer.reset();
            if (hand != null) mc.player.swingArm(hand);
        }
        if (postHit != null) postHit.run();
        return (livingEntity != null);
    }

    @Getter
    private static final StopWatch cooldownTimer = new StopWatch();

    private static boolean missDetected;
    private static int counterTo0PostMissHits;

    public static long getMsCooldown() {
        return 0L;
    }

    public static boolean msCooldownReached(long msOffset) {
        return cooldownTimer.finished(getMsCooldown() + msOffset);
    }

    public static boolean msCooldownReached() {
        return msCooldownReached(0L);
    }

    public static boolean msCooldownHasMs(long ms) {
        return cooldownTimer.finished(ms);
    }

    public static float msCooldownPC01() {
        return Math.min((float) cooldownTimer.elapsedTime() / (float) getMsCooldown(), 1.0F);
    }

    public static float msCooldownReach() {
        return (float) cooldownTimer.elapsedTime();
    }

    public static boolean anyEntityOnRay(LivingEntity livingEntity, double range) {
        return (livingEntity != null && RayTraceUtil.isViewEntity(livingEntity, MathHelper.wrapDegrees(mc.player.rotationYaw), mc.player.rotationPitch, (float) range, true));
    }

    public static boolean shouldAttack(LivingEntity livingTarget, boolean rayCast, boolean distanceCheck, boolean fallCheck, long cooldownMSOffset, float[] ranges) {
        if (distanceCheck && livingTarget != null && !mc.player.validDistance(livingTarget, ranges[0], true))
            return false;
        if (!msCooldownReached(cooldownMSOffset)) return false;
        boolean valid = isBestMomentToHit(fallCheck);
        if (valid && rayCast && !anyEntityOnRay(livingTarget, ranges[0]))
            valid = false;
        return valid;
    }

    public static boolean shouldAttack(LivingEntity livingTarget, boolean rayCast, boolean fallCheck, long cooldownMSOffset, float[] ranges) {
        return shouldAttack(livingTarget, rayCast, true, fallCheck, cooldownMSOffset, ranges);
    }

    public static boolean cancelSprintTick(LivingEntity targetIn, float[] ranges, String stopSprintMode) {
        if (stopSprintMode == null || stopSprintMode.equals("Выкл")) return false;
        if (stopSprintMode.equals("Нейро")) return false; // обрабатывается в updateNeuro
        if (stopSprintMode.equals("Test"))  return false; // обрабатывается в updateAttack
        if (targetIn == null) return false;
        if (!shouldAttack(targetIn, false, false, -50L, ranges)) return false;
        // "Авто" — только в воздухе при падении (не на земле, не в воде)
        if (stopSprintMode.equals("Авто")) {
            if (mc.player.isOnGround() || mc.player.areEyesInFluid(FluidTags.WATER)) return false;
            return mc.player.getMotion().y <= 0.0030162615090425808D;
        }
        // "Всегда", "Тихий", "Агрессивный" — всегда, включая землю
        return true;
    }

    private static int maxHitsCountOnMiss() {
        return 3;
    }

    public static void antiMissesHittingReset() {
        missDetected = false;
        counterTo0PostMissHits = 0;
    }

    public static void antiMissesHittingUpdate(LivingEntity targetIn, boolean cpsBypass, boolean rayCastCheck, boolean enabled) {
        if (targetIn == null || counterTo0PostMissHits == 0 || !enabled || targetIn.hurtTime != 0)
            antiMissesHittingReset();
        if (enabled && targetIn != null && msCooldownHasMs(cpsBypassTrigger() ? 250L : 150L) && mc.player.isSwingInProgress) {
            if (!missDetected && counterTo0PostMissHits == 0 && targetIn.hurtTime == 0) {
                missDetected = true;
                counterTo0PostMissHits = maxHitsCountOnMiss();
            }
            if (missDetected && counterTo0PostMissHits > 0) {
                if ((!rayCastCheck || anyEntityOnRay(targetIn, 6.0D)) && useEntity(targetIn, () -> {}, () -> {}, Hand.MAIN_HAND, cpsBypass)) {
                    counterTo0PostMissHits--;
                }
            }
        }
    }
}
