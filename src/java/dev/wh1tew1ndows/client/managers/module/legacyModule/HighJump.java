package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "HighJump", category = Category.MOVEMENT, desc = "Увеличение высоты прыжка")
public class HighJump extends Module {

    private float yBoost = 0f; // глобально в классе

    @EventHandler
    public void onEvent(MotionEvent event) {
        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (tile instanceof ShulkerBoxTileEntity) {
                if (Math.sqrt(Math.pow(mc.player.getPosX() - (tile.getPos().getX() + 0.5), 2) + Math.pow(mc.player.getPosZ() - (tile.getPos().getZ() + 0.5), 2)) <= 1 && Math.abs(mc.player.getPosY() - (tile.getPos().getY() + 0.5)) <= (mc.player.motion.y > 1 ? 54 : 35) && mc.player.fallDistance == 0) {


                    if (((ShulkerBoxTileEntity) tile).getProgress(2.5f) > 0.0f
                            && ((ShulkerBoxTileEntity) tile).getProgress(2.5f) != 2.5) {

                        if (yBoost < 16) {
                            yBoost += 0.11f; // скорость прироста (можешь менять, например 0.05f для плавнее)
                        }
                        mc.player.setMotion(mc.player.getMotion().x, yBoost, mc.player.getMotion().z);

                    } else {
                        yBoost = 0f; // сбрасываем, когда условие не выполняется
                    }

                }
            }
        }
    }
}
