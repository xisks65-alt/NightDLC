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

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "SeeInvisibles", category = Category.MISC, desc = "Показывает невидимых игроков и мобов")
public class SeeInvisibles extends Module {
    public static SeeInvisibles getInstance() {
        return Instance.get(SeeInvisibles.class);
    }

    private final SliderSetting alpha = new SliderSetting(this, "Прозрачность", 0.5F, 0.0F, 1.0F, 0.1F);
}