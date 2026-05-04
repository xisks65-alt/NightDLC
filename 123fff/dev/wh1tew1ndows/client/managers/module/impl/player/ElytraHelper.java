package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.utils.InventoryUtil;
import dev.wh1tew1ndows.client.utils.MovementManager;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;

import java.util.Random;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ElytraHelper", category = Category.PLAYER, desc = "Помощник для полета на элитрах")
public class ElytraHelper extends Module {

    final BindSetting swapChestKey = new BindSetting(this, "Свап на нагрудник", -1);
    final BindSetting fireworkKey = new BindSetting(this, "Фейерверк", -1);
    final BooleanSetting autoFly = new BooleanSetting(this, "Авто полёт", true);
    final BooleanSetting noUse = new BooleanSetting(this, "Не использовать при еде", true);
    final BooleanSetting invMoveSync = new BooleanSetting(this, "Синхронизировать с InvMove", false);
  
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    ItemStack currentStack = ItemStack.EMPTY;
    public static TimerUtil stopWatch = new TimerUtil();
    Task current;
    long delay;
    boolean fireworkUsed;
    SwapState chestSwapState = SwapState.IDLE;
    final TimerUtil swap = new TimerUtil();
    final TimerUtil swap2 = new TimerUtil();
    final TimerUtil swap3 = new TimerUtil();

    // Логика фейерверков для инвентаря
    SwapState fireworkSwapState = SwapState.IDLE;
    final TimerUtil fireworkActionTimer = new TimerUtil();
    int fireworkSlot = -1;
    int fireworkSwappedFromSlot = -1;
    boolean isSwappingFirework;

    // Для хотбара (легитимные фейерверки)
    enum FireworkLegitState {IDLE, SWITCH, USE, SWAP_BACK}

    FireworkLegitState fireworkLegitState = FireworkLegitState.IDLE;
    int oldHotbarSlot = -1;
    int fireworkHotbarSlot = -1;
    long fireworkActionStart;
    final Random random = new Random();

    enum SwapState {
        IDLE, SWAPPING, SWAPPED, USING, RETURNING, CLOSING
    }

    @EventHandler
    private void onEventKey(EventKeyboardMouse e) {
        if (e.getKey() == swapChestKey.getValue() &&
                !(noUse.getValue() && mc.player.isHandActive() && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD)) {
            ItemStack chestItem = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            int targetSlot = chestItem.getItem() == Items.ELYTRA ? getChestPlateSlot() : getItemSlot(Items.ELYTRA);
            if (targetSlot >= 0 && chestSwapState == SwapState.IDLE) {
                MovementManager.getInstance().lockMovement("ElytraHelper");
                chestSwapState = SwapState.SWAPPING;
                currentStack = chestItem;
                swap.reset();
                swap2.reset();
                swap3.reset();
                stopWatch.reset();
            }
        }

        if (e.getKey() == fireworkKey.getValue() && !(noUse.getValue() && mc.player.isHandActive() &&
                mc.player.getHeldItemOffhand().getItem() != Items.SHIELD)) {

            // Авто-определение: пробуем сначала хотбар, потом инвентарь
            int hotbarSlot = getFireworkHotbarOnly();
            if (hotbarSlot != -1 && mc.player.isElytraFlying() && fireworkLegitState == FireworkLegitState.IDLE) {
                // Используем легитимную логику для хотбара
                oldHotbarSlot = mc.player.inventory.currentItem;
                fireworkHotbarSlot = hotbarSlot;
                fireworkLegitState = FireworkLegitState.SWITCH;
                fireworkActionStart = System.currentTimeMillis();
            } else if (fireworkSwapState == SwapState.IDLE) {
                // Используем логику для инвентаря
                fireworkUsed = true;
                isSwappingFirework = true;
                fireworkSwapState = SwapState.SWAPPING;
                fireworkActionTimer.reset();
                MovementManager.getInstance().lockMovement("ElytraHelper");
            }
        }
    }

    @EventHandler
    private void onMotion(MotionEvent e) {
        // Обработка легитимных фейерверков из хотбара
        if (fireworkLegitState != FireworkLegitState.IDLE) {
            handleFireworkLegit();
        }

        // Обработка фейерверков из инвентаря
        if (fireworkUsed && fireworkSwapState != SwapState.IDLE) {
            handleFireworkMotion();
        }
    }

