package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CustomModel", category = Category.RENDER, desc = "Изменение модельки игрока")
public class CustomModel extends Module {
    public static CustomModel getInstance() {
        return Instance.get(CustomModel.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Модель",
            "Демон", "Кролик", "Амогус", "Джефф");


}
