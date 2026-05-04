package dev.wh1tew1ndows.client.managers.module.legacyModule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CCloseWindowPacket;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "XCarry", category = Category.PLAYER, desc = "Перенос предметов в слотах X")
public class XCarry extends Module {
    public static XCarry getInstance() {
        return Instance.get(XCarry.class);
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof CCloseWindowPacket) {
            event.cancel();
        }
    }
}