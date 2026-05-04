package dev.wh1tew1ndows.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CEntityActionPacket;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MoveEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Sneak", category = Category.MOVEMENT, desc = "Отключает замедление игрока при нажатии Shift")
public class Sneak extends Module {
    private final BooleanSetting onlySneakPress = new BooleanSetting(this, "Только на шифте", true);

    public static Sneak getInstance() {
        return Instance.get(Sneak.class);
    }

    private final StopWatch time = new StopWatch();


    @EventHandler
    public void onDisable() {
        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (!onlySneakPress.getValue() || mc.gameSettings.keyBindSneak.isKeyDown()) {
            if ((NoSlow.getInstance().isBlockUnderWithMotion() || mc.gameSettings.keyBindJump.isKeyDown()) && mc.player.isOnGround()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
            } else {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
            }
            mc.player.movementInput.sneaking = true;
        }
    }

    @EventHandler
    public void onMoveInput(MoveInputEvent event) {
        event.setSneakSlow(1);
    }
}
