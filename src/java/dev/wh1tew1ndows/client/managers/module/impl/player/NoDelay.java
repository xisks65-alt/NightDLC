package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
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
import net.minecraft.item.Items;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoDelay", category = Category.PLAYER, desc = "Убирает задержки при использовании предметов")
public class NoDelay extends Module {
    public static NoDelay getInstance() {
        return Instance.get(NoDelay.class);
    }

    private final MultiBooleanSetting elements = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("No Jump Delay", true),
            BooleanSetting.of("No Place Delay", false),
            BooleanSetting.of("Fast exp", false)
    );

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (elements.getValue("No Place Delay")) {
            mc.setRightClickDelayTimer(0);
        }
        if (elements.getValue("Fast exp") && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.setRightClickDelayTimer(1);
        }
    }

    @EventHandler
    public void onEvent(MotionEvent event) {
        if (elements.getValue("No Jump Delay") && mc.player.onGroundTicks > 0) {
            mc.player.setJumpTicks(0);
        }
    }
}