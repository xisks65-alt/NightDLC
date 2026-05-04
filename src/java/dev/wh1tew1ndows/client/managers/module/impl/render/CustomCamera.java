package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.CameraClipEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CustomCamera", category = Category.RENDER, desc = "Настройки камеры и угла обзора")
public class CustomCamera extends Module {
    public static CustomCamera getInstance() {
        return Instance.get(CustomCamera.class);
    }

    public SliderSetting sliderSetting = new SliderSetting(this, "Дистанция", 3, 1, 10, 0.5F);

    public SliderSetting sliderSetting2 = new SliderSetting(this, "Поворот камеры по X", 0, -2, 2, 0.05F);

    public BooleanSetting cameraclip = new BooleanSetting(this, "Camera Clip", true);

    @EventHandler
    public void onEvent(CameraClipEvent event) {
        if (cameraclip.getValue())
            event.cancel();
    }


}
