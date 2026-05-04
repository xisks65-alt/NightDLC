package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.StopUseItemEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoFarm", category = Category.MISC, desc = "Автоматическое фермерство и сбор ресурсов")
public class AutoFarm extends Module {
    private final StopWatch watchClose = new StopWatch();
    private final StopWatch watchOther = new StopWatch();
    private boolean autoRepair;

    @Override
    public void toggle() {
        super.toggle();
        autoRepair = false;
    }

    @EventHandler
    public void onStopUseItem(StopUseItemEvent e) {
        e.setCancelled(mc.player.getFoodStats().needFood());
    }
}
