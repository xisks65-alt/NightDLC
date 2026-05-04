package dev.wh1tew1ndows.client.managers.module.impl.combat.aura;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

@UtilityClass
public class TralaleloTralala implements IMinecraft {

    @Setter
    @Getter
    private boolean jumped;

    public float getAICooldown() {

        if (mc.player.getHeldItemMainhand().getItem() == Items.AIR) {
            return 1;
        }

        if (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.LEVITATION)
                || mc.player.isPotionActive(Effects.SLOW_FALLING)
                || mc.player.isInLava()
                || mc.player.isInWater()
                || mc.player.isOnLadder()
                || mc.player.isPassenger()
                || PlayerUtil.isPlayerInWeb()
                || mc.player.isElytraFlying()
                || mc.player.abilities.isFlying)
            return 0.944f;

        if (mc.player.getHeldItemMainhand().getItem() instanceof AxeItem || mc.player.getHeldItemMainhand().getItem() instanceof ShovelItem)
            return 0.99f;

        return 0.944f;
    }

    public float getNewFallDistance(LivingEntity target) {


        return 0;
    }

    public boolean canAIFall() {

        return ((PlayerUtil.getBlock(0, 3, 0) == Blocks.AIR && PlayerUtil.getBlock(0, 2, 0) == Blocks.AIR && PlayerUtil.getBlock(0, 1, 0) == Blocks.AIR)
                || mc.player.fallDistance < (PlayerUtil.getBlock(0, 2, 0) != Blocks.AIR ? 0.08f : 0.6f)
                || mc.player.fallDistance > 1.2f);
    }

    private boolean isBlockBelow() {
        Vector3d pos = mc.player.getPositionVec().add(0, -1, 0);
        AxisAlignedBB hitbox = mc.player.getBoundingBox();

        float off = 0.15f;

        return !isAir(hitbox.minX - off, pos.y, hitbox.minZ - off)
                || !isAir(hitbox.maxX + off, pos.y, hitbox.minZ - off)
                || !isAir(hitbox.minX - off, pos.y, hitbox.maxZ + off)
                || !isAir(hitbox.maxX + off, pos.y, hitbox.maxZ + off);
    }

    private boolean isAir(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR;
    }

}
