package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CPlayerPacket;


@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AirStuck", category = Category.MOVEMENT, desc = "Заморозка персонажа в воздухе")
public class AirStuck extends Module {
    public static AirStuck getInstance() {
        return Instance.get(AirStuck.class);
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (!mc.player.isOnGround()) {
            mc.player.setVelocity(0, 0, 0);
        }
    }

    @EventHandler
    public void onMotion(MotionEvent eventMotion) {
        if (!mc.player.isOnGround()) {
            eventMotion.cancel();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!mc.player.isOnGround()) {
            if (e.getPacket() instanceof CPlayerPacket) {
                e.cancel();
            }
        }
    }

}
