package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoEat", category = Category.PLAYER, desc = "Автоматическое поедание пищи при голоде")
public class AutoEat extends Module {

    private final SliderSetting foodThreshold = new SliderSetting(this, "Уровень голода", 18, 4, 20, 1);
    private boolean active;

    @Override
    public void toggle() {
        super.toggle();
        active = false;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        mc.gameSettings.keyBindUseItem.setPressed(false);
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (shouldEat()) {
            startEating();
        } else if (active/* && mc.player.isHandActive() && mc.player.getActiveHand().equals(Hand.OFF_HAND)*/) {
            stopEating();
        }
    }

    private void startEating() {
        active = true;

        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
            mc.playerController.processRightClick(mc.player, mc.world, Hand.OFF_HAND);
            mc.gameSettings.keyBindUseItem.setPressed(true);
        }
    }

    private void stopEating() {
        mc.gameSettings.keyBindUseItem.setPressed(false);
        mc.playerController.onStoppedUsingItem(mc.player);
        mc.gameSettings.keyBindUseItem.setPressed(false);
        active = false;
    }

    private boolean shouldEat() {
        return mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.EAT) && mc.player.getFoodStats().needFood() && mc.player.getFoodStats().getFoodLevel() <= foodThreshold.getValue();
    }
}
