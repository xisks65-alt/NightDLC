package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoRender", category = Category.RENDER, desc = "Отключение рендера различных элементов")
public class NoRender extends Module {
    public static NoRender getInstance() {
        return Instance.get(NoRender.class);
    }

    private final MultiBooleanSetting elements = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Огонь", true),
            BooleanSetting.of("Тряска", true),
            BooleanSetting.of("Боссбар", false),
            BooleanSetting.of("Плохие эффекты", true),
            BooleanSetting.of("Скорборд", false),
            BooleanSetting.of("Анимация тотема", true),
            BooleanSetting.of("Эффект воды", true),
            BooleanSetting.of("Эффект лавы", true),
            BooleanSetting.of("Эффект свечения", true)
    );
}
