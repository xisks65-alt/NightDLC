package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DragSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Flight", category = Category.MOVEMENT, desc = "Позволяет летать в режиме выживания")
public class Flight extends Module {
    public static Flight getInstance() {
        return Instance.get(Flight.class);
    }

    public final ModeSetting mode = new ModeSetting(this, "Режим", "Motion", "ReallyWorld");

    private final SliderSetting speed = new SliderSetting(this, "Скорость", 1.5F, 0.1F, 10F, 0.1F).setVisible(() -> mode.is("Motion"));

    private final SliderSetting speedY = new SliderSetting(this, "Скорость", 3, 1, 6, 1).setVisible(() -> mode.is("FunTimeY"));


    private final DragSetting drag = new DragSetting(this, "Position");

    @EventHandler
    public void onEvent(PacketEvent event) {

    }

    private final float yBoost = 0f; // глобально в классе

    @EventHandler
    public void onEvent(MotionEvent event) {
        if (mode.is("Motion")) {
            mc.player.setMotion(0, 0, 0);
            boolean isSneaking = mc.gameSettings.keyBindSneak.isKeyDown();
            boolean isJumping = mc.gameSettings.keyBindJump.isKeyDown();
            float motionSpeed = speed.getValue() / 2F;
            if (isSneaking) {
                mc.player.motion.y = -motionSpeed;
            } else if (isJumping) {
                mc.player.motion.y = motionSpeed;
            }
            MoveUtil.setSpeed(speed.getValue());
        } else if (mode.is("ReallyWorld")) {
            if (mc.world.getBlockState(mc.player.getPosition().down()).isAir() && mc.player.fallDistance > 0.14) {
                mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPosition().down(4).getVec(), Direction.UP, mc.player.getPosition().down(4), false));
                mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPosition().down(3).getVec(), Direction.UP, mc.player.getPosition().down(3), false));
                mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPosition().down(2).getVec(), Direction.UP, mc.player.getPosition().down(2), false));
                mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPosition().down(1).getVec(), Direction.UP, mc.player.getPosition().down(1), false));
            }
        }
    }
}