package dev.wh1tew1ndows.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.EntityHitBoxEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "HitBox", category = Category.COMBAT, desc = "Расширение хитбокса энтити")
public class HitBox extends Module {
    public static HitBox getInstance() {
        return Instance.get(HitBox.class);
    }

    private final MultiBooleanSetting targets = new MultiBooleanSetting(this, "Сущности",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Мобы", true)
    );

    private final SliderSetting playersSize = new SliderSetting(this, "Игроки", 0.3F, 0F, 2F, 0.05F).setVisible(() -> targets.getValue("Игроки"));
    private final SliderSetting mobsSize = new SliderSetting(this, "Мобы", 0.3F, 0F, 2F, 0.05F).setVisible(() -> targets.getValue("Мобы"));

    @EventHandler
    public void onEvent(EntityHitBoxEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (targets.getValue("Игроки") && event.getEntity() instanceof PlayerEntity)
            event.setSize(playersSize.getValue());
        if (targets.getValue("Мобы") && event.getEntity() instanceof MobEntity) event.setSize(mobsSize.getValue());
    }
}