package dev.wh1tew1ndows.client.managers.module.legacyModule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AntiHunger", category = Category.MOVEMENT, desc = "Убирает замедление от голода")
public class AntiHunger extends Module {
    public static AntiHunger getInstance() {
        return Instance.get(AntiHunger.class);
    }
}
