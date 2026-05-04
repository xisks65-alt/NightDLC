package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "TestModule", category = Category.MOVEMENT, desc = "Тестовый модуль")
public class TestModule extends Module {

    private final SliderSetting speed = new SliderSetting(this, "Скорость тест тест тест", 1.5F, 0.1F, 900, 0.1F);

    private final SliderSetting speedY = new SliderSetting(this, "ТестМодульТестКфгБуститНАЙТ)", 3, 1, 300, 1);
    private final SliderSetting speed33 = new SliderSetting(this, "НАЙТОЧЕНЬБУСТИТ!!!!CFGNAVP", 1.5F, 0.1F, 50, 0.1F);
}
