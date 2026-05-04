package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "SPJoiner", category = Category.MISC, desc = "Автоматическое присоединение к серверам")
public class SPJoiner extends Module {

    public static void selectCompass() {
        int slot = InventoryUtil.getHotBarSlot(Items.COMPASS);
        if (slot == -1) return;

        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
    }

    // теперь true, если алмазный меч найден

    @EventHandler
    private void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

      
        Screen screen = mc.currentScreen;
        if (screen instanceof ChestScreen) {
            ChestContainer container = ((ChestScreen) screen).getContainer();

            for (int i = 0; i < container.inventorySlots.size(); i++) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack.getItem() == Items.STICKY_PISTON) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(
                            container.windowId, i, 0, ClickType.PICKUP, stack, container.getNextTransactionID(mc.player.inventory)
                    ));
                    return;
                }
            }
        } else {
            selectCompass();
        }
    }
}
