package dev.wh1tew1ndows.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.DeathEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoRespawn", category = Category.PLAYER, desc = "Автоматическое возрождение после смерти")
public class AutoRespawn extends Module {
    public static AutoRespawn getInstance() {
        return Instance.get(AutoRespawn.class);
    }

    @EventHandler
    public void onEvent(DeathEvent event) {
        if (mc.player == null) return;
        mc.player.respawnPlayer();
        mc.displayScreen(null);
    }
}