//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wh1tew1ndows.client.utils;

import com.google.common.eventbus.Subscribe;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class SwapHelpers implements IMinecraft {
    public int find(int id) {
        int slot = -1;

        for (int i = 0; i < 36; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && mc.player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    slot = i;
                    break;
                }
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public int find(Item item) {
        int slot = -1;

        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == item) {
                return -2;
            }
        }

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public int find(Item item, boolean ignoreEnchanted) {
        return find(item, ignoreEnchanted, false);
    }

    public int find(Item item, boolean ignoreEnchanted, boolean onlyEnchanted) {
        int slot = -1;

        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == item) {
                if (onlyEnchanted && stack.isEnchanted()) {
                    return -2;
                }
                if (!onlyEnchanted && (!ignoreEnchanted || !stack.isEnchanted())) {
                    return -2;
                }
            }
        }

        // Если нужны только зачарованные
        if (onlyEnchanted) {
            for (int i = 0; i < 36; ++i) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() == item && stack.isEnchanted()) {
                    slot = i;
                    break;
                }
            }
        } else {
            // Сначала ищем незачарованные
            for (int i = 0; i < 36; ++i) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() == item && !stack.isEnchanted()) {
                    slot = i;
                    break;
                }
            }

            // Если не нашли незачарованные и не игнорируем зачарованные, ищем любые
            if (slot == -1 && !ignoreEnchanted) {
                for (int i = 0; i < 36; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (stack.getItem() == item) {
                        slot = i;
                        break;
                    }
                }
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public int find(UseAction action) {
        int slot = -1;

        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getUseAction() == action) {
                return -2;
            }
        }

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getUseAction() == action) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public int find() {
        RayTraceResult var2 = mc.objectMouseOver;
        if (var2 instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();
            int bestSlot = -1;
            float bestSpeed = 1.0F;

            for (int slot = 0; slot < 9; ++slot) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);
                float speed = stack.getDestroySpeed(block.getDefaultState());
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }

            return bestSlot;
        } else {
            return -1;
        }
    }

    public int find(ItemStack itemStack) {
        int slot = -1;

        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack == itemStack) {
                return -2;
            }
        }

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == itemStack) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public boolean haveHotBar(Item item) {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }


    public boolean haveHotBar(int index) {
        return index >= 36 && index <= 44;
    }

    public int format(int slot) {
        return slot - 36;
    }

    public int find(String name) {
        int slot = -1;
        ContainerScreen<?> containerScreen = (ContainerScreen) mc.currentScreen;

        for (int i = 0; i < containerScreen.getContainer().inventorySlots.size(); ++i) {
            String itemName = containerScreen.getContainer().inventorySlots.get(i).getStack().getDisplayName().getString();
            if (itemName.contains(name)) {
                return i;
            }
        }

        return slot;
    }

    public static class Hand3 {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

        @Subscribe
        public void onEventPacket(PacketEvent eventPacket) {
            if (eventPacket.getPacket() instanceof SHeldItemChangePacket) {
                this.isChangingItem = true;
            }

        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                Minecraft var10000 = mc;
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
