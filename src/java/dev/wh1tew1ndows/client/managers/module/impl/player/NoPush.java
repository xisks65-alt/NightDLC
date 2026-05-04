package dev.wh1tew1ndows.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoPush", category = Category.PLAYER, desc = "Отключение толкания от мобов и игроков")
public class NoPush extends Module {
    public static NoPush getInstance() {
        return Instance.get(NoPush.class);
    }

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Не отталкиваться от",
            BooleanSetting.of("Игроков", true),
            BooleanSetting.of("Блоков", true),
            BooleanSetting.of("Воды", true)
    );
}
