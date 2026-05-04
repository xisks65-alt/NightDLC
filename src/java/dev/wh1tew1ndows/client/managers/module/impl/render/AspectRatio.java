package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AspectRatio", category = Category.RENDER, desc = "Изменение соотношения сторон экрана")
public class AspectRatio extends Module {
    public static AspectRatio getInstance() {
        return Instance.get(AspectRatio.class);
    }


    private final ModeSetting aspect = new ModeSetting(this, "Соотношение экрана", "16:9", "4:3", "1:1", "16:10", "21:9", "32:9", "5:4", "2:1", "Кастомное");
    private final SliderSetting customAspect = new SliderSetting(this, "Кастомое значние", 2, 1, 3, 0.1F)
            .setVisible(() -> this.aspect.is("Кастомное"));

    // private final ModeSetting mode = new ModeSetting(this, "Режим",
    //         "16:9", "1:1", "16:10", "4:3", "Custom"
    // );
//
    // private final SliderSetting ratio = new SliderSetting(this, "Значение", 0.5F, 0F, 1F, 0.01F).setVisible(() -> mode.is("Custom"));

    public float getAspectRation() {
        if (!this.isEnabled()) {
            return 0F;
        }

        float aspect = (float) mw.getWidth() / mw.getHeight();

        float newAspect = switch (this.aspect.getValue()) {
            case "16:9" -> 16F / 9f;
            case "4:3" -> 4F / 3F;
            case "1:1" -> 1F;
            case "16:10" -> 16F / 10F;
            case "21:9" -> 21F / 9F;
            case "32:9" -> 32F / 9F;
            case "5:4" -> 5F / 4F;
            case "2:1" -> 2F;
            default -> this.customAspect.getValue();
        };

        return newAspect - aspect;
    }

    // @EventHandler
    // public void onEvent(AspectRatioEvent event) {
    //     if (mode.getValue().equals("Custom")) {
    //         event.setAspectRatio(0.1F + ratio.getValue() * 2.5F);
    //         return;
    //     }
    //     String[] ratio = mode.getValue().split(":");
    //     float w = Float.parseFloat(ratio[0]);
    //     float h = Float.parseFloat(ratio[1]);
    //     event.setAspectRatio(w / h);
    // }
}