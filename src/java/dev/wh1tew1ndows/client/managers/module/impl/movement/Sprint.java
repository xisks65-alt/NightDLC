package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;

@ModuleInfo(name = "Sprint", category = Category.MOVEMENT, desc = "Автоматический бег")
public class Sprint extends Module {

    public static Sprint getInstance() {
        return Instance.get(Sprint.class);
    }

    public BooleanSetting keepSprint = new BooleanSetting(this, "Сохранять спринт", false);
    public BooleanSetting antihynde = keepSprint;
}
