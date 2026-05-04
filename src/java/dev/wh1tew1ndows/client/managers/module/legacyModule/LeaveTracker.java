package dev.wh1tew1ndows.client.managers.module.legacyModule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.EntityRemoveEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "LeaveTracker", category = Category.MISC, desc = "Отслеживание игроков, покинувших сервер")
public class LeaveTracker extends Module {
    public static LeaveTracker getInstance() {
        return Instance.get(LeaveTracker.class);
    }

    @EventHandler
    public void onEvent(EntityRemoveEvent event) {
        if (event.getEntity() instanceof AbstractClientPlayerEntity clientPlayer) {
            if (clientPlayer instanceof ClientPlayerEntity || mc.player.getDistance(clientPlayer) < 100) {
                return;
            }
            float x = (float) Mathf.round(clientPlayer.getPosX(), 1);
            float y = (float) Mathf.round(clientPlayer.getPosY(), 1);
            float z = (float) Mathf.round(clientPlayer.getPosZ(), 1);

            ChatUtil.addText(String.format("Игрок %s телепортировался на координаты xyz: %s, %s, %s", clientPlayer.getGameProfile().getName(), x, y, z));
        }
    }
}