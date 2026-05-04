package dev.wh1tew1ndows.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FullBright", category = Category.RENDER, desc = "Полная яркость без факелов")
public class FullBright extends Module {
    public static FullBright getInstance() {
        return Instance.get(FullBright.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Гамма", "Ночное Зрение");
    
}