    private void handleFireworkMotion() {
        if (fireworkSwapState == SwapState.SWAPPING) {
            fireworkSlot = getFireworkSlot();
            if (fireworkSlot == 40 && mc.player.getHeldItemOffhand().getItem() == Items.FIREWORK_ROCKET) {
                fireworkSwapState = SwapState.SWAPPED;
                fireworkSwappedFromSlot = 40;
            } else if (fireworkSlot != -1) {
                startFireworkSwap(fireworkSlot);
            } else {
                resetFireworkState();
            }
        } else if (fireworkSwapState == SwapState.SWAPPED) {
            useFirework();
        } else if (fireworkSwapState == SwapState.USING && fireworkActionTimer.isReached(150L)) {
            returnFirework();
        } else if (fireworkSwapState == SwapState.RETURNING) {
            fireworkSwapState = SwapState.CLOSING;
            resetFireworkState();
        }
    }

    private void handleFireworkLegit() {
        long now = System.currentTimeMillis();
        switch (fireworkLegitState) {
            case SWITCH:
                if (fireworkHotbarSlot != mc.player.inventory.currentItem) {
                    mc.player.inventory.currentItem = fireworkHotbarSlot;
                }
                if (now - fireworkActionStart >= 25 + random.nextInt(20)) {
                    fireworkLegitState = FireworkLegitState.USE;
                    fireworkActionStart = now;
                }
                break;
            case USE:
                try {
                    mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
                    mc.player.swingArm(Hand.MAIN_HAND);
                } catch (Exception ignored) {
                }
                fireworkLegitState = FireworkLegitState.SWAP_BACK;
                fireworkActionStart = now;
                break;
            case SWAP_BACK:
                if (now - fireworkActionStart >= 45 + random.nextInt(15)) {
                    if (oldHotbarSlot != mc.player.inventory.currentItem) {
                        mc.player.inventory.currentItem = oldHotbarSlot;
                    }
                    fireworkLegitState = FireworkLegitState.IDLE;
                    oldHotbarSlot = -1;
                    fireworkHotbarSlot = -1;
                }
                break;
            case IDLE:
            default:
                break;
        }
    }

