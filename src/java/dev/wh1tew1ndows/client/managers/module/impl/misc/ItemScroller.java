package dev.wh1tew1ndows.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ItemScroller", category = Category.MISC, desc = "Быстрое перемещение предметов в инвентаре колесиком мыши")
public class ItemScroller extends Module {
    public static ItemScroller getInstance() {
        return Instance.get(ItemScroller.class);
    }

    private final SliderSetting delay = new SliderSetting(this, "Задержка", 100, 0, 1000, 1);
    private final StopWatch time = new StopWatch();
}