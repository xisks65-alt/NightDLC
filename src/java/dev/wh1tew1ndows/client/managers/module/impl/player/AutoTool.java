package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockRayTraceResult;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoTool", category = Category.PLAYER, desc = "Автоматический выбор лучшего инструмента")
public class AutoTool extends Module {
    public static AutoTool getInstance() {
        return Instance.get(AutoTool.class);
    }


    int itemIndex = -1;
    int oldSlot = -1;
    boolean status;

    @Override
    public void onDisable() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;
        super.onDisable();
    }

    private int findBestToolSlotInHotBar() {
        Object object = mc.objectMouseOver;
        if (object instanceof BlockRayTraceResult blockRayTraceResult) {
            object = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();
            int n = -1;
            float f = 1.0f;
            for (int i = 0; i < 9; ++i) {
                float f2 = mc.player.inventory.getStackInSlot(i).getDestroySpeed(((Block) object).getDefaultState());
                if (!(f2 > f)) continue;
                f = f2;
                n = i;
            }
            return n;
        }
        return 1;
    }


    private boolean isMousePressed() {
        return mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown();
    }

    @EventHandler
    public void event(Render2DEvent event) {
        if (mc.player == null || mc.player.isCreative()) {
            itemIndex = -1;
            return;
        }
        if (isMousePressed()) {
            itemIndex = findBestToolSlotInHotBar();
            if (itemIndex != -1) {
                status = true;
                if (oldSlot == -1) {
                    oldSlot = mc.player.inventory.currentItem;
                }
                mc.player.inventory.currentItem = itemIndex;
            }
        } else if (status && oldSlot != -1) {
            mc.player.inventory.currentItem = oldSlot;
            itemIndex = oldSlot;
            status = false;
            oldSlot = -1;
        }
    }
}