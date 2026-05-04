package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.annotations.PVE;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@PVE
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoFish", category = Category.PLAYER, desc = "Автоматическая рыбалка")
public class AutoFish extends Module {
    public static AutoFish getInstance() {
        return Instance.get(AutoFish.class);
    }

    private final TimerUtil delay = new TimerUtil();
    private boolean isHooked = false;
    private boolean needToHook = false;

    @Override
    public void onDisable() {
        super.onDisable();
        delay.reset();
        isHooked = false;
        needToHook = false;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.world == null || mc.player.fishingBobber == null) return;

        if (event.getPacket() instanceof SPlaySoundEffectPacket wrapper) {
            if (wrapper.getSound().getName().getPath().equals("entity.fishing_bobber.splash")) {
                Vector3d soundPos = new Vector3d(wrapper.getX(), wrapper.getY(), wrapper.getZ());
                Vector3d bobberPos = mc.player.fishingBobber.getPositionVec();

                if (soundPos.distanceTo(bobberPos) <= 1.5) {
                    isHooked = true;
                    delay.reset();
                }
            }
        }
    }

    @EventHandler
    public void onUpdate2(UpdateEvent e) {
        if (delay.hasReached(600) && isHooked) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            isHooked = false;
            needToHook = true;
            delay.reset();
        }

        if (delay.hasReached(300) && needToHook) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            needToHook = false;
            delay.reset();
        }
    }
}