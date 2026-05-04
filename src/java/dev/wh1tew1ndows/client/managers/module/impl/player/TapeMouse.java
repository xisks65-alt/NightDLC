package dev.wh1tew1ndows.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.RayTraceResult;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "TapeMouse", category = Category.PLAYER, desc = "Фиксация мыши в определенной позиции")
public class TapeMouse extends Module {
    public static TapeMouse getInstance() {
        return Instance.get(TapeMouse.class);
    }

    private final BooleanSetting entityRaytrace = new BooleanSetting(this, "Проверка на энтити", false);
    private final ModeSetting attackMode = new ModeSetting(this, "Режим ударов", "По кулдауну", "По задержке");
    private final SliderSetting delay = new SliderSetting(this, "Задержка", 1000, 100, 5000, 100).setVisible(() -> attackMode.is("По задержке"));
    private final StopWatch timerUtil = new StopWatch();

    @Override
    public void toggle() {
        super.toggle();
        resetTimer();
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (entityRaytrace.getValue() && (mc.objectMouseOver == null || !mc.objectMouseOver.getType().equals(RayTraceResult.Type.ENTITY))) {
            return;
        }

        if (attackMode.is("По задержке") && timerUtil.finished(delay.getValue().intValue())) {
            mc.clickMouse();
            timerUtil.reset();
        }

        if (attackMode.is("По кулдауну") && mc.player.getCooledAttackStrength(1F) >= 1F) {
            mc.clickMouse();
        }
    }

    private void resetTimer() {
        timerUtil.reset();
    }
}