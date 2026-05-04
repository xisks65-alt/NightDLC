package dev.wh1tew1ndows.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "SRPSpoof", category = Category.MISC, desc = "Подмена серверных ресурс-паков")
public class SRPSpoof extends Module {
    public static SRPSpoof getInstance() {
        return Instance.get(SRPSpoof.class);
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (event.getPacket() instanceof CResourcePackStatusPacket wrapper) {
            wrapper.action = CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED;
        }
    }
}