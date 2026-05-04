package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.managers.component.impl.rotation.FreeLookComponent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.PointOfView;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FreeLook", category = Category.RENDER, desc = "Свободный обзор камеры")
public class FreeLook extends Module {
    public static FreeLook getInstance() {
        return Instance.get(FreeLook.class);
    }


    @Override
    public void onDisable() {

        super.onDisable();
        if (!AttackAura.getInstance().isEnabled())
            FreeLookComponent.setActive(false);

        mc.gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!AttackAura.getInstance().isEnabled())
            mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
        if (mc.gameSettings.getPointOfView() == PointOfView.THIRD_PERSON_BACK) {
            if (!AttackAura.getInstance().isEnabled())
                FreeLookComponent.setActive(true);
        }
    }
}
