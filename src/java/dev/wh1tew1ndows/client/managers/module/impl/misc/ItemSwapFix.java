package dev.wh1tew1ndows.client.managers.module.impl.misc;


import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ItemSwapFix", category = Category.MISC, desc = "Исправление проблем со сменой предметов")
public class ItemSwapFix extends Module {


    @EventHandler
    public void onAttack(PacketEvent event) {
        if (event.isReceive() && event.getPacket() instanceof SHeldItemChangePacket) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            event.setCancelled(true);
        }
    }
}
