package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.other.TickEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

import java.util.Random;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ClickPearl", category = Category.PLAYER, desc = "Кидание эндер-жемчуга по бинду")
public class ClickPearl extends Module {
    public static ClickPearl getInstance() {
        return Instance.get(ClickPearl.class);
    }

    private final DelimiterSetting delimiter = new DelimiterSetting(this, "Кидания эндер жемчуга");
    final ModeSetting mode = new ModeSetting(this, "Тип", "Обычный", "Легитный");
    final BindSetting pearlKey = new BindSetting(this, "Кнопка Перки", -1, true);

    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    final Random random = new Random();
    ActionType actionType = ActionType.IDLE;
    int oldSlot = -1;
    int pearlSlot = -1;
    Hand activeHand = Hand.MAIN_HAND;
    long actionStartTime;
    boolean throwPearl;

    @EventHandler
    public void onKey(EventKeyboardMouse e) {
        if (mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
            return;
        }

        if (mc.player.isSpectator() || !mc.player.isAlive()) {
            return;
        }

        if (e.getKey() == pearlKey.getValue()) {
            throwPearl = true;
            if (actionType == ActionType.IDLE) {
                pearlSlot = findPearlSlot();
                if (pearlSlot == -1) return;

                oldSlot = mc.player.inventory.currentItem;
                activeHand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

                if (mode.is("Обычный")) {
                    usePearl(activeHand);
                } else {
                    actionType = ActionType.SWITCH;
                    actionStartTime = System.currentTimeMillis();
                }
            }
        }
    }

    @EventHandler
    public void onMotion(MotionEvent e) {
        if (mode.is("Обычный")) {
            if (throwPearl) {
                if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                    boolean isOffhandEnderPearl = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem;
                    if (isOffhandEnderPearl) {
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.player.swingArm(Hand.MAIN_HAND);
                    } else {
                        int slot = findPearlAndThrow();
                        if (slot > 8) {
                            mc.playerController.pickItem(slot);
                        }
                    }
                }
                throwPearl = false;
            }
        }
    }

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @EventHandler
    private void onPacket(PacketEvent e) {
        this.handUtil.onEventPacket(e);
    }

    private int findPearlAndThrow() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);

            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }

        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);

        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);

            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (actionType != ActionType.IDLE && mode.is("Легитный")) {
            handleLegitMode();
        }
    }

    private void handleLegitMode() {
        long currentTime = System.currentTimeMillis();
        switch (actionType) {
            case SWITCH:
                if (activeHand != Hand.OFF_HAND && pearlSlot != mc.player.inventory.currentItem) {
                    mc.player.inventory.currentItem = pearlSlot;
                }
                if (currentTime - actionStartTime >= 40 + random.nextInt(20)) {
                    actionType = ActionType.USE;
                }
                break;
            case USE:
                usePearl(activeHand);
                actionType = ActionType.SWAP_BACK;
                actionStartTime = currentTime;
                break;
            case SWAP_BACK:
                if (currentTime - actionStartTime >= 60 + random.nextInt(20)) {
                    if (activeHand != Hand.OFF_HAND && oldSlot != mc.player.inventory.currentItem) {
                        mc.player.inventory.currentItem = oldSlot;
                    }
                    resetState();
                }
                break;
        }
    }

    private int findPearlSlot() {
        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();
        float yaw = activeRenderInfo.getYaw();
        float pitch = activeRenderInfo.getPitch();

        return InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
    }

    private void usePearl(Hand hand) {
        mc.playerController.processRightClick(mc.player, mc.world, hand);
        mc.player.swingArm(hand);
    }

    private void resetState() {
        actionType = ActionType.IDLE;
        pearlSlot = -1;
        oldSlot = -1;
        activeHand = Hand.MAIN_HAND;
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    public enum ActionType {
        IDLE, SWITCH, USE, SWAP_BACK
    }
}
