package dev.wh1tew1ndows.client.utils.rotation;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.impl.movement.AirStuck;
import dev.wh1tew1ndows.client.managers.module.impl.player.FreeCam;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;

public class AttackUtil implements IMinecraft {


    static boolean canCrit = false;

    public static boolean isAttack(boolean onlyCrit, boolean onlySpace, boolean canCrit) {

        boolean cancelReason =
                mc.player.areEyesInFluid(FluidTags.WATER) && mc.player.movementInput.jump
                        || mc.player.areEyesInFluid(FluidTags.LAVA) && mc.player.movementInput.jump
                        || mc.player.isInWater() && mc.player.areEyesInFluid(FluidTags.WATER)
                        || mc.player.isInLava() && mc.player.areEyesInFluid(FluidTags.LAVA)
                        || mc.player.isOnLadder()
                        || mc.player.isRidingHorse()
                        || PlayerUtil.isPlayerInWeb()
                        || Zetrix.inst().moduleManager().get(AirStuck.class).isEnabled()
                        || Zetrix.inst().moduleManager().get(FreeCam.class).isEnabled()
                        || mc.player.isPassenger()
                        || mc.player.abilities.isFlying
                        || mc.player.isPotionActive(Effects.LEVITATION)
                        || mc.player.isPotionActive(Effects.BLINDNESS)
                        || mc.player.isPotionActive(Effects.SLOW_FALLING);
        boolean onSpace = !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() && onlySpace;

        if (!cancelReason && onlyCrit) {
            return onSpace && mc.player.ticksOnGround > 6 && mc.player.fallDistance < 0.15F || mc.player.fallDistance > Mathf.randomValue(0.02F, 0.09F) && canCrit && PlayerUtil.isBlockAboveHead();
        }

        return true;
    }
}
