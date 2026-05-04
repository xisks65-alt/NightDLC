//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.StopWatchShadow;
import dev.wh1tew1ndows.client.utils.SwapHelpers;
import dev.wh1tew1ndows.client.utils.other.LockManager;
import dev.wh1tew1ndows.client.managers.module.impl.movement.InvMove;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;

@ModuleInfo(
        name = "AutoSwap",
        category = Category.COMBAT,
        desc = "Автоматическая смена предмета в левой руке по бинду"
)
public class AutoSwap extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "Обычный", "Spooky");
    private final ModeSetting firstItemSetting = new ModeSetting(this, "Первый предмет", "Шар", "Золотое яблоко", "Щит", "Тотем");
    private final ModeSetting secondItemSetting = new ModeSetting(this, "Второй предмет", "Шар", "Золотое яблоко", "Щит", "Тотем");
    private final BindSetting bind = new BindSetting(this, "Кнопка", -1);
    private final BooleanSetting swaprender = new BooleanSetting(this, "Отображать свапнутый предмет", true);
    private final BooleanSetting onlyEnchanted = new BooleanSetting(this, "Только зачарованные тотемы", false);
    private final SwapHelpers swaps = new SwapHelpers();
    private boolean swap;
    private boolean hand;
    private final StopWatchShadow swapWatch = new StopWatchShadow();

    // Отслеживание реального свапа для уведомлений
    private Item lastOffhandItem = Items.AIR;


    @EventHandler
    public void update(UpdateEvent event) {
        if (mc.player != null) {
            lastOffhandItem = mc.player.getHeldItemOffhand().getItem();
        }

        if (this.swap && this.hand) {
            if (this.firstItemSetting.getValue().equals("Шар")) {
                this.swap(Items.PLAYER_HEAD, "Шар", false);
            } else if (this.firstItemSetting.getValue().equals("Тотем")) {
                this.swap(Items.TOTEM_OF_UNDYING, "Тотем", this.onlyEnchanted.getValue());
            } else if (this.firstItemSetting.getValue().equals("Золотое яблоко")) {
                this.swap(Items.GOLDEN_APPLE, "Золотое яблоко", false);
            } else if (this.firstItemSetting.getValue().equals("Щит")) {
                this.swap(Items.SHIELD, "Щит", false);
            }
            this.hand = false;
        }

        if (this.swap) {
            if (this.secondItemSetting.getValue().equals("Шар")) {
                this.swap(Items.PLAYER_HEAD, "Шар", false);
            } else if (this.secondItemSetting.getValue().equals("Золотое яблоко")) {
                this.swap(Items.GOLDEN_APPLE, "Золотое яблоко", false);
            } else if (this.secondItemSetting.getValue().equals("Тотем")) {
                this.swap(Items.TOTEM_OF_UNDYING, "Тотем", this.onlyEnchanted.getValue());
            } else if (this.secondItemSetting.getValue().equals("Щит")) {
                this.swap(Items.SHIELD, "Щит", false);
            }
            this.hand = true;
        }
    }

    @EventHandler
    public void input(EventKeyboardMouse event) {
        this.swap = event.isKeyDown(bind.getValue());

    }

    private void swap(Item item, String itemName, boolean onlyEnchanted) {
        if (LockManager.isLocked()) {
            this.swap = false;
            return;
        }

        boolean spooky = this.mode.getValue().equals("Spooky");

        // Spooky: синхронизация с InvMove встроена — ждём пока он не в стопе
        if (spooky) {
            InvMove invMove = InvMove.getInstance();
            if (invMove != null && invMove.isEnabled() && invMove.stoppedStatus()) {
                return;
            }
        }

        int slot = item == Items.TOTEM_OF_UNDYING ? this.swaps.find(item, false, onlyEnchanted) : this.swaps.find(item);
        if (slot != -1) {
            InvMove invMove = InvMove.getInstance();
            if (spooky && invMove != null && invMove.isEnabled() && !invMove.type().is("Обычный")) {
                int containerSlot = slot < 9 ? slot + 36 : slot;
                CClickWindowPacket pkt = new CClickWindowPacket(0, containerSlot, 40, ClickType.SWAP,
                        mc.player.openContainer.getSlot(containerSlot).getStack(),
                        mc.player.openContainer.getNextTransactionID(mc.player.inventory));
                invMove.queueSwapPacket(pkt);
            } else {
                mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 40, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket());
            }
            if (swaprender.getValue())
                InterFace.getInstance().autoSwapRenderer().trigger(itemName);
        }
        this.swap = false;
    }

    public boolean isMoving() {
        return mc.player.moveForward != 0 || mc.player.moveStrafing != 0;
    }

}
