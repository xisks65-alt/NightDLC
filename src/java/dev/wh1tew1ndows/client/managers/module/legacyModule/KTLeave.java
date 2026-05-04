package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import net.minecraft.network.play.client.CPlayerPacket;

@ModuleInfo(name = "KTLeave", category = Category.PLAYER, desc = "Автоматический выход с сервера KT")
public class KTLeave extends Module {

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (e instanceof UpdateEvent) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(3502035F, 3502035F, false));
            toggle();
        }

    }

}
