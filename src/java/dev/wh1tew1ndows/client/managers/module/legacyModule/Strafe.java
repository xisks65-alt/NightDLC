package dev.wh1tew1ndows.client.managers.module.legacyModule;


import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.FreeLookComponent;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.*;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.player.FreeCam;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.DamageUtil;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.player.StrafeMovement;
import dev.wh1tew1ndows.client.utils.rotation.SensUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;


@Getter
@Accessors(fluent = true)
@ModuleInfo(name = "Strafe", category = Category.MOVEMENT, desc = "Улучшенное движение в воздухе")
public class Strafe extends Module {


    private final ModeSetting strafeModeImpl = new ModeSetting(this, "Обход", "Ванилла/матрикс", "Современные ач", "StormHvH");
    private final BooleanSetting damageBoost = new BooleanSetting(this, "Буст от урона", false).setVisible(() -> strafeModeImpl.is("Ванилла/матрикс"));
    private final SliderSetting damageSpeed = new SliderSetting(this, "Скорость буста", .5F, .1F, 5.F, 0.1f).setVisible(damageBoost::getValue);
    private final BooleanSetting collisionBoost = new BooleanSetting(this, "Буст от столкновений", false).setVisible(() -> strafeModeImpl.is("Современные ач"));

    private final StrafeMovement strafeMovement = new StrafeMovement();
    private final DamageUtil damageUtil = new DamageUtil();

    @EventHandler
    public void action(ActionEvent event) {
        if (strafeModeImpl.is("Ванилла/матрикс")) handleActionEvent(event);
    }

    @EventHandler
    public void move(MoveEvent event) {
        if (strafeModeImpl.is("Ванилла/матрикс")) handleMoveEvent(event);
        if (strafeModeImpl.is("StormHvH")) {
            if (mc.player.isOnGround()) {
                MoveUtil.setSpeed(MoveUtil.getSpeed());
            }
            if (MoveUtil.isMoving()) {
                if (mc.player.fallDistance > 0.67) MoveUtil.setSpeed(.35f);
                else if (mc.player.fallDistance > 0.17) MoveUtil.setSpeed(0.4f);
                else MoveUtil.setSpeed(0.39f);

            }
        }
    }

    @EventHandler
    public void postMove(PostMoveEvent event) {
        if (strafeModeImpl.is("Ванилла/матрикс")) strafeMovement.postMove(event.getHorizontalMove());
    }

    @EventHandler
    public void packet(PacketEvent event) {
        if (strafeModeImpl.is("Ванилла/матрикс")) handlePacketEvent(event);
    }

    @EventHandler
    public void damage(DamageEvent event) {
        if (strafeModeImpl.is("Ванилла/матрикс")) handleDamageEvent(event);
    }


    @EventHandler
    public void onMoveInput(MoveInputEvent event) {
        if (this.strafeModeImpl.is("Современные ач") && event.isMoving() && this.canAroundRotateUpdated()) {
            event.setForward(1F);
            event.setStrafe(0F);
        }
    }

    @EventHandler
    public void onSprintLock(SprintLockEvent event) {
        if (this.canAroundRotateUpdated())
            event.unlockSprint();
    }


    private void handleMoveEvent(MoveEvent event) {
        if (allowStrafe()) {
            if (damageBoost.getValue()) damageUtil.time(1300L);
            final double speed = strafeMovement.calcSpeed(event, damageBoost.getValue(), damageUtil.isNormalDamage(), false, damageSpeed.getValue());
            if (MoveUtil.isMoving()) MoveUtil.setSpeed(event, speed);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }


    private void handleActionEvent(ActionEvent event) {
        if (allowStrafe()) {
            if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
                event.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
            }
        }
        if (strafeMovement.isNeedSwap()) {
            event.setSprintState(!mc.player.isServerSprintState());
            strafeMovement.setNeedSwap(false);
        }
    }

