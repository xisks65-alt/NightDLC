package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import net.minecraft.network.play.client.CPlayerPacket;

@ModuleInfo(
        name = "NoServerRotation",
        category = Category.MISC,
        desc = "Уберает ротацию сервера"
)
public class NoServerRotation extends Module {
    public static NoServerRotation getInstance() {
        return Instance.get(NoServerRotation.class);
    }


    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (getInstance().isEnabled()) {
            if (event.isSend()) {
                if (this.isPacketSent) {
                    if (event.getPacket() instanceof CPlayerPacket playerPacket) {
                        //   playerPacket.setRotation(targetYaw, targetPitch);
                        this.isPacketSent = false;
                    }
                }
            }
        }
    }

    public void sendRotationPacket(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.isPacketSent = true;
    }
}

