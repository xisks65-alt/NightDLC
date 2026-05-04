package dev.wh1tew1ndows.client.managers.module.legacyModule;


import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name = "Jesus", category = Category.MOVEMENT, desc = "Хождение по воде")
public class Jesus extends Module {
    private int ticks;
    private final ModeSetting modeValue = new ModeSetting(this, "Мод", "Безопасный", "Обычный", "Быстрый");

    @EventHandler
    public void onMotion(MotionEvent e) {
        BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.008D, mc.player.getPosZ());
        Block playerBlock = mc.world.getBlockState(playerPos).getBlock();

        if (playerBlock == Blocks.WATER && !mc.player.isOnGround()) {
            switch (modeValue.getValue()) {
                case "Безопасный":
                    safeJesus(e);
                    break;
                case "Обычный":
                    normalJesus(e);
                    break;
                case "Быстрый":
                    fastJesus(e);
                    break;
            }
        }
    }

    private void safeJesus(MotionEvent e) {
        float moveSpeed = 1.03F;
        mc.player.setVelocity(mc.player.motion.x * moveSpeed, 0.0D, mc.player.motion.z * moveSpeed);
    }

    private void normalJesus(MotionEvent e) {
        float moveSpeed = 1.08f;
        mc.player.setVelocity(mc.player.motion.x * moveSpeed, 0.0D, mc.player.motion.z * moveSpeed);
    }

    private void fastJesus(MotionEvent e) {
        float moveSpeed = 1.16f;
        mc.player.setVelocity(mc.player.motion.x * moveSpeed, 0.0D, mc.player.motion.z * moveSpeed);
    }
}