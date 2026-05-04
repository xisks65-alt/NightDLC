package dev.wh1tew1ndows.client.managers.module.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.other.ContainerRenderEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AucHelper", category = Category.MISC, desc = "Помощник аукциона — подсвечивает самый дешёвый лот")
public class AucHelper extends Module {

    public static AucHelper getInstance() {
        return Instance.get(AucHelper.class);
    }

    // Поиск предмета в руке по бинду
    public final BindSetting search = new BindSetting(this, "Поиск предмета", -1);

    // Фильтровать предметы (только валидные — с ценой)
    private final BooleanSetting checkFilters = new BooleanSetting(this, "Фильтровать предметы", false);

    // Дешёвый за слот (подсвечивать самый дешёвый по цене за 1 штуку)
    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Дешёвый за",
            BooleanSetting.of("Слот", true)
    );

    // Кэши — не создаём new каждый кадр
    private final List<PriceSlot> priceSlots          = new ArrayList<>(54);
    private final List<PriceSlot> pricesWithCountSlot  = new ArrayList<>(54);

    // ── Поиск по бинду ────────────────────────────────────────────────────

    @EventHandler
    public void onKey(EventKeyboardMouse event) {
        if (mc.player == null) return;
        if (!event.isKeyDown(search.getValue())) return;
        ItemStack stack = mc.player.getHeldItemMainhand();
        if (stack == null || stack.isEmpty()) return;
        String name = stack.getDisplayName().getString()
                .replaceAll("§.", "")
                .replaceAll("^\\[[^\\]]*\\]\\s*", "")
                .trim();
        if (!name.isEmpty()) mc.player.sendChatMessage("/ah search " + name);
    }

    // ── Рендер подсветки ──────────────────────────────────────────────────

    @EventHandler
    public void onContainerRender(ContainerRenderEvent event) {
        final Container container = event.getContainer();
        final ITextComponent title = event.getTitle();
        final MatrixStack matrix = event.getMatrix();

        if (!(container instanceof ChestContainer)) return;
        if (!isSearchScreen(title.getString())) return;

        priceSlots.clear();
        pricesWithCountSlot.clear();

        // Перебираем все слоты кроме инвентаря игрока (последние 36)
        final int totalSlots = container.inventorySlots.size();
        final int slotCount  = Math.max(0, totalSlots - 36);

        for (int i = 0; i < slotCount; i++) {
            Slot slot = container.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (checkFilters.getValue() && !hasPrice(stack)) continue;

            int price = getPrice(stack);
            if (price == -1) continue;

            priceSlots.add(new PriceSlot(i, price));
            pricesWithCountSlot.add(new PriceSlot(i, price / Math.max(1, stack.getCount())));
        }

        if (priceSlots.isEmpty()) return;

        priceSlots.sort(Comparator.comparingInt(o -> o.price));
        pricesWithCountSlot.sort(Comparator.comparingInt(o -> o.price));

        final float sz = 16;
        final Slot minSlot      = container.inventorySlots.get(priceSlots.get(0).slotIndex);
        final Slot minSlotCount = container.inventorySlots.get(pricesWithCountSlot.get(0).slotIndex);

        // Зелёным — самый дешёвый по слоту
        if (checks.getValue("Слот")) {
            RectUtil.drawRect(matrix, minSlot.xPos, minSlot.yPos, sz, sz, ColorUtil.multAlpha(ColorUtil.GREEN, 1f));
        }

        // Голубым — самый дешёвый за единицу (если отличается от зелёного)
        if (minSlotCount != minSlot) {
            RectUtil.drawRect(matrix, minSlotCount.xPos, minSlotCount.yPos, sz, sz, ColorUtil.multAlpha(Color.CYAN.getRGB(), 1f));
        }
    }

    // ── Утилиты ───────────────────────────────────────────────────────────

    private boolean hasPrice(ItemStack stack) {
        return getPrice(stack) != -1;
    }

    public static int getPrice(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains("display", 10)) return -1;
        CompoundNBT display = tag.getCompound("display");
        if (!display.contains("Lore", 9)) return -1;
        ListNBT lore = display.getList("Lore", 8);

        for (int j = 0; j < lore.size(); j++) {
            String raw = lore.getString(j);

            // Способ 1: JSON extra array
            try {
                JsonObject obj = new JsonParser().parse(raw).getAsJsonObject();
                if (obj.has("extra")) {
                    JsonArray arr = obj.getAsJsonArray("extra");
                    // Ищем элемент содержащий "цена" и следующий за ним с числом
                    for (int k = 0; k < arr.size() - 1; k++) {
                        String text = arr.get(k).getAsJsonObject().get("text").getAsString().trim().toLowerCase();
                        if (text.contains("цена") || text.contains("ценa") || text.contains("price")) {
                            // Следующий элемент — число
                            for (int m = k + 1; m < arr.size(); m++) {
                                String numStr = arr.get(m).getAsJsonObject().get("text").getAsString()
                                        .trim().replaceAll("[^0-9]", "");
                                if (!numStr.isEmpty()) {
                                    try { return Integer.parseInt(numStr); } catch (NumberFormatException ignored) {}
                                }
                            }
                        }
                    }
                }
                // Способ 2: plain text в JSON
                if (obj.has("text")) {
                    String text = obj.get("text").getAsString();
                    int price = extractPriceFromText(text);
                    if (price != -1) return price;
                }
            } catch (Exception ignored) {}

            // Способ 3: raw строка (убираем форматирование)
            String plain = TextFormatting.removeFormatting(raw).replaceAll("[\"{}\\[\\]]", "");
            int price = extractPriceFromText(plain);
            if (price != -1) return price;
        }
        return -1;
    }

    private static int extractPriceFromText(String text) {
        String lower = text.toLowerCase();
        if (!lower.contains("цена") && !lower.contains("ценa") && !lower.contains("price")) return -1;
        // Ищем число после слова цена
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:цена|ценa|price)[^0-9]*([0-9][0-9,. ]*)").matcher(lower);
        if (m.find()) {
            String num = m.group(1).replaceAll("[^0-9]", "");
            if (!num.isEmpty()) {
                try { return Integer.parseInt(num); } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    public boolean isSearchScreen(String title) {
        title = TextFormatting.removeFormatting(title);
        // Поддерживаем разные форматы заголовка аукциона
        return title.contains("Поиск:") || title.contains("Search:") || title.contains("Аукцион");
    }

    private record PriceSlot(int slotIndex, int price) {}
}