    private void startFireworkSwap(int slot) {
        if (mc.player.container == null || mc.currentScreen != null || !mc.player.isElytraFlying() || mc.player == null || mc.world == null) {
            resetFireworkState();
            return;
        }

        MovementManager.getInstance().lockMovement("ElytraHelper");
        fireworkSwappedFromSlot = slot;

        try {
            if (slot != 40) {
                mc.playerController.windowClick(mc.player.container.windowId, slot < 9 ? slot + 36 : slot, 40, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.container.windowId));
            }
            fireworkSwapState = SwapState.SWAPPED;
            fireworkActionTimer.reset();
        } catch (Exception ex) {
            resetFireworkState();
        }
    }

    private void useFirework() {
        MovementManager.getInstance().lockMovement("ElytraHelper");
        try {
            mc.playerController.processRightClick(mc.player, mc.world, Hand.OFF_HAND);
            mc.player.swingArm(Hand.OFF_HAND);
            fireworkSwapState = SwapState.USING;
            delay = System.currentTimeMillis();
            fireworkActionTimer.reset();
        } catch (Exception ex) {
            resetFireworkState();
        }
    }

    private void returnFirework() {
        if (mc.player.container == null || mc.currentScreen != null) {
            resetFireworkState();
            return;
        }

        MovementManager.getInstance().lockMovement("ElytraHelper");
        try {
            if (fireworkSwappedFromSlot != -1 && fireworkSwappedFromSlot != 40) {
                mc.playerController.windowClick(mc.player.container.windowId, fireworkSwappedFromSlot < 9 ? fireworkSwappedFromSlot + 36 : fireworkSwappedFromSlot, 40, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.container.windowId));
            }
            fireworkSwapState = SwapState.RETURNING;
            fireworkActionTimer.reset();
        } catch (Exception ex) {
            resetFireworkState();
        }
    }

    private void resetFireworkState() {
        fireworkUsed = false;
        fireworkSwapState = SwapState.IDLE;
        fireworkSwappedFromSlot = -1;
        fireworkSlot = -1;
        isSwappingFirework = false;
        if (chestSwapState == SwapState.IDLE) {
            MovementManager.getInstance().unlockMovement("ElytraHelper");
        }
    }

    private int getFireworkSlot() {
        if (mc.player.getHeldItemOffhand().getItem() == Items.FIREWORK_ROCKET) {
            return 40;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.FIREWORK_ROCKET) {
                return i + 36;
            }
        }
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.FIREWORK_ROCKET) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    private int getFireworkHotbarOnly() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        if (chestSwapState != SwapState.IDLE || fireworkSwapState != SwapState.IDLE) {
            MovementManager.getInstance().lockMovement("ElytraHelper");
        } else {
            MovementManager.getInstance().unlockMovement("ElytraHelper");
        }


        holyWorld();


        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 280L);
    }

    private void holyWorld() {
        if (mc.currentScreen != null) {
            chestSwapState = SwapState.IDLE;
            if (fireworkSwapState == SwapState.IDLE) {
                MovementManager.getInstance().unlockMovement("ElytraHelper");
            }
            return;
        }

        ItemStack itemStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (chestSwapState == SwapState.SWAPPING && !MoveUtil.isMoving()) {
            if (!MoveUtil.isMoving()) {
                int elytraSlot = getItemSlot(Items.ELYTRA);
                int chestplateSlot = getChestPlateSlot();
                if (itemStack.getItem() != Items.ELYTRA && elytraSlot >= 0) {
                    if (elytraSlot >= 36 && elytraSlot <= 44) {
                        mc.playerController.windowClick(0, 6, elytraSlot % 9, ClickType.SWAP, mc.player);
                    } else {
                        mc.playerController.windowClick(0, elytraSlot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.playerController.windowClick(0, 6, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.playerController.windowClick(0, elytraSlot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                    }
                    chestSwapState = SwapState.CLOSING;
                } else if (itemStack.getItem() == Items.ELYTRA && chestplateSlot >= 0) {
                    if (chestplateSlot >= 36 && chestplateSlot <= 44) {
                        mc.playerController.windowClick(0, 6, chestplateSlot % 9, ClickType.SWAP, mc.player);
                    } else {
                        mc.playerController.windowClick(0, chestplateSlot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.playerController.windowClick(0, 6, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.playerController.windowClick(0, chestplateSlot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                    }
                    chestSwapState = SwapState.CLOSING;
                } else {
                    chestSwapState = SwapState.IDLE;
                    if (fireworkSwapState == SwapState.IDLE) {
                        MovementManager.getInstance().unlockMovement("ElytraHelper");
                    }
                }
            }
        }

        if (chestSwapState == SwapState.CLOSING && mc.currentScreen == null) {
            mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
            chestSwapState = SwapState.IDLE;
            if (fireworkSwapState == SwapState.IDLE) {
                MovementManager.getInstance().unlockMovement("ElytraHelper");
            }
        }

        if (autoFly.getValue() && itemStack.getItem() == Items.ELYTRA
                && mc.currentScreen == null
                && MoveUtil.isMoving()
                && !mc.player.isOnLadder() && !mc.player.isInLava() && !mc.player.isInWater()
                && !mc.player.isPotionActive(Effects.SLOW_FALLING) && !mc.player.isPotionActive(Effects.LEVITATION)
                && !mc.player.isRidingHorse()) {
            if (stopWatch.isReached(150L)) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                } else if (ElytraItem.isUsable(itemStack) && !mc.player.isElytraFlying() && !mc.player.abilities.isFlying) {
                    mc.player.startFallFlying();
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                    stopWatch.reset();
                }
            }
        }
    }


    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null || chestSwapState != SwapState.SWAPPING) return;
        MovementManager.getInstance().lockMovement("ElytraHelper");

        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                current = new Task(elytraSlot, 6);
                mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                chestSwapState = SwapState.CLOSING;
            } else {
                chestSwapState = SwapState.IDLE;
                if (fireworkSwapState == SwapState.IDLE) {
                    MovementManager.getInstance().unlockMovement("ElytraHelper");
                }
            }
        } else {
            int armorSlot = getChestPlateSlot();
            if (armorSlot >= 0) {
                current = new Task(armorSlot, 6);
                mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                chestSwapState = SwapState.CLOSING;
            } else {
                chestSwapState = SwapState.IDLE;
                if (fireworkSwapState == SwapState.IDLE) {
                    MovementManager.getInstance().unlockMovement("ElytraHelper");
                }
            }
        }
    }

    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
                Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }

    private int getItemSlot(Item input) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        stopWatch.reset();
        chestSwapState = SwapState.IDLE;
        fireworkSwapState = SwapState.IDLE;
        fireworkLegitState = FireworkLegitState.IDLE;
        current = null;
        fireworkUsed = false;
        fireworkSwappedFromSlot = -1;
        fireworkSlot = -1;
        isSwappingFirework = false;
        oldHotbarSlot = -1;
        fireworkHotbarSlot = -1;
        MovementManager.getInstance().unlockMovement("ElytraHelper");
        super.onDisable();
    }

    @RequiredArgsConstructor
    class Task {
        final int one, two;
        int stage;
    }
}