package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.time.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoLoot", category = Category.MISC, desc = "Автоматически забирает ресусры в сундуках, и эндер-честах")
public class AutoLoot extends Module {

    final SliderSetting delay = new SliderSetting(this, "Задержка", 50.0f, 1.0f, 500.0f, 1.0f);
    final MultiBooleanSetting lootTypes = new MultiBooleanSetting(this, "Типы контейнеров",
            BooleanSetting.of("Шалкера", true),
            BooleanSetting.of("Бочки", true),
            BooleanSetting.of("Сундуки", true),
            BooleanSetting.of("Всё", false));
    final StopWatch timer = new StopWatch();
    final Random random = new Random();
    final List<Integer> lootSlots = new ArrayList<>();
    int currentIndex = 0;

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        if (mc.player == null || mc.player.openContainer == null || mc.currentScreen == null) return;

        String title = mc.currentScreen.getTitle().getString().toLowerCase();
        boolean isChest = title.contains("сундук");
        boolean isBarrel = title.contains("бочка");
        boolean isShulker = title.contains("шалкер") || title.contains("shulker");

        boolean shouldLoot = (lootTypes.get("Сундуки").getValue() && isChest) ||
                (lootTypes.get("Бочки").getValue() && isBarrel) ||
                (lootTypes.get("Шалкера").getValue() && isShulker) ||
                lootTypes.get("Всё").getValue();

        if (!shouldLoot) {
            lootSlots.clear();
            currentIndex = 0;
            return;
        }

        Container container = mc.player.openContainer;
        int invSize = container.inventorySlots.size() - 36;

        if (invSize <= 0) {
            mc.player.closeScreen();
            lootSlots.clear();
            currentIndex = 0;
            return;
        }

        if (timer.isReached(Math.round(delay.getValue()))) {
            if (lootSlots.isEmpty()) {
                lootSlots.clear();
                for (int i = 0; i < invSize; i++) {
                    ItemStack stack = container.getSlot(i).getStack();
                    if (!stack.isEmpty()) {
                        lootSlots.add(i);
                    }
                }
                Collections.shuffle(lootSlots, random);
                currentIndex = 0;
            }

            if (currentIndex < lootSlots.size()) {
                int slot = lootSlots.get(currentIndex);
                if (mc.playerController != null) {
                    mc.playerController.windowClick(container.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                }
                currentIndex++;
            } else if (!lootSlots.isEmpty()) {
                mc.player.closeScreen();
                lootSlots.clear();
                currentIndex = 0;
            }
            timer.reset();
        }
    }
}