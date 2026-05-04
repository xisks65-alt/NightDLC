package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.ViaUtil;
import dev.wh1tew1ndows.client.utils.rotation.RotationUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Spider", category = Category.MOVEMENT, desc = "Лазание по стенам как паук")
public class Spider extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "Blocks", "Water", "Matrix", "SpookyTime");
    private final ModeSetting modeRotate = new ModeSetting(this, "Ротация", "Packet", "Client").setVisible(() -> mode.is("Blocks"));
    private final Script script = new Script();
    int prevSlot = -1;
    private final SliderSetting spiderSpeed = new SliderSetting(
            this, "Speed",
            2.0f,
            1.0f,
            10.0f,
            0.05f
    ).setVisible(() -> mode.is("Matrix"));


    final TimerUtil stopWatch = new TimerUtil();

    private final TimerUtil timerUtil = new TimerUtil();

    @Override
    public void toggle() {
        super.toggle();
        if (mode.is("SpookyTime")) {
            mc.gameSettings.keyBindSneak.setPressed(false);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (mode.is("SpookyTime")) {
            timer.cancel();
            timer = new Timer();
            canUse = true;
            if (mc.gameSettings != null) {
                mc.gameSettings.keyBindSneak.setPressed(false);
                mc.gameSettings.keyBindJump.setPressed(false);
            }
        }
    }

    private long lastBucketUse = 0L;
    private final boolean hasWallContact = false;
    private final Random randomizer = new Random();
    private final int tickSeed = 0;
    private final Random random = new Random();

    // WaterBucket mode fields
    private Timer timer = new Timer();
    private boolean canUse = true;
    private final long lastWallJumpMs = 0L;
    private static final long WALL_JUMP_COOLDOWN_MS = 250L;


    private int locateWaterBucket() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.WATER_BUCKET)
                return i;
        }
        return -1;
    }

    private void performWallBoost(int bucketSlot) {
        mc.gameSettings.keyBindJump.setPressed(true);
        mc.gameSettings.keyBindSneak.setPressed(true);

        Rotation aimed = new Rotation(mc.player.rotationYaw, 0);
        RotationComponent.update(aimed, 180.0F, 180.0F, 1, 5);

        if (System.currentTimeMillis() - lastBucketUse < getCooldownByHeight())
            return;

        swapAndUseBucket(bucketSlot);

        lastBucketUse = System.currentTimeMillis();
    }

    private void swapAndUseBucket(int slot) {
        int previousSlot = mc.player.inventory.currentItem;

        if (slot != previousSlot)
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));

        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        mc.player.motion.y = 0.42D + (random.nextDouble() * 0.03D);

        if (slot != previousSlot)
            mc.player.connection.sendPacket(new CHeldItemChangePacket(previousSlot));
    }

    private long getCooldownByHeight() {
        double distance = computeHeightGap();
        return distance <= 100.0 ? 300L : random.nextLong(15, 17);
    }

    private double computeHeightGap() {
        if (mc.player == null || mc.world == null) return 0.0;

        double startY = mc.player.getPosY();
        for (double y = startY; y > 0.0; y -= 0.1) {
            BlockPos pos = new BlockPos(mc.player.getPosX(), y, mc.player.getPosZ());
            if (!mc.world.isAirBlock(pos)) {
                return Math.max(startY - (y + 1), 0.0);
            }
        }
        return 0.0;
    }


    @EventHandler
    public void onMotion(MotionEvent e) {
        if (mode.is("Water")) {
            if (mc.player.isInWater()) {

                mc.player.setMotion(mc.player.getMotion().x, 0.33, mc.player.getMotion().z);
            }

        }

        if (mode.is("Matrix")) {
            if (!mc.player.collidedHorizontally) {
                return;
            }
            long speed = MathHelper.clamp(500 - (spiderSpeed.getValue().longValue() / 2 * 100), 0, 500);
            if (stopWatch.isReached(speed)) {
                e.setOnGround(true);
                mc.player.setOnGround(true);
                mc.player.collidedVertically = true;
                mc.player.collidedHorizontally = true;
                mc.player.isAirBorne = true;
                mc.player.jump();
                stopWatch.reset();
            }
        }

        if (mode.is("SpookyTime")) {
            handleWaterBucketMode(e);
        }

    }

    private void handleWaterBucketMode(MotionEvent e) {
        if (mc.player == null || mc.player.isInWater()) {
            return;
        }
        Rotation aimed = new Rotation(mc.player.rotationYaw, 0);
        RotationComponent.update(aimed, 180.0F, 180.0F, 1, 5);
        if (!mc.player.collidedHorizontally) {
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.gameSettings.keyBindSneak.setPressed(false);
            }
            return;
        }

        int waterSlot = locateWaterBucket();
        if (waterSlot == -1) {
            return;
        }


        if (canUse) {
            int currentSlot = mc.player.inventory.currentItem;

            if (waterSlot != currentSlot) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(waterSlot));
            }

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            if (waterSlot != currentSlot) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(currentSlot));
            }

            double yBoost = 0.42D + (random.nextDouble() * 0.03D);
            mc.player.motion.y = yBoost;

            canUse = false;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    canUse = true;
                }
            }, getAppliedDelayMs());
        }


        mc.gameSettings.keyBindSneak.setPressed(true);

    }
    
    private int getAppliedDelayMs() {
        float base = 0.45F;
        int ms = (int) (base * 1000.0F);
        int offset = random.nextInt(60) - 30;
        return Math.max(150, ms + offset);
    }

    private int findWaterBucketSlot() {
        return IntStream.range(0, 9)
                .filter(i -> mc.player.inventory.getStackInSlot(i).getItem().getTranslationKey().equals("item.minecraft.water_bucket"))
                .findFirst()
                .orElse(-1);
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        script.update();
        if (mode.is("Blocks")) {
            int slotId = findBlockSlotId();
            if (slotId != -1) {
                Hand hand = (mc.player.getHeldItemOffhand().getItem() instanceof BlockItem bi && bi.getBlock().getDefaultState().isSolid()) ? Hand.OFF_HAND : Hand.MAIN_HAND;
                ItemStack itemStack = hand.equals(Hand.OFF_HAND) ? mc.player.getHeldItemOffhand() : mc.player.inventory.getStackInSlot(slotId);
                BlockPos pos = findPos(-1);
                if (canPlace(itemStack) && !pos.equals(BlockPos.ZERO)) {
                    Vector3d vec = Vector3d.copyCentered(pos);
                    Direction direction = Direction.getFacingFromVector(vec.x - mc.player.getPosX(), 0, vec.z - mc.player.getPosZ());
                    float[] rotate = RotationUtil.calculateAngle(vec.subtract(new Vector3d(direction.toVector3f()).mul(0.5)));

                    if (hand.equals(Hand.MAIN_HAND)) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(slotId));
                        prevSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = slotId;
                    }
                    if (modeRotate.is("Packet")) {
                        ViaUtil.sendPositionPacket(rotate[0], rotate[1], true);
                    } else {
                        RotationComponent.update(new Rotation(rotate[0], rotate[1]), 360, 360, 0, 5);
                    }
                    if (!mc.player.isSneaking()) {
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
                    }
                    mc.playerController.rightClickBlock(mc.player, mc.world, hand, new BlockRayTraceResult(vec, direction.getOpposite(), pos, false));
                    if (!mc.player.isSneaking()) {
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
                    }
                    if (modeRotate.is("Packet")) {
                        ViaUtil.sendPositionPacket(mc.player.rotationYaw, mc.player.rotationPitch, true);
                    }
                    if (hand.equals(Hand.MAIN_HAND)) {
                        script.cleanup().addTickStep(0, () -> {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
                            mc.player.inventory.currentItem = prevSlot;
                        }, 10);
                    }
                }
            }
        }
    }

    private int findBlockSlotId() {
        return IntStream.range(0, 9).filter(i -> mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem).findFirst().orElse(-1);
    }

    private boolean canPlace(ItemStack stack) {
        return mc.player.getPosition().getY() + mc.player.motion.y < mc.player.getPosY() && mc.world.getBlockState(new BlockPos(mc.player.getPosition().getVec().add(0, -0.01f, 0))).getBlock().canSpawnInBlock();
    }

    private BlockPos findPos(int yOffset) {
        BlockPos blockPos = mc.player.getPosition().add(0, yOffset, 0);
        List<BlockPos> list = List.of(blockPos.west(), blockPos.east(), blockPos.south(), blockPos.north());
        return list.stream().filter(pos -> !mc.world.getBlockState(pos).isAir()).findFirst().orElse(BlockPos.ZERO);
    }
}
