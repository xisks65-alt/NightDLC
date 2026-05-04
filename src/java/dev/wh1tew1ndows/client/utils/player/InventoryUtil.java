package dev.wh1tew1ndows.client.utils.player;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

import java.util.stream.IntStream;

public class InventoryUtil implements IMinecraft {

    @Getter
    private static final InventoryUtil instance = new InventoryUtil();

    public static int findItemSlot(Item item) {
        return findItemSlot(item, true);
    }

    public static int getItemInHotBar(Item item) {
        return IntStream.range(0, 9).filter(i -> mc.player.inventory.getStackInSlot(i).getItem().equals(item)).findFirst().orElse(-1);
    }

    public static int findItemSlot(Item item, boolean armor) {
        if (armor) {
            for (ItemStack stack : mc.player.getArmorInventoryList()) {
                if (stack.getItem() == item) {
                    return -2;
                }
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static int getHotBarSlot(Item item) {
        for (int i = 0; i < 9; ++i) {
            if (InventoryUtil.mc.player.inventory.getStackInSlot(i).getItem() != item) continue;
            return i;
        }
        return 1;
    }

    public static int getSlotIDFromItem(Item item) {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() != item) continue;
            return -2;
        }
        int slot = -1;
        for (int i = 0; i < 36; ++i) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() != item) continue;
            slot = i;
            break;
        }
        if (slot < 9 && slot != -1) {
            slot += 36;
        }
        return slot;
    }


    public static boolean swapAndUse(Item item) {
        int slotId = getSlotIDFromItem(item);
        if (slotId == -1) {
            return false;
        }


        KeyBinding[] pressedKeys = {
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint,
                mc.gameSettings.keyBindSneak
        };

        boolean wasSneaking = mc.player.isSneaking();
        mc.gameSettings.keyBindSneak.setPressed(false);
        if (mc.player.isSneaking()) {
            mc.player.setSneaking(false);
        }

        final TimerUtil timer = new TimerUtil();
        timer.reset();

        new Thread(() -> {
            while (!timer.hasReached(100)) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                }
                mc.gameSettings.keyBindSneak.setPressed(false);
            }

            if (slotId < 9) {
                int oldSlot = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(slotId));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));

                timer.reset();
                while (!timer.hasReached(100)) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    mc.gameSettings.keyBindSneak.setPressed(false);
                }
                mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
            } else {
                int swapSlot = 4;
                if (mc.player.inventory.currentItem == 4) {
                    swapSlot = 5;
                }
                mc.playerController.windowClick(0, slotId, swapSlot, ClickType.SWAP, mc.player);

                timer.reset();
                while (!timer.hasReached(100)) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    mc.gameSettings.keyBindSneak.setPressed(false);
                }

                mc.player.connection.sendPacket(new CHeldItemChangePacket(swapSlot));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));

                timer.reset();
                while (!timer.hasReached(100)) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    mc.gameSettings.keyBindSneak.setPressed(false);
                }
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                timer.reset();
                while (!timer.hasReached(100)) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    mc.gameSettings.keyBindSneak.setPressed(false);
                }
                mc.playerController.windowClick(0, slotId, swapSlot, ClickType.SWAP, mc.player);
                mc.playerController.windowClick(0, slotId, swapSlot, ClickType.QUICK_MOVE, mc.player);
            }

            timer.reset();
            while (!timer.hasReached(100)) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                }
                mc.gameSettings.keyBindSneak.setPressed(false);
            }

            if (wasSneaking && !mc.gameSettings.keyBindSneak.isPressed()) {
            }

        }).start();

        return true;
    }

    public static int findEmptySlot(boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 45;

        for (int i = start; i < end; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                continue;
            }

            return i;
        }
        return -1;
    }

    public static boolean inventorySwapClick(Item item, String nbt, String nbtReq, boolean rotation) {
        int slsl = -1;
        boolean found = false;


        for (int i = 0; i < 9; ++i) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() != item) continue;
            CompoundNBT tag = s.getTag();
            if (tag != null) {
                INBT nbtElement = tag.get(nbt);
                if (nbtElement != null && nbtElement.getString().equals(nbtReq)) {
                    found = true;
                    slsl = i;
                    break;
                }
            }
        }


        if (!found) {
            for (int i = 9; i < 36; ++i) {
                ItemStack s = mc.player.inventory.getStackInSlot(i);
                if (s.getItem() != item) continue;
                CompoundNBT tag = s.getTag();
                if (tag != null) {
                    INBT nbtElement = tag.get(nbt);
                    if (nbtElement != null && nbtElement.getString().equals(nbtReq)) {
                        found = true;
                        slsl = i;
                        break;
                    }
                }
            }
        }


        if (!found) {
            String itemName = "";
            switch (nbtReq) {
                case "ender_eye":
                    itemName = "Дезориентация";
                    break;
                case "netherite_scrap":
                    itemName = "Трапка";
                    break;
                case "dried_kelp":
                    itemName = "Пласт";
                    break;
                case "sugar":
                    itemName = "Явная пыль";
                    break;
                case "phantom_membrane":
                    itemName = "Божья аура";
                    break;
                case "potion-acid":
                    itemName = "Серная кислота";
                    break;
                case "potion-burp":
                    itemName = "Зелье отрыжки";
                    break;
                case "potion-killer":
                    itemName = "Зелье Киллера";
                    break;
                case "potion-medic":
                    itemName = "Зелье Медика";
                    break;
                case "potion-winner":
                    itemName = "Зелье Победителя";
                    break;
                case "potion-agent":
                    itemName = "Зелье Агента";
                    break;
            }


            for (int i = 0; i < 9; ++i) {
                ItemStack s = mc.player.inventory.getStackInSlot(i);
                if (s.getItem() != item) continue;
                if (s.hasDisplayName() && s.getDisplayName().getString().contains(itemName)) {
                    found = true;
                    slsl = i;
                    break;
                }
            }


            if (!found) {
                for (int i = 9; i < 36; ++i) {
                    ItemStack s = mc.player.inventory.getStackInSlot(i);
                    if (s.getItem() != item) continue;
                    if (s.hasDisplayName() && s.getDisplayName().getString().contains(itemName)) {
                        found = true;
                        slsl = i;
                        break;
                    }
                }
            }
        }

        if (!found) {
            // Zetrix.inst().notificationManager.pushNotify("Нет предмета для использования!", NotificationManager.Type.Info);
            // Zetrix.inst().notificationManager().register(("Нет предмета для использования!"), NotificationType.INFO, 1500);
            ChatUtil.addText("Нет предмета для использования!");
            return false;
        }

        inventorySwapClick(item, slsl, rotation);
        return true;
    }


    static void inventorySwapClick(Item item, int slsl, boolean rotation) {

        KeyBinding[] pressedKeys = {
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint,
                mc.gameSettings.keyBindSneak
        };

        for (KeyBinding keyBinding : pressedKeys) {
            keyBinding.setPressed(false);
        }


        boolean wasSneaking = mc.player.isSneaking();
        mc.gameSettings.keyBindSneak.setPressed(false);
        if (mc.player.isSneaking()) {
            mc.player.setSneaking(false);
        }

        final TimerUtil timer = new TimerUtil();
        timer.reset();

        new Thread(() -> {
            while (!timer.hasReached(180)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                mc.gameSettings.keyBindSneak.setPressed(false);
            }

            if (slsl != -1) {
                if (doesHotbarHaveItem(item)) {
                    if (slsl != mc.player.inventory.currentItem) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(slsl));
                    }
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                    if (slsl == mc.player.inventory.currentItem) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            boolean press = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
                            keyBinding.setPressed(press);
                        }
                        //  if (curgui) Zetrix.inst().getModuleList().guiMove.setState(true);
                        //  if (shouldDisableMove) {
                        //      Zetrix.inst().disableMove = false;
                        //  }
                        return;
                    }

                    timer.reset();
                    while (!timer.hasReached(30)) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                        }
                    }

                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                } else {
                    int swapSlot = 4;
                    if (mc.player.inventory.currentItem == 4) {
                        swapSlot = 5;
                    }

                    mc.playerController.windowClick(0, slsl, swapSlot, ClickType.SWAP, mc.player);
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(swapSlot));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));

                    timer.reset();
                    while (!timer.hasReached(5)) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                    }

                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    mc.playerController.windowClick(0, slsl, swapSlot, ClickType.SWAP, mc.player);
                    mc.playerController.windowClick(0, slsl, swapSlot, ClickType.QUICK_MOVE, mc.player);
                }
            }

            timer.reset();
            while (!timer.hasReached(200)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }

            for (KeyBinding keyBinding : pressedKeys) {
                boolean press = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
                keyBinding.setPressed(press);
            }
            // if (curgui) Zetrix.inst().getModuleList().guiMove.setState(true);
            // if (shouldDisableMove) {
            //     Zetrix.inst().disableMove = false;
            // }

            if (wasSneaking && !mc.gameSettings.keyBindSneak.isPressed()) {
            }
        }).start();
    }


    public static void moveItem(int from, int to, boolean air) {

        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        if (air)
            pickupItem(from, 0);
    }

    public static int getItemIndex(Item item) {
        for (int i = 0; i < 45; ++i) {
            if (Minecraft.getInstance().player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    public static void inventorySwapClick(Item item) {
        int n;
        if (getItemIndex(item) == -1) return;
        if (doesHotbarHaveItem(item)) {
            for (n = 0; n < 9; ++n) {
                if (mc.player.inventory.getStackInSlot(n).getItem() != item) continue;
                boolean bl = false;
                if (n != mc.player.inventory.currentItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(n));
                    bl = true;
                }
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                if (!bl) break;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                break;
            }
        }
        if (!doesHotbarHaveItem(item)) {
            for (n = 0; n < 36; ++n) {
                if (mc.player.inventory.getStackInSlot(n).getItem() != item) continue;
                mc.playerController.windowClick(0, n, mc.player.inventory.currentItem % 8 + 1,
                        ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.playerController.windowClick(0, n, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                break;
            }
        }
    }


    public static void inventorySwapClick2(Item item) {
        int n;
        if (getItemIndex(item) == -1) return;
        if (doesHotbarHaveItem(item)) {
            for (n = 0; n < 9; ++n) {
                if (mc.player.inventory.getStackInSlot(n).getItem() != item) continue;
                boolean bl = false;
                if (n != mc.player.inventory.currentItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(n));
                    bl = true;
                }
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                if (!bl) break;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                break;
            }
        }
        if (!doesHotbarHaveItem(item)) {
            for (n = 0; n < 36; ++n) {
                if (mc.player.inventory.getStackInSlot(n).getItem() != item) continue;
                mc.playerController.windowClick(0, n, mc.player.inventory.currentItem % 8 + 1,
                        ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.playerController.windowClick(0, n, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                break;
            }
        }
    }

    public static boolean doesHotbarHaveItem(Item item) {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    public static void moveItemTime(int from, int to, boolean air, int time) {

        if (from == to)
            return;
        pickupItem(from, 0, time);
        pickupItem(to, 0, time);
        if (air)
            pickupItem(from, 0, time);
    }

    public static void moveItem(int from, int to) {
        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        pickupItem(from, 0);
    }

    public static void pickupItem(int slot, int button) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void pickupItem(int slot, int button, int time) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void clickSlot(Slot slot, int button, ClickType clickType, boolean packet) {
        if (slot != null) clickSlotId(slot.slotNumber, button, clickType, packet);
    }

    public static void clickSlotId(int slotId, int buttonId, ClickType clickType, boolean packet) {
        clickSlotId(mc.player.openContainer.windowId, slotId, buttonId, clickType, packet);
    }

    public static Slot getAxeSlot() {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem() instanceof AxeItem).findFirst().orElse(null);
    }


    public static void clickSlotId(int windowId, int slotId, int buttonId, ClickType clickType, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CClickWindowPacket(windowId, slotId, buttonId, clickType, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
        } else {
            mc.playerController.windowClick(windowId, slotId, buttonId, clickType, mc.player);
        }
    }

    public int getAxeInInventory(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public int findBestSlotInHotBar() {
        int emptySlot = findEmptySlot();
        if (emptySlot != -1) {
            return emptySlot;
        } else {
            return findNonSwordSlot();
        }
    }


    private int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty() && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    private int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof SwordItem) && !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ElytraItem) && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventory(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public int getSlotInInventoryOrHotbar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int getSlotInInventoryOrHotbar() {
        int firstSlot = 0;
        int lastSlot = 9;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {

            if (Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).getItem()) instanceof SlabBlock) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static class Hand {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

        public void onEventPacket(PacketEvent eventPacket) {
            if (!eventPacket.isReceive()) {
                return;
            }
            if (eventPacket.getPacket() instanceof SHeldItemChangePacket) {
                this.isChangingItem = true;
            }
        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                mc.player.inventory.currentItem = this.originalSlot;
                if (resetItem) {
                    this.isChangingItem = false;
                    this.originalSlot = -1;
                    isEnabled = false;
                }
            }
        }

        public void setOriginalSlot(int slot) {
            this.originalSlot = slot;
        }
    }

}