    private void handlePacketEvent(PacketEvent event) {
        if (damageBoost.getValue()) {
            damageUtil.processPacket(event);
        }
        if (event.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleDamageEvent(DamageEvent event) {
        if (damageBoost.getValue()) {
            damageUtil.processDamage(event);
        }
    }


    public boolean allowStrafe() {
        if (isInvalidPlayerState()) {
            return false;
        }
        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();
        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }
        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }
        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null || mc.player.isSneaking() || mc.player.isElytraFlying() || mc.player.isInWater() || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }


    private float getInputMoveYaw(float appendYaw) {
        float moveYaw = MoveUtil.moveYaw(appendYaw);
        return SensUtil.getSens(moveYaw);
    }

    private boolean canAroundRotateUpdated() {
        if (RotationComponent.getInstance().currentTask() != RotationComponent.RotationTask.IDLE || mc.currentScreen != null || Zetrix.inst().moduleManager().get(FreeCam.class).isEnabled())
            return false;
        final Item[] throwableItems = new Item[]{Items.BOW, Items.TRIDENT, Items.CROSSBOW, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.ENDER_PEARL, Items.SNOWBALL, Items.FISHING_ROD, Items.EXPERIENCE_BOTTLE, Items.EGG};
        final ItemStack main = mc.player.getHeldItemMainhand(), off = mc.player.getHeldItemOffhand();
        final Item itemMain = main.getItem(), itemOff = off.getItem();
        if (mc.player.isSneaking()) return false;
        for (Item throwableItem : throwableItems) {
            if (throwableItem == itemMain) {
                return false;
            }
        }
        for (Item item : throwableItems) {
            if (item == itemOff) {
                return false;
            }
        }
        return true;
    }

    public float moveYaw = -999;


    //@EventHandler
    //public void onMoveRotate(RotateMoveSideEvent event) {
    //    if (mc.currentScreen != null) return;
    //    if (MoveUtil.isMoving() && this.canAroundRotateUpdated())
    //        event.setYaw(this.getInputMoveYaw(FreeLookComponent.getFreeYaw()));
    //}

    public boolean preGround;

    private boolean rageHead() {
        return RotationComponent.getInstance().currentTask() != RotationComponent.RotationTask.IDLE;
    }

    @EventHandler
    public void onPreUpdatePlayer(MotionEvent event) {
        if (this.strafeModeImpl.is("Современные ач") && MoveUtil.isMoving()) {
            if (this.collisionBoost.getValue()) {
                final double selfSpeed = MoveUtil.getSpeed();
                final AxisAlignedBB selfAABB;
                int collisions;
                if (selfSpeed >= .08D && (collisions = Math.min(mc.world.getEntitiesWithinAABB(LivingEntity.class, selfAABB = mc.player.getBoundingBox().grow(.1F)).size() - mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, selfAABB).size() - 1, 5)) > 0) {
                    final double speedAppend = Mathf.clamp(1.D - MoveUtil.getSpeed() / 1.8D, 0.D, 1.D) * (mc.player.isSprinting() ? .2F : .3F), yawRad = Math.toRadians(this.moveYaw);
                    float deltaMax = .5F * (int) (collisions * 1.999999F);
                    mc.player.addVelocity(-Math.sin(yawRad) * speedAppend * deltaMax, 0.D, Math.cos(yawRad) * speedAppend * deltaMax);
                }
            }
            if (MoveUtil.isMoving() && this.canAroundRotateUpdated()) {
                this.moveYaw = this.getInputMoveYaw(FreeLookComponent.getFreeYaw());
                final boolean sendRotate = !rageHead();
                if (sendRotate) {
                    event.setYaw(this.moveYaw);
                    mc.player.rotationYawHead = this.moveYaw;
                    mc.player.renderYawOffset = this.moveYaw;
                    if (mc.player.ticksOnGround > 1) mc.player.renderYawOffset = this.moveYaw;
                }
                this.preGround = mc.player.isOnGround();
                return;
            }
            if (MoveUtil.isMoving()) return;
        }
        this.preGround = mc.player.isOnGround();
        this.moveYaw = -999;
    }

    @Override
    public void onDisable() {
        strafeMovement.setOldSpeed(0);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
    }
}
